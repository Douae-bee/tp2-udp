import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class UDPServerSeq {

    private int port;
    private int bufferSize;

    public UDPServerSeq(int port, int bufferSize) {
        this.port = port;
        this.bufferSize = bufferSize;
    }

    public UDPServerSeq(int port) {
        this(port, 1024);
    }

    public UDPServerSeq() {
        this(8080, 1024);
    }

    public void launch() {

        // Expected sequence number for the next message
        int expectedSequenceNumber = 0;

        try (DatagramSocket socket = new DatagramSocket(port)) {

            System.out.println("UDP Server started on port " + port);
            System.out.println("Waiting for messages...");

            while (true) {

                // Create the receiving buffer
                byte[] buffer = new byte[bufferSize];

                // Create the packet
                DatagramPacket packet = new DatagramPacket(
                        buffer,
                        buffer.length
                );

                // Receive the packet
                socket.receive(packet);

                // Extract the message
                String receivedMessage = new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );

                // Display sender information
                System.out.println("\n--------------------------------");
                System.out.println("Sender IP: " + packet.getAddress());
                System.out.println("Sender Port: " + packet.getPort());
                System.out.println("Bytes received: " + packet.getLength());

                // Extract the sequence number and message
                int separatorIndex = receivedMessage.indexOf(':');

                if (separatorIndex == -1) {
                    System.out.println("Invalid message: no sequence number found.");
                    System.out.println("Received: " + receivedMessage);
                    continue;
                }

                try {

                    // Extract sequence number
                    String sequenceString = receivedMessage.substring(
                            0,
                            separatorIndex
                    );

                    int receivedSequenceNumber =
                            Integer.parseInt(sequenceString);

                    // Extract the actual message
                    String actualMessage = receivedMessage.substring(
                            separatorIndex + 1
                    );

                    System.out.println("Sequence number: " + receivedSequenceNumber);
                    System.out.println("Message: " + actualMessage);

                    // Check the sequence number
                    if (receivedSequenceNumber == expectedSequenceNumber) {

                        System.out.println("Message received in correct order.");

                        expectedSequenceNumber++;

                    } else if (receivedSequenceNumber > expectedSequenceNumber) {

                        System.out.println("Missing message(s) detected!");

                        System.out.println(
                                "Expected sequence number: "
                                        + expectedSequenceNumber
                        );

                        System.out.println(
                                "Received sequence number: "
                                        + receivedSequenceNumber
                        );

                        // Update the expected number
                        expectedSequenceNumber =
                                receivedSequenceNumber + 1;

                    } else {

                        System.out.println("Out-of-order message detected!");

                        System.out.println(
                                "Expected sequence number: "
                                        + expectedSequenceNumber
                        );

                        System.out.println(
                                "Received sequence number: "
                                        + receivedSequenceNumber
                        );
                    }

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

        UDPServerSeq server = new UDPServerSeq(port, bufferSize);
        server.launch();
    }
}