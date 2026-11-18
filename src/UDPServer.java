import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class UDPServer {

    private int port;
    private boolean running;

    // Constructor with a specified port
    public UDPServer(int port) {
        this.port = port;
        this.running = false;
    }

    // Default constructor
    public UDPServer() {
        this(8080);
    }

    // Launch the server
    public void launch() throws IOException {

        DatagramSocket socket = new DatagramSocket(port);
        running = true;

        System.out.println("UDP Server started on port " + port);
        System.out.println(this);

        // Buffer to receive messages (maximum 1024 bytes)
        byte[] buffer = new byte[1024];

        // Infinite loop: the server continuously waits for messages
        while (true) {

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // Wait until a datagram arrives
            socket.receive(packet);

            // Convert received bytes into a UTF-8 string
            String message = new String(
                    packet.getData(),
                    0,
                    packet.getLength(),
                    StandardCharsets.UTF_8
            );

            // Get sender information
            String senderAddress = packet.getAddress().getHostAddress();
            int senderPort = packet.getPort();

            // Display the received message
            System.out.println(
                    "[" + senderAddress + ":" + senderPort + "] " + message
            );
        }
    }

    // Describing the server
    @Override
    public String toString() {
        return "UDPServer [port=" + port + ", running=" + running + "]";
    }

    // Main method
    public static void main(String[] args) {

        UDPServer server;

        if (args.length >= 1) {
            int port = Integer.parseInt(args[0]);
            server = new UDPServer(port);
        } else {
            server = new UDPServer();
        }

        try {
            server.launch();
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }
}