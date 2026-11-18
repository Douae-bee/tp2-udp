package fr.ensea.rts.udp;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * UDP server that receives UTF-8 strings in datagrams and displays them,
 * prefixed with the address and port of the sender.
 */
public class UDPServer {

    /** Default listening port. */
    public static final int DEFAULT_PORT = 8080;

    /** Maximum accepted message size in bytes; any extra data is truncated. */
    public static final int BUFFER_SIZE = 1024;

    private final int port;
    private final PrintStream out;
    private DatagramSocket socket;
    private volatile boolean running;

    /**
     * Creates a server listening on the given port and printing to the given stream.
     *
     * @param port the listening port
     * @param out  where received messages are displayed
     */
    public UDPServer(int port, PrintStream out) {
        this.port = port;
        this.out = out;
        this.running = false;
    }

    /**
     * Creates a server listening on the given port, printing to the standard output.
     *
     * @param port the listening port
     */
    public UDPServer(int port) {
        this(port, System.out);
    }

    /** Creates a server listening on {@link #DEFAULT_PORT}. */
    public UDPServer() {
        this(DEFAULT_PORT);
    }

    /**
     * Starts the server and receives datagrams until {@link #stop()} is called.
     *
     * @throws IOException if the socket cannot be opened or fails
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

    /** Stops the server; a blocked {@link #launch()} returns. */
    public void stop() {
        closeSocket();
    }

    /**
     * Tells whether the server is currently running.
     *
     * @return {@code true} if the server is listening
     */
    public boolean isRunning() {
        return running;
    }

    private void openSocket() throws SocketException {
        socket = new DatagramSocket(port);
        running = true;
    }

    private void closeSocket() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    private void receiveLoop() throws IOException {
        while (running) {
            try {
                out.println(PacketFormatter.format(receivePacket()));
            } catch (SocketException e) {
                // stop() closes the socket to unblock receive(): not an error
                if (running) {
                    throw e;
                }
            }
        }
    }

    private DatagramPacket receivePacket() throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return packet;
    }

    @Override
    public String toString() {
        return "UDPServer [port=" + port + ", running=" + running + "]";
    }

    static UDPServer createFromArgs(String[] args) {
        if (args.length >= 1) {
            return new UDPServer(Integer.parseInt(args[0]));
        }
        return new UDPServer();
    }

    /**
     * Starts a server. Usage: {@code java fr.ensea.rts.udp.UDPServer [port]}
     *
     * @param args optional listening port
     */
    public static void main(String[] args) {
        try {
            createFromArgs(args).launch();
        } catch (NumberFormatException e) {
            System.err.println("Invalid port. Usage: java fr.ensea.rts.udp.UDPServer [port]");
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
}