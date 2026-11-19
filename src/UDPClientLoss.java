import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class UDPClientLoss {

    private String serverHost;
    private int serverPort;
    private int numberOfPackets;

    public UDPClientLoss(String serverHost, int serverPort, int numberOfPackets) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.numberOfPackets = numberOfPackets;
    }

    public void launch() {

        try (DatagramSocket socket = new DatagramSocket()) {

            InetAddress serverAddress = InetAddress.getByName(serverHost);

            System.out.println("UDP Loss Client started.");
            System.out.println("Sending " + numberOfPackets + " datagrams...");

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < numberOfPackets; i++) {

                // Create a numbered message
                String message = i + ":Hello";

                byte[] data = message.getBytes(StandardCharsets.UTF_8);

                DatagramPacket packet = new DatagramPacket(
                        data,
                        data.length,
                        serverAddress,
                        serverPort
                );

                // Send without waiting for an ACK
                socket.send(packet);
            }

            long endTime = System.currentTimeMillis();

            System.out.println("All datagrams sent.");
            System.out.println("Datagrams sent: " + numberOfPackets);
            System.out.println("Time taken: " + (endTime - startTime) + " ms");

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        String host = "localhost";
        int port = 5000;
        int numberOfPackets = 10000;

        if (args.length >= 1) {
            host = args[0];
        }

        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }

        if (args.length >= 3) {
            numberOfPackets = Integer.parseInt(args[2]);
        }

        UDPClientLoss client = new UDPClientLoss(
                host,
                port,
                numberOfPackets
        );

        client.launch();
    }
}