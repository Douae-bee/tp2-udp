import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class UDPServerAck {

    private int port;
    private int bufferSize;

    public UDPServerAck(int port, int bufferSize) {
        this.port = port;
        this.bufferSize = bufferSize;
    }

    public UDPServerAck(int port) {
        this(port, 1024);
    }

    public UDPServerAck() {
        this(8080, 1024);
    }

    // Send "ACK:n" back to the sender of the received packet
    private void sendAck(DatagramSocket socket, DatagramPacket received, int sequenceNumber)
            throws Exception {

        byte[] ackData = ("ACK:" + sequenceNumber).getBytes(StandardCharsets.UTF_8);

        DatagramPacket ack = new DatagramPacket(
                ackData,
                ackData.length,
                received.getAddress(),   // address of the client
                received.getPort()       // source port of the client
        );

        socket.send(ack);
        System.out.println("ACK sent: ACK:" + sequenceNumber);
    }

    public void launch() {

        int expectedSequenceNumber = 0;

        try (DatagramSocket socket = new DatagramSocket(port)) {

            System.out.println("UDP Server started on port " + port);
            System.out.println("Waiting for messages...");

            while (true) {

                byte[] buffer = new byte[bufferSize];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);

                String receivedMessage = new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );

                System.out.println("\n--------------------------------");
                System.out.println("Sender IP: " + packet.getAddress());
                System.out.println("Sender Port: " + packet.getPort());
                System.out.println("Bytes received: " + packet.getLength());

                int separatorIndex = receivedMessage.indexOf(':');

                if (separatorIndex == -1) {
                    System.out.println("Invalid message: no sequence number found.");
                    System.out.println("Received: " + receivedMessage);
                    continue;
                }

                try {
                    int receivedSequenceNumber =
                            Integer.parseInt(receivedMessage.substring(0, separatorIndex));

                    String actualMessage = receivedMessage.substring(separatorIndex + 1);

                    System.out.println("Sequence number: " + receivedSequenceNumber);
                    System.out.println("Message: " + actualMessage);

                    if (receivedSequenceNumber == expectedSequenceNumber) {

                        System.out.println("Message received in correct order.");
                        expectedSequenceNumber++;

                    } else if (receivedSequenceNumber > expectedSequenceNumber) {

                        System.out.println("Missing message(s) detected!");
                        System.out.println("Expected sequence number: " + expectedSequenceNumber);
                        System.out.println("Received sequence number: " + receivedSequenceNumber);

                        expectedSequenceNumber = receivedSequenceNumber + 1;

                    } else {

                        System.out.println("Out-of-order or duplicate message detected!");
                        System.out.println("Expected sequence number: " + expectedSequenceNumber);
                        System.out.println("Received sequence number: " + receivedSequenceNumber);
                    }

                    // Step 12: acknowledge every valid message, even a duplicate
                    // (a duplicate probably means our previous ACK was lost)
                    sendAck(socket, packet, receivedSequenceNumber);

                } catch (NumberFormatException e) {
                    System.out.println("Invalid sequence number.");
                    System.out.println("Received: " + receivedMessage);
                }

                System.out.println("--------------------------------");
            }

        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        int port = 8080;
        int bufferSize = 1024;

        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        if (args.length >= 2) {
            bufferSize = Integer.parseInt(args[1]);
        }

        UDPServerAck server = new UDPServerAck(port, bufferSize);
        server.launch();
    }
}