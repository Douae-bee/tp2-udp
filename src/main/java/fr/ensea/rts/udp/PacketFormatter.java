package fr.ensea.rts.udp;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

/**
 * Utility class that decodes and formats received datagrams.
 */
public final class PacketFormatter {

    private PacketFormatter() {
    }

    /**
     * Decodes the bytes actually received in a datagram as a UTF-8 string.
     *
     * @param packet the received datagram
     * @return the decoded message
     */
    public static String decode(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(),
                packet.getLength(), StandardCharsets.UTF_8);
    }

    /**
     * Formats a datagram for display, prefixed with the sender's address and port.
     *
     * @param packet the received datagram
     * @return a line such as {@code [127.0.0.1:54321] hello}
     */
    public static String format(DatagramPacket packet) {
        return "[" + packet.getAddress().getHostAddress() + ":" + packet.getPort()
                + "] " + decode(packet).stripTrailing();
    }
}