# tp2-udp
## Part 2 — UDP Datagram Analysis

This part explores UDP receive buffer sizes, maximum datagram sizes, and IPv4 fragmentation.

### Step 5 — Receive Buffer Size

We modified `UDPServer.java` to allow the user to choose the receive buffer size using a command-line argument.

Compile the Java files:

```powershell
javac -d out src\main\java\fr\ensea\rts\udp\*.java
```

Run the server with a 64-byte buffer:

```powershell
java -cp out fr.ensea.rts.udp.UDPServer 8080 64
```

We tested different buffer sizes:

- **64 bytes:** A message longer than the buffer was truncated.
- **2048 bytes:** A message of 1500 characters was received without visible data loss.
- **65007 bytes:** The server accepted a large receive buffer.

The receiver chooses the buffer size. If the buffer is too small, the remaining data is lost.

### Step 6 — Maximum UDP Datagram Size

We created `DatagramSizeTest.java` to test different UDP datagram sizes.

Start the server:

```powershell
java -cp out fr.ensea.rts.udp.UDPServer 8080 65007
```

Run the client:

```powershell
java -cp out fr.ensea.rts.udp.DatagramSizeTest 127.0.0.1 8080
```

Test results:

| Payload size | Result |
|---|---|
| 1000 bytes | Sent successfully |
| 10000 bytes | Sent successfully |
| 65507 bytes | Sent successfully |
| 65508 bytes | Failed — exceeds the IPv4 UDP payload limit |

The maximum UDP payload size for IPv4 with a 20-byte IP header is 65507 bytes.

### Step 7 — Wireshark Datagram Analysis

We used Wireshark to observe UDP datagrams and IPv4 fragmentation.

We first tested communication on the local computer using the loopback interface.

Then, we tested communication between two Windows computers on the same network.

- **Client IP:** `192.168.32.182`
- **Server IP:** `192.168.32.3`
- **UDP port:** `8080`

To reproduce the test, start the server on the receiving computer:

```powershell
java -cp out fr.ensea.rts.udp.UDPServer 8080 65007
```

On the client computer, replace the IP address with the server's actual address:

```powershell
java -cp out fr.ensea.rts.udp.DatagramSizeTest 192.168.32.3 8080
```

Start a Wireshark capture on the active network interface. Use this display filter, replacing the IP address if needed:

```text
ip.addr == 192.168.32.3
```

Observed results:

- **1000 bytes:** No IPv4 fragmentation.
- **10000 bytes:** Fragmented into 7 IPv4 packets.
- **65507 bytes:** Sent by the client, but its fragmentation was not analyzed in detail.

For the 10000-byte datagram, we observed:

- IP identification: `0x4941`
- First fragment offset: `0`
- Last fragment offset: `8880`
- Last fragment length: `1148 bytes`
- Last fragment: `More Fragments = 0`

Wireshark showed that all 7 fragments were reassembled.

### Step 8 — Questions

**Why does the receiver choose the receive buffer size?**

The receiver manages the memory used to store incoming data. If its buffer is too small, the UDP message is truncated.

**Why should we avoid large UDP datagrams?**

Large UDP datagrams may require IP fragmentation. If one fragment is lost, the complete datagram cannot be reassembled. Keeping UDP payloads around 1400 bytes or less helps avoid fragmentation on many common networks.

### Conclusion

We tested UDP receive buffer sizes, datagram size limits, and IPv4 fragmentation. We also verified UDP communication between two computers using Wireshark.