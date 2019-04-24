package TCP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoMultiServer {
    private ServerSocket serverSocket;
    private String text = "";

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                new EchoClientHandler(serverSocket.accept()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insert(int insertedCharIndex, char insertedChar) {
        text = text.substring(0, insertedCharIndex) + insertedChar + text.substring(insertedCharIndex);
        System.out.println("text: " + text);
    }

    private void remove(int removedCharIndex) {
        text = text.substring(0, removedCharIndex) + text.substring(removedCharIndex + 1);
        System.out.println("text: " + text);
    }

    private class EchoClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        private String incomingCommand = "";
        private String textOnThread;

        private SenderThread senderThread;

        EchoClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            textOnThread = text;

            // start sender thread
            senderThread = new SenderThread();
            senderThread.start();

            try {
                incomingCommand = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (incomingCommand != null) {
                System.out.println(incomingCommand);
                doCommand();

                try {
                    incomingCommand = in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void doCommand() {
            String[] commands = incomingCommand.split("`");
            if (commands.length > 1) {
                int idx = Integer.parseInt(commands[0]);
                char character = commands[1].toCharArray()[0];

                insert(idx, character);
            } else {
                int idx = Integer.parseInt(commands[0]);

                remove(idx);
            }

            textOnThread = text;
        }

        private class SenderThread extends Thread {
            SenderThread() {

            }

            @Override
            public void run() {
                while (!incomingCommand.equals("end")) {
                    out.println(text);
//                    if (!textOnThread.equals(text)) {
//                        System.out.println("tot: " + textOnThread);
//                        out.println(text);
//                        textOnThread = text;
//                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        new EchoMultiServer().start(4445);
    }
}