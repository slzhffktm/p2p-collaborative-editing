import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class EchoServer extends Thread {

    private DatagramSocket socket;
    private boolean running;
    private byte[] buf;

    public EchoServer() {
        try {
            socket = new DatagramSocket(4445);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        running = true;

        while (running) {
            buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buf, buf.length, address, port);
            String received = new String(packet.getData(), 0, packet.getLength());

            System.out.println(received);

            if (received.equals("end")) {
                running = false;
                continue;
            }

            try {
                socket.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        socket.close();
    }

    public static void main(String args[]) {
        new EchoServer().start();
    }
}