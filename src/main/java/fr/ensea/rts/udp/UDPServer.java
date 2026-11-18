
package fr.ensea.rts.udp;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Objects;

/**
 * UDP server that receives UTF-8 datagrams and displays
 * each message with the sender's IP address and port.
 *
 * The receive buffer size can be configured.
 */
public class UDPServer {

    /** Default listening port. */
    public static final int DEFAULT_PORT = 8080;

    /** Default receive buffer size in bytes. */
    public static final int BUFFER_SIZE = 1024;

    /** Maximum UDP payload size over IPv4. */
    public static final int MAX_BUFFER_SIZE = 65507;

    private final int port;
    private final int bufferSize;
    private final PrintStream out;

    private DatagramSocket socket;
    private volatile boolean running;

    /**
     * Creates a server with a configurable receive buffer.
     *
     * @param port listening port (1 to 65535)
     * @param bufferSize receive buffer size (1 to 65507)
     * @param out output stream for received messages
     */
    public UDPServer(int port, int bufferSize, PrintStream out) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(
                    "Invalid port: " + port);
        }

        if (bufferSize < 1 || bufferSize > MAX_BUFFER_SIZE) {
            throw new IllegalArgumentException(
                    "Invalid buffer size: " + bufferSize);
        }

        this.port = port;
        this.bufferSize = bufferSize;
        this.out = Objects.requireNonNull(out, "Output stream");
        this.running = false;
    }

    /** Creates a server with a specified port and buffer size. */
    public UDPServer(int port, int bufferSize) {
        this(port, bufferSize, System.out);
    }

    /** Creates a server with the default buffer size. */
    public UDPServer(int port, PrintStream out) {
        this(port, BUFFER_SIZE, out);
    }

    /** Creates a server with a specified port. */
    public UDPServer(int port) {
        this(port, BUFFER_SIZE);
    }

    /** Creates a server with default port and buffer size. */
    public UDPServer() {
        this(DEFAULT_PORT, BUFFER_SIZE);
    }

    /**
     * Starts the UDP server and receives datagrams
     * until the server is stopped.
     *
     * @throws IOException if a socket error occurs
     */
    public void launch() throws IOException {
        openSocket();

        try {
            out.println("UDP Server started on port " + port);
            out.println(this);
            receiveLoop();
        } finally {
            closeSocket();
        }
    }

    /**
     * Stops the server and closes its socket.
     */
    public void stop() {
        closeSocket();
    }

    /**
     * Returns whether the server is running.
     *
     * @return true if the server is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Opens the UDP socket.
     *
     * @throws SocketException if the port cannot be opened
     */
    private void openSocket() throws SocketException {
        socket = new DatagramSocket(port);
        running = true;
    }

    /** Closes the UDP socket. */
    private void closeSocket() {
        running = false;

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    /**
     * Receives and displays UDP datagrams continuously.
     *
     * @throws IOException if receiving a datagram fails
     */
    private void receiveLoop() throws IOException {
        while (running) {
            try {
                DatagramPacket packet = receivePacket();
                out.println(PacketFormatter.format(packet));

            } catch (SocketException e) {
                // Closing the socket during stop() is expected.
                if (running) {
                    throw e;
                }
            }
        }
    }

    /**
     * Receives one UDP datagram using the configured buffer.
     *
     * @return received datagram
     * @throws IOException if reception fails
     */
    private DatagramPacket receivePacket() throws IOException {
        byte[] buffer = new byte[bufferSize];

        DatagramPacket packet =
                new DatagramPacket(buffer, buffer.length);

        socket.receive(packet);

        return packet;
    }

    /**
     * Returns the current server configuration.
     *
     * @return server state as a string
     */
    @Override
    public String toString() {
        return "UDPServer [port=" + port
                + ", bufferSize=" + bufferSize
                + ", running=" + running + "]";
    }

    /**
     * Creates a server from command-line arguments.
     *
     * @param args optional port and buffer size
     * @return configured UDP server
     */
    static UDPServer createFromArgs(String[] args) {
        if (args.length > 2) {
            throw new IllegalArgumentException(
                    "Usage: UDPServer [port] [bufferSize]");
        }

        int port = args.length >= 1
                ? Integer.parseInt(args[0])
                : DEFAULT_PORT;

        int size = args.length == 2
                ? Integer.parseInt(args[1])
                : BUFFER_SIZE;

        return new UDPServer(port, size);
    }

    /**
     * Starts the UDP server.
     *
     * Usage: UDPServer [port] [bufferSize]
     *
     * @param args optional port and buffer size
     */
    public static void main(String[] args) {
        try {
            UDPServer server = createFromArgs(args);
            server.launch();

        } catch (NumberFormatException e) {
            System.err.println(
                    "Port and buffer size must be valid integers.");

        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());

        } catch (IOException e) {
            System.err.println(
                    "Error starting server: " + e.getMessage());
        }
    }
}
