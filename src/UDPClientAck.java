import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class UDPClientAck {

    private static final int TIMEOUT_MS = 2000;   // how long we wait for an ACK
    private static final int MAX_ATTEMPTS = 5;    // total number of sends per message

    private String serverHost;
    private int serverPort;

    public UDPClientAck(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public void launch() {
        int sequenceNumber = 0;

        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            InetAddress serverAddress = InetAddress.getByName(serverHost);

            // receive() will throw SocketTimeoutException after this delay
            socket.setSoTimeout(TIMEOUT_MS);

            System.out.println("UDP Client started.");
            System.out.println("Enter messages (Ctrl+Z then Enter to stop):");

            while (scanner.hasNextLine()) {
                String message = scanner.nextLine();
                String numberedMessage = sequenceNumber + ":" + message;
                byte[] data = numberedMessage.getBytes(StandardCharsets.UTF_8);

                DatagramPacket packet = new DatagramPacket(
                        data, data.length, serverAddress, serverPort);

                boolean acknowledged = false;
                int attempt = 1;

                while (!acknowledged && attempt <= MAX_ATTEMPTS) {

                    socket.send(packet);
                    System.out.println("Sent (attempt " + attempt + "/" + MAX_ATTEMPTS + "): "
                            + numberedMessage);

                    try {
                        // Wait for the ACK; ignore ACKs that belong to other messages
                        while (!acknowledged) {
                            byte[] ackBuffer = new byte[64];
                            DatagramPacket ackPacket =
                                    new DatagramPacket(ackBuffer, ackBuffer.length);

                            socket.receive(ackPacket);

                            String reply = new String(
                                    ackPacket.getData(),
                                    0,
                                    ackPacket.getLength(),
                                    StandardCharsets.UTF_8).trim();

                            if (reply.equals("ACK:" + sequenceNumber)) {
                                System.out.println("Received " + reply);
                                acknowledged = true;
                            } else {
                                System.out.println("Ignored unexpected reply: " + reply);
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        System.out.println("Timeout: no ACK for message " + sequenceNumber);
                        attempt++;
                    }
                }

                if (!acknowledged) {
                    System.err.println("ERROR: message " + sequenceNumber
                            + " not acknowledged after " + MAX_ATTEMPTS + " attempts.");
                }

                // Move on to the next number in every case
                sequenceNumber++;
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

        UDPClientAck client = new UDPClientAck(host, port);
        client.launch();
    }
}