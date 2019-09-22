package ru.levelp.serverside;

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
        start();
    }

    @Override
    public void run() {
        String clientName = null;
        DBUtils dbUtils = new DBUtils(entityManagerFactory);

        try {
            clientName = in.readLine();
            out.write(dbUtils.showHistory());
            out.flush();

            synchronized (clientList) {
                sendMessageToClients(clientName + " joined us");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                String messageFromClient = in.readLine();
                String messageForSendingToAllClients = clientName + " : " + messageFromClient;
                Message message = new Message(clientName, messageFromClient);

                dbUtils.saveMessage(message);

                synchronized (clientList) {
                    sendMessageToClients(messageForSendingToAllClients);
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


    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            entityManagerFactory = Persistence.createEntityManagerFactory("TestPersistenceUnit");
            serverSocket = new ServerSocket(SERVER_PORT);
        } catch (IOException e) {
            entityManagerFactory.close();
        }

        clientList = Collections.synchronizedList(new ArrayList<>());
        ExecutorService pool = Executors.newFixedThreadPool(2);

        try {
            while (true) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Socket finalSocket = socket;
                pool.execute(() -> {
                            Client client = new Client(finalSocket);
                            clientList.add(client);
                        }
                );
                //Почему при добавлении 3 соединений sout пишет, что pool size = 2?
                System.out.println(pool.toString());
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

//TODO handle exceptions
//TODO synchronized synchronizedList it's ok? ------ OK
//TODO listener
//TODO ExecutorService
//TODO растащить Connect на части, а то уж больно много логики там в одну кучу сложено.
//TODO если одного из клиентов завершить, то все остальные в цикле получают null
