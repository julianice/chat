package ru.levelp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server extends Thread implements Listener{
    public static final int SERVER_PORT = 9994;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    public static List<Server> serverList;


    public Server(Socket connection) {
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
            synchronized (serverList) {
                for (Server sL : serverList) {
                    sL.sendMessage("Joined us ");
                }
            }        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            String word;
            try {
                word = in.readLine();
                String send = name + ": " + word;
                System.out.println(send);

            synchronized (serverList) {
                for (Server sL : serverList) {
                    sL.sendMessage(send);
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


    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        serverList = Collections.synchronizedList(new ArrayList<>());
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                Server server = new Server(socket);
                pool.execute(server);
                //Почему при добавлении 3 соединений sout пишет, что pool size = 2?
                System.out.println(pool.toString());
                serverList.add(server);
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
