
package fr.ensea.rts.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Tests UDP datagrams of different sizes.
 */
public final class DatagramSizeTest {

    private static final int[] TEST_SIZES = {
            1000, 10000, 65507, 65508
    };

    private DatagramSizeTest() {
    }

    /**
     * Sends test datagrams to a UDP server.
     *
     * @param args optional server host and port
     */
    public static void main(String[] args) {
        if (args.length > 2) {
            System.err.println(
                    "Usage: DatagramSizeTest [host] [port]");
            return;
        }

        String host = args.length >= 1
                ? args[0] : "127.0.0.1";

        int port;

        try {
            port = args.length == 2
                    ? Integer.parseInt(args[1]) : 8080;

            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException(
                        "Port must be between 1 and 65535");
            }

            sendTestDatagrams(host, port);

        } catch (NumberFormatException e) {
            System.err.println("Invalid port number.");
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
        }
    }

    /**
     * Sends datagrams with increasing payload sizes.
     *
     * @param host destination host
     * @param port destination UDP port
     * @throws IOException if a network error occurs
     */
    private static void sendTestDatagrams(
            String host, int port) throws IOException {

        InetAddress address = InetAddress.getByName(host);

        try (DatagramSocket socket = new DatagramSocket()) {
            for (int size : TEST_SIZES) {
                byte[] data = new byte[size];

                // Fill the payload with ASCII characters.
                java.util.Arrays.fill(data, (byte) 'A');

                DatagramPacket packet =
                        new DatagramPacket(
                                data, data.length, address, port);

                try {
                    socket.send(packet);
                    System.out.println(
                            "Sent: " + size + " bytes");

                } catch (IOException e) {
                    System.out.println(
                            "Failed: " + size + " bytes - "
                                    + e.getMessage());
                }
            }
        }
    }
}
