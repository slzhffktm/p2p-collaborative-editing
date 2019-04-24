package UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Peer2Peer {

    private DatagramSocket socket;
    private int port;

    private boolean running = false;
    private byte[] buf;

    private ArrayList<InetAddress> addresses = new ArrayList<>();

    public Peer2Peer() {
        port = 4445;
        int addressNumber = 1;
        while (addressNumber <= 10) {
            String address = "127.0.0." + addressNumber;
            try {
                socket = new DatagramSocket(port, InetAddress.getByName(address));
                System.out.println("Creating new socket at " + socket.getLocalSocketAddress());
                running = true;
                break;
            } catch (Exception e) {
                ++addressNumber;
            }
        }

        if (running) {
            addAddresses(addressNumber);
            sendPing();
            new ReceiverThread().start();
        }
    }

    private void addAddresses(int addressNumber) {
        for (int i = 1; i < addressNumber; i++) {
            try {
                addresses.add(InetAddress.getByName("127.0.0." + i));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendPing() {
        sendEcho("p");
    }

    public void sendEcho(String msg) {
        buf = msg.getBytes();
        DatagramPacket packet;
        for (InetAddress address : addresses) {
            packet = new DatagramPacket(buf, buf.length, address, port);

            try {
                socket.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ReceiverThread extends Thread {
        ReceiverThread() {

        }

        @Override
        public void run() {
            super.run();

            DatagramPacket packet;

            while (running) {
                byte[] buf = new byte[256];

                packet = new DatagramPacket(buf, buf.length);

                try {
                    socket.receive(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                addAddress(packet.getAddress());

                String received = new String(packet.getData(), 0, packet.getLength());

                System.out.println(received);
            }
        }

        private void addAddress(InetAddress newAddress) {
            System.out.println("new address: " + newAddress);
            if (!addresses.contains(newAddress)) {
                addresses.add(newAddress);
            }
        }
    }

    public static void main(String[] args) {
        Peer2Peer peer2Peer = new Peer2Peer();

        Scanner sc = new Scanner(System.in);
        String msg;

        do {
            msg = sc.nextLine();
            peer2Peer.sendEcho(msg);
        } while (!msg.equals("end"));
    }
}
