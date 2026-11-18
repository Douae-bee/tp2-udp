import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;

public class UDPClientSeq {

    private String serverHost;
    private int serverPort;

    public UDPClientSeq(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public void launch() {
        int sequenceNumber = 0;

        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            InetAddress serverAddress = InetAddress.getByName(serverHost);

            System.out.println("UDP Client started.");
            System.out.println("Enter messages (Ctrl+Z then Enter to stop):");

            while (scanner.hasNextLine()) {
                String message = scanner.nextLine();

                // Add the sequence number to the message
                String numberedMessage = sequenceNumber + ":" + message;

                // Convert the message into bytes
                byte[] data = numberedMessage.getBytes(StandardCharsets.UTF_8);

                // Create the UDP packet
                DatagramPacket packet = new DatagramPacket(
                        data,
                        data.length,
                        serverAddress,
                        serverPort
                );

                // Send the packet
                socket.send(packet);

                System.out.println("Sent: " + numberedMessage);

                // Increment the sequence number
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

        UDPClientSeq client = new UDPClientSeq(host, port);
        client.launch();
    }
}