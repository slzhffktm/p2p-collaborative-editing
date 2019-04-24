package TCP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class EchoClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendMessage(String msg) {
        out.println(msg);
        String resp = null;
        try {
            resp = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resp;
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

    public static void main(String[] args) {
        EchoClient client = new EchoClient();
        TextEditor textEditor = new TextEditor();

//        client.startConnection("localhost", 4445);
//
////        Scanner sc = new Scanner(System.in);
//        String echo;
//        String msg;
//
//        do {
//            msg = textEditor.getText();
//            echo = client.sendMessage(msg);
////            System.out.println(echo);
//            textEditor.setText(echo);
//        } while (!msg.equals("end"));
//
//        client.stopConnection();
    }
}