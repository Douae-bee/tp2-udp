import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class UDPClientAck14 {

    private String serverHost;
    private int serverPort;

    private static final int TIMEOUT_MS = 1000;
    private static final int MAX_ATTEMPTS = 5;

    public UDPClientAck14(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public void launch() {

        int sequenceNumber = 0;

        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            InetAddress serverAddress = InetAddress.getByName(serverHost);

            socket.setSoTimeout(TIMEOUT_MS);

            System.out.println("UDP Client Step 14 started.");
            System.out.println("Enter messages (Ctrl+Z then Enter to stop):");

            while (scanner.hasNextLine()) {

                String message = scanner.nextLine();

                String numberedMessage = sequenceNumber + ":" + message;

                byte[] data = numberedMessage.getBytes(
                        StandardCharsets.UTF_8
                );

                DatagramPacket packet = new DatagramPacket(
                        data,
                        data.length,
                        serverAddress,
                        serverPort
                );

                boolean acknowledged = false;

                for (int attempt = 1;
                     attempt <= MAX_ATTEMPTS && !acknowledged;
                     attempt++) {

                    // Send or retransmit the message
                    socket.send(packet);

                    System.out.println(
                            "Sent (attempt " + attempt + "/"
                                    + MAX_ATTEMPTS + "): "
                                    + numberedMessage
                    );

                    try {

                        byte[] ackBuffer = new byte[1024];

                        DatagramPacket ackPacket = new DatagramPacket(
                                ackBuffer,
                                ackBuffer.length
                        );

                        socket.receive(ackPacket);

                        String ackMessage = new String(
                                ackPacket.getData(),
                                ackPacket.getOffset(),
                                ackPacket.getLength(),
                                StandardCharsets.UTF_8
                        );

                        String expectedAck = "ACK:" + sequenceNumber;

                        if (ackPacket.getAddress().equals(serverAddress)
                                && ackPacket.getPort() == serverPort
                                && ackMessage.equals(expectedAck)) {

                            System.out.println("Received " + ackMessage);
                            acknowledged = true;

                        } else {

                            System.out.println("Unexpected ACK received: "
                                    + ackMessage);

                            // Ignore unexpected ACKs and wait for the correct one
                            attempt--;
                        }

                    } catch (SocketTimeoutException e) {

                        System.out.println(
                                "Timeout: no ACK for message "
                                        + sequenceNumber
                        );
                    }
                }

                if (acknowledged) {

                    System.out.println("Message " + sequenceNumber
                            + " successfully acknowledged.");

                    sequenceNumber++;

                } else {

                    System.out.println(
                            "ERROR: message " + sequenceNumber
                                    + " not acknowledged after "
                                    + MAX_ATTEMPTS + " attempts."
                    );

                    // Stop to avoid creating a sequence-number gap
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        String host = "localhost";
        int port = 8080;

        if (args.length >= 1) {
            host = args[0];
        }

        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }

        UDPClientAck14 client = new UDPClientAck14(host, port);
        client.launch();
    }
}