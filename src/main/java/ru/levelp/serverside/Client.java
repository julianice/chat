package ru.levelp.serverside;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Client implements Runnable, Listener {
    public static final int SERVER_PORT = 9994;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    public static CopyOnWriteArrayList<Client> clientList;
    private static EntityManagerFactory entityManagerFactory;

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
    }

    @Override
    public void run() {
        DBUtils dbUtils = new DBUtils(entityManagerFactory);
        String clientName = null;
        try {
            clientName = in.readLine();
            out.write(dbUtils.showHistory());
            out.flush();
        } catch (IOException e) {
        }

        if (clientName == null) {
            System.out.println(clientList.remove(this));
            return;
        }

        sendMessageToClients(clientName + " joined us");

        while (true) {
            String messageFromClient = null;
            try {
                messageFromClient = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (messageFromClient == null) {
                clientList.remove(this);
                dbUtils.closeDBManager();
                break;
            }
            String messageForSendingToAllClients = clientName + " : " + messageFromClient;
            Message message = new Message(clientName, messageFromClient);
            dbUtils.saveMessage(message);
            sendMessageToClients(messageForSendingToAllClients);
        }
        dbUtils.closeDBManager();
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


    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            entityManagerFactory = Persistence.createEntityManagerFactory("TestPersistenceUnit");
            serverSocket = new ServerSocket(SERVER_PORT);
        } catch (IOException e) {
            entityManagerFactory.close();
        }
        clientList = new CopyOnWriteArrayList();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            while (true) {
                Client client;
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                pool.submit(client = new Client(socket));
                clientList.add(client);
            }
        } finally {
            try {
                entityManagerFactory.close();
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

//12. Вообще, было бы очень хорошо растащить Connect на части, а то уж больно много логики там в одну кучу сложено.
//TODO last 10 messages
