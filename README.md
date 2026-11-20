# TP2 — UDP Client-Server with Java Sockets

Java for Networks — 3RTS, ENSEA — 2026–2027

## Overview

This lab studies UDP communication using Java sockets. We implement a UDP client and server, explore datagram size limits and fragmentation, observe connectionless communication, add basic reliability mechanisms, and test multicast and broadcast.

The project currently contains **two source layouts** developed on separate Git branches. The commands below use the source layout that contains each program. **To run all parts from one checkout, both sets of files must first be present in the same working tree.**

## Requirements

- Java JDK (tested with Java 25); `java` and `javac` available in the terminal
- Windows PowerShell (commands below); other systems can adapt the paths
- Ncat (optional, for Step 2)
- Wireshark (for Steps 7 and 9)
- Two computers on the same network for the cross-computer tests

Check your Java installation:

```powershell
java -version
javac -version
```

## Project structure

```text
src/
  UDPClient.java             # Interactive client (Steps 3–4, 9)
  UDPServer.java             # Basic server (Steps 1–2, 9)
  UDPClientSeq.java          # Sequence-number client (Step 11)
  UDPServerSeq.java          # Sequence-number server (Step 11)
  UDPClientAck.java          # Client with ACKs and retries (Step 12)
  UDPServerAck.java          # Server that sends ACKs (Step 12)
  UDPClientLoss.java         # Sends a burst of numbered packets (Step 13)
  UDPServerLoss.java         # Counts received packets (Step 13)
  UDPClientAck14.java        # Client for simulated ACK loss (Step 14)
  UDPServerAck14.java        # Server for simulated ACK loss (Step 14)

src/main/java/fr/ensea/rts/udp/
  UDPServer.java             # Server with configurable buffer (Steps 5–8, 18)
  UDPClient.java             # Sends one CLI message (Steps 17–18)
  DatagramSizeTest.java      # Datagram-size tests (Step 6)
  MulticastReceiver.java     # Multicast receiver (Steps 16–17)
  PacketFormatter.java      # UDP packet decoding and display
```

The classes under `src/` have **no Java package**. The classes under `src/main/java/` belong to the `fr.ensea.rts.udp` package. They include classes with the same short names (`UDPClient` and `UDPServer`), but they are different programs with different command-line arguments.

## Compilation

Run these commands from the repository root in PowerShell.

**Programs in `src/` (Parts 1, 3, and 4):**

```powershell
New-Item -ItemType Directory -Force out-basic | Out-Null
javac -d out-basic src\*.java
```

**Packaged programs (Parts 2 and 5):**

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -d out src\main\java\fr\ensea\rts\udp\*.java
```

Use separate output folders to keep the two sets of programs clear.

### Run the Executable JARThe project includes an executable `UDPServer.jar`.

Run the UDP server directly without compiling the source code:

```powershell
java -jar UDPServer.jar 8080 1024
```

- `8080`: UDP listening port.
- `1024`: Receive buffer size in bytes.

The JAR was tested successfully with Java 25.

To stop the server, press `Ctrl+C`.

## Part 1 — Creating a UDP client-server (Steps 1–4)

### Steps 1–2: UDP server and Ncat

Start the server (default port: `8080`):

```powershell
java -cp out-basic UDPServer 8080
```

In another terminal, test it with Ncat:

```powershell
ncat -u localhost 8080
```

Type a message and press Enter. The server prints the sender IP address, source port, and UTF-8 message. Its receive buffer is 1024 bytes.

### Steps 3–4: Interactive UDP client

Start the same server, then launch the interactive client in another terminal:

```powershell
java -cp out-basic UDPClient localhost 8080
```

Type several messages. The client sends each line as a UDP datagram. On Windows, use **Ctrl+Z**, then **Enter**, to close standard input. You can also start two clients in different terminals and send messages to the same server. To test between computers, replace `localhost` with the receiving computer's IP address.

## Part 2 — Datagram analysis (Steps 5–8)

### Step 5: Receive buffer size

The packaged `UDPServer` accepts a port and an optional buffer size:

```powershell
java -cp out fr.ensea.rts.udp.UDPServer 8080 64
java -cp out fr.ensea.rts.udp.UDPServer 8080 2048
java -cp out fr.ensea.rts.udp.UDPServer 8080 65507
```

Run **one server at a time** on port 8080. With a 64-byte buffer, a longer message is truncated; the rest is not delivered as a second datagram. A larger buffer can receive a larger datagram without this application-level truncation.

### Step 6: Datagram size limits

Start the server with a large buffer:

```powershell
java -cp out fr.ensea.rts.udp.UDPServer 8080 65507
```

In another terminal:

```powershell
java -cp out fr.ensea.rts.udp.DatagramSizeTest 127.0.0.1 8080
```

The test program attempts UDP payloads of 1,000, 10,000, 65,507, and 65,508 bytes. In our tests, the first three sends succeeded; the 65,508-byte payload failed because it exceeds the usual IPv4 UDP payload maximum of 65,507 bytes. A successful `send()` does **not** by itself prove delivery.

### Step 7: Wireshark fragmentation

For a test between two computers, start the server on the receiving computer and replace the destination IP below with that computer's address:

```powershell
java -cp out fr.ensea.rts.udp.UDPServer 8080 65507
```

```powershell
java -cp out fr.ensea.rts.udp.DatagramSizeTest 192.168.32.3 8080
```
Our test configuration was:

- Client IP: `192.168.32.182`
- Server IP: `192.168.32.3`
- UDP port: `8080`

We used the following Wireshark display filter:

`ip.addr == 192.168.32.3`

Capture packets on the active network interface in Wireshark. Our observed results were:

- 1,000-byte UDP payload: no IPv4 fragmentation observed.
- 10,000-byte UDP payload: **7 IPv4 fragments** observed and reassembled.
- 65,507-byte payload: sending succeeded, but its fragmentation was not analyzed in detail.

In Wireshark, inspect the UDP source port, destination port, length, and checksum, and the IPv4 identification, fragment offset, and More Fragments flag.

### Step 8: Questions

**Why does the receiver choose its buffer size?** The receiver controls the memory available for incoming datagrams. If the buffer is too small, the datagram is truncated, and UDP does not automatically recover the missing bytes.

**Why avoid large UDP datagrams?** Large packets may be fragmented at the IP layer. Losing one fragment prevents reassembly of the whole datagram. Payloads around 1,400 bytes or less often reduce fragmentation risk on common Ethernet networks, although the safe size depends on the path MTU.

## Part 3 — Connectionless communication (Steps 9–10)

Use the interactive programs from Part 1:

```powershell
java -cp out-basic UDPClient localhost 8080
java -cp out-basic UDPServer 8080
```

**Step 9 experiments to perform and document:**

1. Send messages **before** starting the server. Then start the server and check whether earlier messages appear.
2. Send messages to UDP ports with no listening server. Capture traffic with Wireshark and look for ICMP Destination Unreachable / Port Unreachable messages, if the operating system and firewall allow them.
3. Keep the server running while starting and stopping the client several times. Compare the source ports printed by the server.

UDP does not establish a connection before sending. A successful client send does not guarantee that a receiver was listening. **The results of these experiments must be recorded from actual tests; they are not established by the source code alone.**

**Step 10 discussion:** Unlike UDP, TCP uses a connection handshake and connection state. UDP remains useful when low overhead and timely delivery matter, or when applications implement their own recovery mechanisms (for example, DNS queries, real-time streaming, and online games).

## Part 4 — Reliability over UDP (Steps 11–15)

Use the programs in `src/`, compiled into `out-basic`. Start the server first and the matching client second. **Stop one server before starting another on the same port.**

### Step 11: Sequence numbers

Server:

```powershell
java -cp out-basic UDPServerSeq 8080
```

Client:

```powershell
java -cp out-basic UDPClientSeq localhost 8080
```

The client sends numbered messages such as `0:hello`, `1:hello`. The server displays each sequence number and reports missing or out-of-order messages.

### Step 12: Acknowledgments and retransmissions

Server:

```powershell
java -cp out-basic UDPServerAck 8080
```

Client:

```powershell
java -cp out-basic UDPClientAck localhost 8080
```

The server replies with `ACK:n`. The client waits up to **2 seconds** for an ACK and retransmits up to **5 total attempts** if necessary. The server also identifies older sequence numbers as out-of-order or duplicates.

### Step 13: Packet-loss measurement

Server (port, delay in milliseconds, expected packet count):

```powershell
java -cp out-basic UDPServerLoss 5000 0 10000
```

Client (destination, port, number of packets):

```powershell
java -cp out-basic UDPClientLoss localhost 5000 10000
```

For the slower-server experiment, use a delay such as 2 ms:

```powershell
java -cp out-basic UDPServerLoss 5000 2 10000
```

The server waits up to 3 seconds for the next datagram after receiving the first one, then prints its received count and an estimated loss rate:

```text
loss rate (%) = max(0, expected - received) / expected * 100
```

For a two-computer test, replace `localhost` with the server computer's IP address. **Record measured results for each environment rather than assuming a fixed loss rate.** The current server counts received datagrams, but does not use a set of sequence numbers to detect duplicate packets.

### Step 14: Simulated acknowledgment loss

Server:

```powershell
java -cp out-basic UDPServerAck14 8080
```

Client:

```powershell
java -cp out-basic UDPClientAck14 localhost 8080
```

The server randomly **drops about 20% of ACK replies**, not 20% of the incoming data datagrams. The client waits **1 second** for an ACK and retries up to **5 times**. If an ACK is lost, the client can resend a message; the server recognizes an already processed sequence number and avoids processing it twice. This tests retry and duplicate-detection behavior, but is not exactly the same as dropping incoming data packets.

### Step 15: Questions

**Choosing a timeout:** A very short timeout causes unnecessary retransmissions; a very long timeout delays recovery. A practical timeout should account for expected network delay and its variation.

**Comparison with TCP:** We implemented simple sequence numbering, acknowledgments, timeouts, retries, and duplicate detection. We did not implement TCP's full sliding window, flow control, congestion control, connection management, or comprehensive ordered byte-stream delivery.

## Part 5 — UDP multicast and broadcast (Steps 16–19)

Use the packaged programs compiled into `out`.

### Step 16: Multicast receiver

The receiver joins a multicast group using `MulticastSocket.joinGroup()` with an explicit network interface:

```powershell
java -cp out fr.ensea.rts.udp.MulticastReceiver 239.0.0.1 8888 wireless_32768
```

`wireless_32768` was the Java network-interface name on our test computer. **It may be different on another computer.** The program can also try to select an active multicast-capable interface if the interface argument is omitted:

```powershell
java -cp out fr.ensea.rts.udp.MulticastReceiver
```

Automatic interface selection may choose the wrong adapter on a computer with multiple interfaces; specifying the correct interface is more reliable.

### Step 17: Sending multicast messages

Start several receivers in separate terminals, then send a message:

```powershell
java -cp out fr.ensea.rts.udp.UDPClient 239.0.0.1 8888 "Hello multicast"
```

We tested three receivers on one computer; all received the same multicast message. To make one receiver leave the group after 30 seconds:

```powershell
java -cp out fr.ensea.rts.udp.MulticastReceiver 239.0.0.1 8888 wireless_32768 30
```

The receiver calls `leaveGroup()` when its listening period ends. Other receivers continue listening. We also successfully tested multicast between two Windows computers on the same Wi-Fi network. Unlike the basic UDP server, multiple multicast sockets can join and receive traffic for the same group and port, subject to socket binding and OS behavior.

### Step 18: Broadcast

Start the packaged UDP server:

```powershell
java -cp out fr.ensea.rts.udp.UDPServer 8888
```

Send a limited broadcast:

```powershell
java -cp out fr.ensea.rts.udp.UDPClient 255.255.255.255 8888 "Hello UDP Broadcast"
```

The current client explicitly calls `setBroadcast(true)` when the host argument is `255.255.255.255`. In our tests, a **directed broadcast** also worked:

```powershell
java -cp out fr.ensea.rts.udp.UDPClient 192.168.35.255 8888 "Hello Broadcast from Rania PC"
```

`192.168.35.255` was the broadcast address of our test network (`192.168.32.0/22`); **do not copy this address for a different network**. The second computer received the message. Broadcast is generally restricted to the local broadcast domain; routers normally do not forward it to other networks.

**Implementation note:** To make directed-broadcast support explicit and portable, the client should also enable broadcast when sending to the subnet's directed broadcast address, rather than checking only `255.255.255.255`.

### Step 19: Questions

**20 unicast receivers vs multicast:** Sending the same message by unicast to 20 receivers requires 20 datagrams from the sender. Multicast requires one datagram from the sender, which the network can replicate where needed.

**Broadcast vs multicast:** Broadcast reaches all hosts in the local broadcast domain, including hosts that do not need the data. Multicast targets receivers that have joined a group and can be more efficient for one-to-many communication.

**Why UDP and not TCP?** UDP is datagram-based and connectionless, so one datagram can be sent to a broadcast or multicast destination. TCP is based on individual connections and does not natively support broadcast or multicast delivery.

## Notes and limitations

- The `integration-tp2-final` branch contains all 15 Java classes, the executable `UDPServer.jar`, the README, and the lab report PDF.
- Some experiments (especially Part 3 and the measured results of Part 4) need their observed outputs and screenshots added to the lab report.
- On Windows, allow incoming Java UDP traffic through the firewall when testing between computers.
- For Wireshark, select the actual active network interface; loopback and Wi-Fi captures may show different details.
- `out/` and `out-basic/` are build outputs, not source files.

## Conclusion

This lab demonstrates basic UDP communication, datagram limits and IP fragmentation, connectionless behavior, application-level reliability, multicast, and broadcast. The programs can be compiled and run from the command line, with different client-server pairs for different experiments.
