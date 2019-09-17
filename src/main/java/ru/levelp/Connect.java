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
        EntityManager manager = ef.createEntityManager();
        try {
            name = in.readLine();
            out.write(sendLastMessages(manager));
            out.flush();

            synchronized (connectList) {
                sendMessage(name + " joined us");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            String message;
            try {
                message = in.readLine();
                String send = name + ": " + message;
                Message msg = new Message(name, message);

                new DBTransaction(manager, msg);

                synchronized (connectList) {
                    sendMessage(send);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendMessage(String msg) {
        for (Connect connect : connectList) {
            if (!connect.equals(this))
            try {
                connect.out.write(msg + "\n");
                connect.out.flush();
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
//TODO DB  ----- ok
//TODO listener
//TODO do not send to yourself
//TODO ExecutorService
//TODO сохранять в БД и вытаскивать при старте записи
