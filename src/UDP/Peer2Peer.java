package UDP;

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

    private String siteId;
    private CRDT crdt;
    private VersionVector vector;
    private ArrayList<Operation> deletionBuffer = new ArrayList<>();


    Peer2Peer() {
        port = 4445;
        int addressNumber = 1;
        while (addressNumber <= 10) {
            String address = "127.0.0." + addressNumber;
            try {
                socket = new DatagramSocket(port, InetAddress.getByName(address));
                System.out.println("Creating new socket at " + socket.getLocalSocketAddress());
                running = true;
                siteId = String.valueOf(socket.getLocalSocketAddress());
                break;
            } catch (Exception e) {
                ++addressNumber;
            }
        }

        if (running) {
            textEditor = new TextEditor();
            vector = new VersionVector(siteId);
            crdt = new CRDT(siteId, this);
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

    public static void main(String[] args) {
        Peer2Peer peer2Peer = new Peer2Peer();

        Scanner sc = new Scanner(System.in);
        String msg;

        do {
            msg = sc.nextLine();
            peer2Peer.sendEcho(msg);
        } while (!msg.equals("end"));
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

    private void sendEcho(String msg) {
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
        crdt.localInsert(insertedChar, insertedCharIndex);

        crdt.printString();

        System.out.println("i`" + insertedCharIndex + "`" + insertedChar);
        sendEcho("i`" + insertedCharIndex + "`" + insertedChar);        // send the command to all nodes
        text = textEditor.getText();
    }

    private void localDelete(int deletedCharIndex, char deletedChar) {
        // TODO: 4/25/2019 implement this
        Char c = crdt.localDelete(deletedCharIndex);

        crdt.printString();

        System.out.println("r`" + deletedCharIndex + "`" + deletedChar + "`" + c.getCounter());
        sendEcho("r`" + deletedCharIndex + "`" + deletedChar + "`" + c.getCounter());          // send the command to all nodes
        text = textEditor.getText();
    }

    private void remoteInsert(int insertedCharIndex, char insertedChar) {
//        ArrayList<Identifier> identifiers = new ArrayList<>();
//        identifiers.add(new Identifier(insertedCharIndex, siteId));
        Char c = crdt.generateChar(insertedChar, insertedCharIndex);

        System.out.println("remote insert");

        // TODO: 4/25/2019 implement this
        Version operationVersion = new Version(c.getSiteId(), c.getCounter());
        if (this.vector.hasBeenApplied(operationVersion)) {
            return;
        }

        System.out.println("after version");

        crdt.remoteInsert(c);
        this.vector.update(operationVersion);
        this.doDeletionBuffer();

        crdt.printString();

        text = crdt.getString();
//        text = text.substring(0, insertedCharIndex) + insertedChar + text.substring(insertedCharIndex);
        updateTextOnEditor();
        System.out.println("text: " + text);
    }

    private void doDeletionBuffer() {
        int inc = 0;
        while (inc < this.deletionBuffer.size()) {
            Operation operation = this.deletionBuffer.get(inc);
            if (this.isInsertionApplied(operation)) {
                Version operationVersion = new Version (operation.getC().getSiteId(), operation.getC().getCounter());
                this.crdt.remoteDelete(operation.getC());
                this.vector.update(operationVersion);
                this.deletionBuffer.remove(operation);
            } else {
                inc++;
            }
        }
    }

    private boolean isInsertionApplied(Operation operation) {
        Version v = new Version (operation.getC().getSiteId(), operation.getC().getCounter());
        return this.vector.hasBeenApplied(v);
    }

    private void remoteDelete(int deletedCharIndex, char deletedChar, int counter, String siteId) {
        ArrayList<Identifier> identifiers = new ArrayList<>();
        identifiers.add(new Identifier(deletedCharIndex, siteId));
        Char c = new Char(deletedChar, counter, siteId, identifiers);

        // TODO: 4/25/2019 implement this
        Operation operation = new Operation(c, 'r');
        this.deletionBuffer.add(operation);
        this.doDeletionBuffer();
        crdt.remoteDelete(c);

        crdt.printString();

        text = crdt.getString();
//        text = text.substring(0, deletedCharIndex) + text.substring(deletedCharIndex + 1);
        updateTextOnEditor();
        System.out.println("text: " + text);
    }

    VersionVector getVector() {
        return vector;
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

                remoteInsert(idx, character);
            } else if (commands[0].equals("r")) {
                int idx = Integer.parseInt(commands[1]);
                char character = commands[2].toCharArray()[0];
                int counter = Integer.parseInt(commands[3]);

                remoteDelete(idx, character, counter, siteId);
            }
        }
    }
}
