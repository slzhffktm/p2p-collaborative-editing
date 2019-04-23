package UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class EchoClient {
    private DatagramSocket socket;
    private InetAddress address;

    private byte[] buf;

    public EchoClient() {
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName("localhost");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String sendEcho(String msg) {
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);

        try {
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        packet = new DatagramPacket(buf, buf.length);

        try {
            socket.receive(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String received = new String(packet.getData(), 0, packet.getLength());

        return received;
    }

    public void close() {
        socket.close();
    }

    public static void main(String[] args) {
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