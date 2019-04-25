package UDP;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
                siteId = String.valueOf(socket.getLocalAddress());
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

    private String sendMsg(char command, char value, int counter, List<Identifier> position) {
        String send = "" + command + "`" + value + "`" + counter;
        for (Identifier pos : position) {
            send += "`" + pos.getDigit();
            send += "`" + pos.getSiteId();
        }
        return send;
    }

    private void localInsert(int insertedCharIndex, char insertedChar) {
        Char c = crdt.localInsert(insertedChar, insertedCharIndex);

        crdt.printString();

        System.out.println("i`" + insertedCharIndex + "`" + insertedChar);
        sendEcho(sendMsg('i', insertedChar, c.getCounter(), c.getPosition()));
        text = textEditor.getText();
    }

    private void localDelete(int deletedCharIndex, char deletedChar) {
        Char c = crdt.localDelete(deletedCharIndex);

        crdt.printString();

        System.out.println("r`" + deletedCharIndex + "`" + deletedChar + "`" + c.getCounter());
        sendEcho(sendMsg('r', deletedChar, c.getCounter(), c.getPosition()));

        text = textEditor.getText();
    }

    private void remoteInsert(Char c) {
        Version operationVersion = new Version(c.getSiteId(), c.getCounter());

        if (this.vector.hasBeenApplied(operationVersion)) {
            return;
        }

        crdt.remoteInsert(c);
        this.vector.update(operationVersion);
        this.doDeletionBuffer();

        crdt.printString();

        text = crdt.getString();

        updateTextOnEditor();
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
                crdt.printString();

                text = crdt.getString();

                updateTextOnEditor();
            } else {
                inc++;
            }
        }
    }

    private boolean isInsertionApplied(Operation operation) {
        Version v = new Version (operation.getC().getSiteId(), operation.getC().getCounter());
        return this.vector.hasBeenApplied(v);
    }

    private void remoteDelete(Char c) {
        Operation operation = new Operation(c, 'r');
        this.deletionBuffer.add(operation);
        this.doDeletionBuffer();
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
                char character = commands[1].toCharArray()[0];
                int counter = Integer.parseInt(commands[2]);

                Char c = new Char(character, counter, siteId,
                        convertToIdentifierList(Arrays.copyOfRange(commands, 3, commands.length)));
                remoteInsert(c);
            } else if (commands[0].equals("r")) {
                char character = commands[1].toCharArray()[0];
                int counter = Integer.parseInt(commands[2]);

                Char c = new Char(character, counter, siteId,
                        convertToIdentifierList(Arrays.copyOfRange(commands, 3, commands.length)));
                remoteDelete(c);
            }
        }

        private ArrayList<Identifier> convertToIdentifierList(String[] identifierStrings) {
            ArrayList<Identifier> identifiers = new ArrayList<>();

            for (int i = 0; i < identifierStrings.length; i += 2) {
                identifiers.add(new Identifier(Integer.parseInt(identifierStrings[i]), identifierStrings[i+1]));
            }

            return identifiers;
        }
    }
}
