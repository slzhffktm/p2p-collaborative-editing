package UDP;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        new EchoServer().start();

        EchoClient client = new EchoClient();

        Scanner sc = new Scanner(System.in);
        String echo;
        String msg;

        do {
            msg = sc.nextLine();
            echo = client.sendEcho(msg);
            System.out.println(echo);
        } while (!msg.equals("end"));

        client.close();
    }
}
