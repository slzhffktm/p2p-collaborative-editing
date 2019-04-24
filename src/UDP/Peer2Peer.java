package UDP;

import UDP.TextEditor;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Peer2Peer {

    private DatagramSocket socket;
    private int port;
    private ArrayList<InetAddress> addresses = new ArrayList<>();

    private boolean running = false;

    private TextEditor textEditor;
    private String text = "";
    private Boolean isUpdatingTextEditor = false;


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
            textEditor = new TextEditor();
            textEditor.getT().getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (!isUpdatingTextEditor) {
                        int insertedCharIndex = textEditor.getCursorPosition();
                        char insertedChar = textEditor.getText().charAt(insertedCharIndex);
                        localInsert(insertedCharIndex, insertedChar);
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (!isUpdatingTextEditor) {
                        int deletedCharIndex = textEditor.getCursorPosition() - 1;
                        char deletedChar = text.charAt(deletedCharIndex);
                        localDelete(deletedCharIndex, deletedChar);
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {

                }
            });

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
        byte[] buf = msg.getBytes();
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

    private void updateTextOnEditor() {
        int cursorPosition = textEditor.getCursorPosition();
        isUpdatingTextEditor = true;
        textEditor.setText(text);
        textEditor.setCursorPosition(cursorPosition);
        isUpdatingTextEditor = false;
    }

    private void localInsert(int insertedCharIndex, char insertedChar) {
        // TODO: 4/25/2019 implement this

        System.out.println("i`" + insertedCharIndex + "`" + insertedChar);
        sendEcho("i`" + insertedCharIndex + "`" + insertedChar);
        text = textEditor.getText();
    }

    private void localDelete(int deletedCharIndex, char deletedChar) {
        // TODO: 4/25/2019 implement this

        System.out.println("r`" + deletedCharIndex + "`" + deletedChar);
        sendEcho("r`" + deletedCharIndex + "`" + deletedChar);
        text = textEditor.getText();
    }

    private void remoteInsert(int insertedCharIndex, char insertedChar, String siteId) {
        // TODO: 4/25/2019 implement this

        text = text.substring(0, insertedCharIndex) + insertedChar + text.substring(insertedCharIndex);
        updateTextOnEditor();
        System.out.println("text: " + text);
    }

    private void remoteDelete(int deletedCharIndex, char deletedChar, String siteId) {
        // TODO: 4/25/2019 implement this

        text = text.substring(0, deletedCharIndex) + text.substring(deletedCharIndex + 1);
        updateTextOnEditor();
        System.out.println("text: " + text);
    }

    private class ReceiverThread extends Thread {
        private String incomingCommand;

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

                incomingCommand = new String(packet.getData(), 0, packet.getLength());
                System.out.println(incomingCommand);
                doCommand(packet.getAddress().toString());

            }
        }

        private void addAddress(InetAddress newAddress) {
            if (!addresses.contains(newAddress)) {
                System.out.println("Adding new address: " + newAddress);
                addresses.add(newAddress);
            }
        }

        private void doCommand(String siteId) {
            String[] commands = incomingCommand.split("`");

            if (commands[0].equals("i")) {
                int idx = Integer.parseInt(commands[1]);
                char character = commands[2].toCharArray()[0];

                remoteInsert(idx, character, siteId);
            } else if (commands[0].equals("r")) {
                int idx = Integer.parseInt(commands[1]);
                char character = commands[2].toCharArray()[0];

                remoteDelete(idx, character, siteId);
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
