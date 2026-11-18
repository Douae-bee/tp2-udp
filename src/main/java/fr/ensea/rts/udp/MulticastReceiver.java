
package fr.ensea.rts.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * Receives UDP messages from a multicast group.
 * A receiver can optionally leave the group after a given duration.
 *
 * Usage: MulticastReceiver [group] [port] [interfaceName] [durationSeconds]
 */
public final class MulticastReceiver {

    private static final String DEFAULT_GROUP = "239.0.0.1";
    private static final int DEFAULT_PORT = 8888;
    private static final int BUFFER_SIZE = 65507;
    private static final int POLL_TIMEOUT_MS = 200;

    private MulticastReceiver() {
        // Prevent instantiation.
    }

    /**
     * Starts the multicast receiver.
     *
     * @param args optional group, port, interface name and duration
     */
    public static void main(String[] args) {
        if (args.length > 4) {
            printUsage();
            return;
        }

        try {
            String groupAddress = args.length > 0
                    ? args[0] : DEFAULT_GROUP;

            int port = args.length > 1
                    ? Integer.parseInt(args[1]) : DEFAULT_PORT;

            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Invalid UDP port.");
            }

            InetAddress group = InetAddress.getByName(groupAddress);

            if (!group.isMulticastAddress()) {
                throw new IllegalArgumentException(
                        "Not a multicast address.");
            }

            NetworkInterface networkInterface = args.length > 2
                    ? NetworkInterface.getByName(args[2])
                    : findNetworkInterface();

            if (networkInterface == null
                    || !networkInterface.isUp()
                    || !networkInterface.supportsMulticast()) {
                throw new IllegalArgumentException(
                        "Invalid multicast network interface.");
            }

            int durationSeconds = args.length > 3
                    ? Integer.parseInt(args[3]) : 0;

            if (durationSeconds < 0) {
                throw new IllegalArgumentException(
                        "Duration cannot be negative.");
            }

            receive(group, port, networkInterface, durationSeconds);

        } catch (IOException | IllegalArgumentException exception) {
            System.err.println(
                    "Receiver error: " + exception.getMessage());
        }
    }

    /**
     * Prints command-line usage.
     */
    private static void printUsage() {
        System.out.println(
                "Usage: MulticastReceiver "
                + "[group] [port] [interfaceName] [durationSeconds]");
    }

    /**
     * Finds an active network interface supporting multicast.
     *
     * @return a suitable interface, or null
     * @throws SocketException if interface lookup fails
     */
    private static NetworkInterface findNetworkInterface()
            throws SocketException {

        var interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface candidate = interfaces.nextElement();

            if (candidate.isUp()
                    && !candidate.isLoopback()
                    && candidate.supportsMulticast()) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * Joins a multicast group, receives messages and leaves cleanly.
     *
     * @param group multicast group address
     * @param port listening UDP port
     * @param networkInterface selected network interface
     * @param durationSeconds listening duration, 0 for unlimited
     * @throws IOException if a network operation fails
     */
    private static void receive(
            InetAddress group,
            int port,
            NetworkInterface networkInterface,
            int durationSeconds) throws IOException {

        InetSocketAddress groupSocketAddress =
                new InetSocketAddress(group, port);

        try (MulticastSocket socket = new MulticastSocket(port)) {

            socket.setNetworkInterface(networkInterface);
            socket.setSoTimeout(POLL_TIMEOUT_MS);

            socket.joinGroup(groupSocketAddress, networkInterface);

            System.out.println("Joined multicast group: "
                    + group.getHostAddress() + ":" + port);
            System.out.println("Interface: "
                    + networkInterface.getName());
            System.out.println("Waiting for messages...");

            long deadline = durationSeconds == 0
                    ? Long.MAX_VALUE
                    : System.nanoTime()
                    + durationSeconds * 1_000_000_000L;

            try {
                while (System.nanoTime() < deadline) {
                    byte[] buffer = new byte[BUFFER_SIZE];

                    DatagramPacket packet =
                            new DatagramPacket(buffer, buffer.length);

                    try {
                        socket.receive(packet);
                    } catch (SocketTimeoutException exception) {
                        // Check whether the listening duration has ended.
                        continue;
                    }

                    String message = new String(
                            packet.getData(),
                            packet.getOffset(),
                            packet.getLength(),
                            StandardCharsets.UTF_8);

                    System.out.println("Received from "
                            + packet.getAddress().getHostAddress()
                            + ":" + packet.getPort()
                            + " -> " + message);
                }
            } finally {
                socket.leaveGroup(groupSocketAddress, networkInterface);
                System.out.println("Left multicast group.");
            }
        }
    }
}
