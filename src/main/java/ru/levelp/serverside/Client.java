package ru.levelp.serverside;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Client extends Thread implements Listener {
    public static final int SERVER_PORT = 9994;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    public static List<Client> clientList;
    private static EntityManagerFactory ef;

    public Client(Socket connection) {
        socket = connection;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            try {
                socket.close();
                in.close();
                out.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        start();
    }

    @Override
    public void run() {
        String clientName = null;
        EntityManager manager = ef.createEntityManager();
        try {
            clientName = in.readLine();
            out.write(sendLastMessages(manager));
            out.flush();

            synchronized (clientList) {
                sendMessageToClients(clientName + " joined us");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            String messageFromClient;
            try {
                messageFromClient = clientName + ": " + in.readLine();
                Message msg = new Message(clientName, messageFromClient);

                new DBTransaction(manager, msg);

                synchronized (clientList) {
                    sendMessageToClients(messageFromClient);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendMessageToClients(String message) {
        for (Client client : clientList) {
            if (!client.equals(this))
            try {
                client.out.write(message + "\n");
                client.out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String sendLastMessages(EntityManager manager) {
        List<Message> messageList = manager.createQuery("from Messages ", Message.class).getResultList();
        StringBuffer history = new StringBuffer();
        history.append("Last messages in chat: " + "\n");
        for (Message message : messageList) {
            history.append(message.getClientName() + ": " + message.getMessage() + "\n");
        }
        history.append("**************************************" + "\n");
        return String.valueOf(history);
    }

    public static void main(String[] args) throws Exception {
        ef = Persistence.createEntityManagerFactory("TestPersistenceUnit");
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        clientList = Collections.synchronizedList(new ArrayList<>());
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                pool.execute(() -> {
                            Client connect = new Client(socket);
                            clientList.add(connect);
                        }
                );
                //Почему при добавлении 3 соединений sout пишет, что pool size = 2?
                System.out.println(pool.toString());
            }
        } finally {
            serverSocket.close();
        }
    }
}

//TODO handle exceptions
//TODO synchronized synchronizedList it's ok? ------ OK
//TODO listener
//TODO ExecutorService