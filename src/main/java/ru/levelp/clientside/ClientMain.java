package ru.levelp.clientside;

import java.io.*;
import java.net.Socket;

import static ru.levelp.serverside.Client.SERVER_PORT;

public class ClientMain {
    private Socket socket;
    private BufferedWriter out;
    private BufferedReader in;
    private BufferedReader keyboardInput;
    private String name;

    public ClientMain(Socket connection) {
        socket = connection;
        try {
            keyboardInput = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            setName();
            new ReadMessageFromServerSide().start();
            new WriteMessageToServerSide().start();
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

    class ReadMessageFromServerSide extends Thread {
        @Override
        public void run() {
            String message;
            try {
                while (true) {
                    message = in.readLine();
                    if (message == null) {
                        closeClient();
                    } else
                        System.out.println(message);
                }
            } catch (IOException e) {
                closeClient();
            }
        }
    }

    class WriteMessageToServerSide extends Thread {
        @Override
        public void run() {
            while (true) {
                String message;
                try {
                    message = keyboardInput.readLine();
                    out.write(message + "\n");
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
        new ClientMain(socket);
    }
}
//TODO exit command