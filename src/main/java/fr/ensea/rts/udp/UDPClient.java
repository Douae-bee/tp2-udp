
package fr.ensea.rts.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * Simple UDP client that sends a message to a destination.
 *
 * Usage: java fr.ensea.rts.udp.UDPClient <host> <port> <message>
 */
public final class UDPClient {

    private UDPClient() {
        // Prevent instantiation.
    }

    /**
     * Reads the destination and message from command-line arguments.
     *
     * @param args host, port and message
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println(
                    "Usage: UDPClient <host> <port> <message>");
            return;
        }

        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);

            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Invalid UDP port.");
            }

            String message = String.join(
                    " ", java.util.Arrays.copyOfRange(
                            args, 2, args.length));

            sendMessage(host, port, message);

        } catch (NumberFormatException exception) {
            System.err.println("Invalid port number.");
        } catch (IllegalArgumentException exception) {
            System.err.println(exception.getMessage());
        } catch (IOException exception) {
            System.err.println("Network error: "
                    + exception.getMessage());
        }
    }

    /**
     * Sends a UTF-8 message as a UDP datagram.
     *
     * @param host destination IP address or hostname
     * @param port destination UDP port
     * @param message message to send
     * @throws IOException if the datagram cannot be sent
     */
    private static void sendMessage(
            String host, int port, String message) throws IOException {

        InetAddress address = InetAddress.getByName(host);
        byte[] data = message.getBytes(StandardCharsets.UTF_8);

        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket packet = new DatagramPacket(
                    data, data.length, address, port);

            socket.send(packet);

            System.out.println("Message sent to "
                    + host + ":" + port);
            System.out.println("Message: " + message);
        }
    }
}
