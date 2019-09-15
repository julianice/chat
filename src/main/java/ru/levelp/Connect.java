package ru.levelp;

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


public class Connect extends Thread implements Listener {
    public static final int SERVER_PORT = 9994;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    public static List<Connect> connectList;
    private static EntityManagerFactory ef;


    public Connect(Socket connection) {
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
        String name = null;
        try {
            name = in.readLine();

            out.write(name + "\n");
            out.flush();

            synchronized (connectList) {
                for (Connect connect : connectList) {
                    connect.sendMessage(name + " joined us");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            String word;
            try {
                word = in.readLine();
                String send = name + ": " + word;
                System.out.println(send);
                Message msg = new Message(name, word);

                EntityManager manager = ef.createEntityManager();
                new DBTransaction(manager, msg);
                Message found = manager.find(Message.class, 1);
                System.out.println("I FOUNDDDDD      " + found.toString());

                synchronized (connectList) {
                    for (Connect connect : connectList) {
                        connect.sendMessage(send);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendMessage(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerListener(Listener listener) {
    }

    public static void main(String[] args) throws Exception {
        ef = Persistence.createEntityManagerFactory("TestPersistenceUnit");

        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        connectList = Collections.synchronizedList(new ArrayList<>());
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                pool.execute(() -> {
                            Connect connect = new Connect(socket);
                            connectList.add(connect);
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
//TODO DB
//TODO listener
//TODO do not send to yourself
//TODO ExecutorService
//TODO сохранять в БД и вытаскивать при старте записи
