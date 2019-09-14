package ru.levelp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server extends Thread {
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
//            try {
//                out.write(name + "\n");
//                out.flush();
//            } catch (IOException ignored) {}
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("It is " + name);
        while (true) {
            String word;
            try {
                word = in.readLine();
                String send = name + ": " + word;
                System.out.println(send);

                for (Server sL : serverList) {
                    sL.out.write(send + "\n");
                    sL.out.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(SERVER_PORT);
        serverList = new ArrayList<>();
        try {
            while (true) {
                Socket client = server.accept();
                Server serverObject = new Server(client);
                serverList.add(serverObject);
            }
        } finally {
            server.close();
        }
    }
}

//TODO handle exceptions
