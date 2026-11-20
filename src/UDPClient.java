import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * UDP client that reads messages from the keyboard
 * and sends them to a UDP server.
 */
public class UDPClient {

    private final String host;
    private final int port;

    /**
     * Creates a UDP client for the specified server.
     *
     * @param host the server hostname or IP address
     * @param port the server listening port
     */
    public UDPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Reads messages from standard input and sends them
     * to the UDP server.
     *
     * @throws IOException if an error occurs during communication
     */
    public void launch() throws IOException {

        InetAddress serverAddress = InetAddress.getByName(host);

        try (
            DatagramSocket socket = new DatagramSocket();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    System.in,
                    StandardCharsets.UTF_8
                )
            )
        ) {

            System.out.println(
                "UDP Client started. Sending messages to "
                + host + ":" + port
            );

            String line;

            // Read messages from the keyboard
            while ((line = reader.readLine()) != null) {

                // Convert the message into UTF-8 bytes
                byte[] data = line.getBytes(StandardCharsets.UTF_8);

                // Create the UDP datagram
                DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    serverAddress,
                    port
                );

                // Send the datagram
                socket.send(packet);

                System.out.println("Message sent: " + line);
            }
        }
    }

    /**
     * Entry point of the application.
     *
     * @param args server hostname and port
     */
    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println(
                "Usage: java UDPClient <host> <port>"
            );
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        UDPClient client = new UDPClient(host, port);

        try {
            client.launch();
        } catch (IOException e) {
            System.err.println(
                "Client error: " + e.getMessage()
            );
        }
    }
}