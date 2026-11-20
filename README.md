# TP2 — UDP Client-Server with Java Sockets

Java for Networks — 3RTS, ENSEA — 2026–2027

## Overview

This lab studies UDP communication using Java sockets. We implement a UDP client and server, explore datagram size limits and fragmentation, observe connectionless communication, add basic reliability mechanisms, and test multicast and broadcast.


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


### Run the Executable JARThe project includes an executable `UDPServer.jar`.

```powershell
java -jar UDPServer.jar 8080 1024
```

- `8080`: UDP listening port.
- `1024`: Receive buffer size in bytes.

The JAR was tested successfully with Java 25.


## Part 1 — Creating a UDP client-server (Steps 1–4)

### Steps 1–2: UDP server and Ncat

```powershell
java -cp out-basic UDPServer 8080
```

In another terminal:

```powershell
ncat -u localhost 8080
```

### Steps 3–4: Interactive UDP client

```powershell
java -cp out-basic UDPClient localhost 8080
```

## Part 2 — Datagram analysis (Steps 5–8)

### Step 5: Receive buffer size


### Step 6: Datagram size limits


```powershell
java -cp out fr.ensea.rts.udp.UDPServer 8080 65507
```

In another terminal:

```powershell
java -cp out fr.ensea.rts.udp.DatagramSizeTest 127.0.0.1 8080
```

### Step 7: Wireshark fragmentation

For a test between two computers:

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

## Part 3 — Connectionless communication (Steps 9–10)

```powershell
java -cp out-basic UDPClient localhost 8080
java -cp out-basic UDPServer 8080
```

**Step 9**

 **In the report**

**Step 10**  
 **In the report**

## Part 4 — Reliability over UDP (Steps 11–15)


### Step 11: Sequence numbers

Server:

```powershell
java -cp out-basic UDPServerSeq 8080
```

Client:

```powershell
java -cp out-basic UDPClientSeq localhost 8080
```

### Step 12: Acknowledgments and retransmissions

Server:

```powershell
java -cp out-basic UDPServerAck 8080
```

Client:

```powershell
java -cp out-basic UDPClientAck localhost 8080
```


### Step 13: Packet-loss measurement

Server (port, delay in milliseconds, expected packet count):

```powershell
java -cp out-basic UDPServerLoss 5000 0 10000
```

Client (destination, port, number of packets):

```powershell
java -cp out-basic UDPClientLoss localhost 5000 10000
```

```powershell
java -cp out-basic UDPServerLoss 5000 2 10000
```

```text
loss rate (%) = max(0, expected - received) / expected * 100
```

### Step 14: Simulated acknowledgment loss

Server:

```powershell
java -cp out-basic UDPServerAck14 8080
```

Client:

```powershell
java -cp out-basic UDPClientAck14 localhost 8080
```

### Step 15: Questions

**In the report** 
## Part 5 — UDP multicast and broadcast (Steps 16–19)

### Step 16: Multicast receiver

```powershell
java -cp out fr.ensea.rts.udp.MulticastReceiver 239.0.0.1 8888 wireless_32768
```

```powershell
java -cp out fr.ensea.rts.udp.MulticastReceiver
```

### Step 17: Sending multicast messages

```powershell
java -cp out fr.ensea.rts.udp.UDPClient 239.0.0.1 8888 "Hello multicast"
```

We tested three receivers on one computer

```powershell
java -cp out fr.ensea.rts.udp.MulticastReceiver 239.0.0.1 8888 wireless_32768 30
```

### Step 18: Broadcast

UDP server:

```powershell
java -cp out fr.ensea.rts.udp.UDPServer 8888
```


```powershell
java -cp out fr.ensea.rts.udp.UDPClient 255.255.255.255 8888 "Hello UDP Broadcast"
```

```powershell
java -cp out fr.ensea.rts.udp.UDPClient 192.168.35.255 8888 "Hello Broadcast from Rania PC"
```

`192.168.35.255` was the broadcast address of our test network (`192.168.32.0/22`); 
### Step 19: Questions

**in the report** 

## Conclusion

This lab demonstrates basic UDP communication, datagram limits and IP fragmentation, connectionless behavior, application-level reliability, multicast, and broadcast.
