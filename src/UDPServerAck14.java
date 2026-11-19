import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class UDPServerAck14 {

    private int port;

    public UDPServerAck14(int port) {
        this.port = port;
    }

    public void launch() {

        int expectedSequenceNumber = 0;

        try (DatagramSocket socket = new DatagramSocket(port)) {

            System.out.println("UDP Server Step 14 started on port " + port);
            System.out.println("Waiting for messages...");

            while (true) {

                byte[] buffer = new byte[1024];

                DatagramPacket packet = new DatagramPacket(
                        buffer, buffer.length
                );

                socket.receive(packet);

                String receivedMessage = new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );

                System.out.println("\n--------------------------------");
                System.out.println("Received: " + receivedMessage);

                int separatorIndex = receivedMessage.indexOf(':');

                if (separatorIndex == -1) {
                    System.out.println("Invalid message format.");
                    continue;
                }

                try {

                    int sequenceNumber = Integer.parseInt(
                            receivedMessage.substring(0, separatorIndex)
                    );

                    String message = receivedMessage.substring(
                            separatorIndex + 1
                    );

                    System.out.println("Sequence number: " + sequenceNumber);

                    if (sequenceNumber == expectedSequenceNumber) {

                        // This is a new message: process it only once
                        System.out.println("New message: " + message);
                        System.out.println("Message processed.");

                        expectedSequenceNumber++;

                    } else if (sequenceNumber < expectedSequenceNumber) {

                        // This message has already been processed
                        System.out.println("DUPLICATE detected!");
                        System.out.println("Message will NOT be processed again.");

                    } else {

                        // A message is missing
                        System.out.println("Out-of-order message detected!");
                        System.out.println("Expected: " + expectedSequenceNumber);
                        System.out.println("Received: " + sequenceNumber);

                        System.out.println("--------------------------------");
                        continue;
                    }

                    // Simulate 20% ACK loss
                    double randomValue = Math.random();

                    if (randomValue < 0.20) {

                        System.out.println("ACK:" + sequenceNumber
                                + " DROPPED (simulated loss)");

                    } else {

                        String ackMessage = "ACK:" + sequenceNumber;
                        byte[] ackData = ackMessage.getBytes(
                                StandardCharsets.UTF_8
                        );

                        DatagramPacket ackPacket = new DatagramPacket(
                                ackData,
                                ackData.length,
                                packet.getAddress(),
                                packet.getPort()
                        );

                        socket.send(ackPacket);

                        System.out.println("ACK sent: " + ackMessage);
                    }

                } catch (NumberFormatException e) {
                    System.out.println("Invalid sequence number.");
                }

                System.out.println("--------------------------------");
            }

        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        int port = 8080;

        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        UDPServerAck14 server = new UDPServerAck14(port);
        server.launch();
    }
}