import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

/**
 * UDP server that receives datagrams and displays their content.
 */
public class UDPServer {

    private final int port;
    private boolean running;

    /**
     * Creates a UDP server on the specified port.
     *
     * @param port the listening port
     */
    public UDPServer(int port) {
        this.port = port;
        this.running = false;
    }

    /**
     * Creates a UDP server using the default port 8080.
     */
    public UDPServer() {
        this(8080);
    }

    /**
     * Starts the server and continuously receives UDP datagrams.
     *
     * @throws IOException if an error occurs while opening or receiving
     */
    public void launch() throws IOException {

        try (DatagramSocket socket = new DatagramSocket(port)) {

            running = true;

            System.out.println("UDP Server started on port " + port);
            System.out.println(this);

            byte[] buffer = new byte[1024];

            while (true) {

                DatagramPacket packet =
                        new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);

                String message = new String(
                        packet.getData(),
                        0,
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );

                String senderAddress =
                        packet.getAddress().getHostAddress();

                int senderPort = packet.getPort();

                System.out.println(
                        "[" + senderAddress + ":" + senderPort + "] "
                        + message
                );
            }
        }
    }

    /**
     * Returns a textual description of the server.
     *
     * @return the server port and running state
     */
    @Override
    public String toString() {
        return "UDPServer [port=" + port
                + ", running=" + running + "]";
    }

    /**
     * Entry point of the application.
     *
     * @param args optional listening port
     */
    public static void main(String[] args) {

        UDPServer server;

        if (args.length > 0) {
            server = new UDPServer(Integer.parseInt(args[0]));
        } else {
            server = new UDPServer();
        }

        try {
            server.launch();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}