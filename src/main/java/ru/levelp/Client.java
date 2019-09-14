package ru.levelp;

import java.io.*;
import java.net.Socket;

import static ru.levelp.Server.SERVER_PORT;

public class Client {
    private Socket socket;
    private BufferedWriter out;
    private BufferedReader in;
    private BufferedReader keyboardInput;
    private String name;

    public Client(Socket connection) {
        socket = connection;
        try {
            keyboardInput = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            setName();
            new ReadThread().start();
            new WriteThread().start();

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

    private void setName() {
        System.out.print("Introduce yourself please: ");
        try {
            name = keyboardInput.readLine();
            out.write(name + "\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ReadThread extends Thread {
        @Override
        public void run() {
            String str;
            try {
                while (true) {
                    str = in.readLine();
                    System.out.println(str);
                }
            } catch (IOException e) {
                try {
                    socket.close();
                    out.close();
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    class WriteThread extends Thread {
        @Override
        public void run() {
            while (true) {
                String word;
                try {
                    word = keyboardInput.readLine();
                    out.write(word + "\n");
                    out.flush();
                } catch (IOException e) {
                    try {
                        socket.close();
                        out.close();
                        in.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", SERVER_PORT);
        new Client(socket);
    }
}
