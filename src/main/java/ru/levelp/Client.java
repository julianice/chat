package ru.levelp;

import java.io.*;
import java.net.Socket;

import static ru.levelp.Connect.SERVER_PORT;

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
            closeClient();
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
                    if (str == null) {
                        closeClient();
                    } else
                        System.out.println(str);
                }
            } catch (IOException e) {
                closeClient();
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
                    closeClient();
                }
            }
        }
    }

    public void closeClient() {
            System.out.println("Socket is closed");
            try {
                socket.close();
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", SERVER_PORT);
        new Client(socket);
    }
}
