package TCP;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private TextEditor textEditor;

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private String serverText = "";

    private Boolean isUpdatingTextEditor = false;

    public Client() {
        textEditor = new TextEditor();
        textEditor.getT().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!isUpdatingTextEditor) {
                    int insertedCharIndex = textEditor.getCursorPosition();
                    System.out.println("cursor pos updating: " + insertedCharIndex);
                    char insertedChar = textEditor.getText().charAt(insertedCharIndex);
                    System.out.println(insertedCharIndex + "`" + insertedChar);
                    sendMessage(insertedCharIndex + "`" + insertedChar);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!isUpdatingTextEditor) {
                    int removedCharIndex = textEditor.getCursorPosition() - 1;
                    sendMessage(Integer.toString(removedCharIndex));
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!isUpdatingTextEditor) {
                    serverText = textEditor.getText();
                }
            }
        });
    }

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            new ReceiveMessageThread().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String msg) {
        out.println(msg);
    }

    /**
     * class ReceiveMessageThread
     */
    private class ReceiveMessageThread extends Thread {
        private ReceiveMessageThread() {

        }

        private void updateDocument() {
//            int cursorPosition = textEditor.getCursorPosition();
//            System.out.println("cursor pos belum diisi:" + cursorPosition);
            textEditor.setText(serverText);
//            textEditor.setCursorPosition(cursorPosition);
        }

        private void receiveMessage() {
            String message = null;
            try {
                message = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (message != null) {
                if (!message.equals(serverText)) {
                    isUpdatingTextEditor = true;
                    System.out.println("Message: " + message);
                    serverText = message;
                    updateDocument();
                    isUpdatingTextEditor = false;
                }

                try {
                    message = in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            System.out.println("ReceiveMessageThread is running...");
            receiveMessage();
            System.out.println("ReceiveMessageThread is ending...");
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.startConnection("localhost", 4445);
    }
}
