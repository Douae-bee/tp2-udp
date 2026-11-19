import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class UDPServerLoss {

    private int port;
    private int bufferSize;
    private int delayMs;
    private int expectedPackets;

    public UDPServerLoss(int port, int bufferSize, int delayMs, int expectedPackets) {
        this.port = port;
        this.bufferSize = bufferSize;
        this.delayMs = delayMs;
        this.expectedPackets = expectedPackets;
    }

    public void launch() {

        int receivedCount = 0;
        boolean firstPacketReceived = false;

        try (DatagramSocket socket = new DatagramSocket(port)) {

            System.out.println("UDP Loss Server started on port " + port);
            System.out.println("Waiting for datagrams...");

            while (true) {

                byte[] buffer = new byte[bufferSize];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                try {
                    // After the first packet, wait at most 3 seconds
                    // for the next packet.
                    if (firstPacketReceived) {
                        socket.setSoTimeout(3000);
                    }

                    socket.receive(packet);
                    firstPacketReceived = true;

                    String message = new String(
                            packet.getData(),
                            packet.getOffset(),
                            packet.getLength(),
                            StandardCharsets.UTF_8
                    );

                    receivedCount++;

                    if (receivedCount % 1000 == 0) {
                        System.out.println("Received: " + receivedCount);
                    }

                    // Optional delay to simulate a slower server
                    if (delayMs > 0) {
                        Thread.sleep(delayMs);
                    }

                } catch (SocketTimeoutException e) {
                    System.out.println("\nNo more datagrams received. Ending test.");
                    break;
                }
            }

            int lostPackets = expectedPackets - receivedCount;

            // Avoid negative loss if the received count exceeds expectation
            double lossRate = Math.max(0, lostPackets) * 100.0 / expectedPackets;

            System.out.println("\n========== RESULTS ==========");
            System.out.println("Expected datagrams: " + expectedPackets);
            System.out.println("Received datagrams: " + receivedCount);
            System.out.println("Lost datagrams: " + Math.max(0, lostPackets));
            System.out.printf("Loss rate: %.2f%%%n", lossRate);
            System.out.println("=============================");

        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        int port = 5000;
        int delayMs = 0;
        int expectedPackets = 10000;

        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        if (args.length >= 2) {
            delayMs = Integer.parseInt(args[1]);
        }

        if (args.length >= 3) {
            expectedPackets = Integer.parseInt(args[2]);
        }

        UDPServerLoss server = new UDPServerLoss(
                port, 1024, delayMs, expectedPackets
        );

        server.launch();
    }
}