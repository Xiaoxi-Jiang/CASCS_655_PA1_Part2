### 1. How to Build

```bash
cd pa1part2
mkdir out
javac -encoding UTF-8 -d out $(find src -name "*.java")
```

### 2. How to Run

Server (for example using port 58034)

```bash
java -cp out Server 58034
```

Client (for example using host csa1.bu.edu): RTT mode, 20 probes, 1000 bytes each probe, server delay = 0

```bash
java -cp out Client csa1.bu.edu 58034 rtt 20 1000 0
```

Throughput mode, 20 probes, 2048 bytes (2KB) each prob, server delay = 0

```bash
java -cp out Client csa1.bu.edu 58034 tput 20 2048 0
```

### 3. Test Cases

Run Server on csa1

```bash
[xiaoxij@csa1 ~]$ cd pa1part2
[xiaoxij@csa1 pa1part2]$ mkdir out
[xiaoxij@csa1 pa1part2]$ javac -encoding UTF-8 -d out $(find src -name "*.java")
[xiaoxij@csa1 pa1part2]$ java -cp out Server 58034
Server is listening on port 58034 ...
```

Run Client on csa2, using RTT mode

```bash
[xiaoxij@csa2 ~]$ cd pa1part2
[xiaoxij@csa2 pa1part2]$ java -cp out Client csa1.bu.edu 58034 rtt 20 1000 0
Server connected.
[CSP SENT] s rtt 20 1000 0
[CSP RESP] 200 OK: Ready
[RESULT] avg RTT over 20 probes (size=1000B): 0.237 ms
[CTP RESP] 200 OK: Closing Connection
```

Output of Server

```
[xiaoxij@csa1 pa1part2]$ java -cp out Server 58034
Server is listening on port 58034 ...
CSP from /128.197.11.36:57138 => type=rtt N=20 size=1000 delay=0ms
```

Run Client on csa2, using Throughput mode

```bash
[xiaoxij@csa2 pa1part2]$ java -cp out Client csa1.bu.edu 58034 tput 20 8192 0
Server connected.
[CSP SENT] s tput 20 8192 0
[CSP RESP] 200 OK: Ready
[RESULT] avg RTT over 20 probes (size=8192B): 3.828 ms
[RESULT] avg Throughput over 20 probes (size=8192B): 71.442 Mbps
[CTP RESP] 200 OK: Closing Connection
```

Output of Server

```bash
[xiaoxij@csa1 pa1part2]$ java -cp out Server 58034
Server is listening on port 58034 ...
CSP from /128.197.11.36:57138 => type=rtt N=20 size=1000 delay=0ms
CSP from /128.197.11.36:45682 => type=tput N=20 size=8192 delay=0ms
```

Concurrency: run another client on csa3, and use `java -cp out Client csa1.bu.edu 58034 rtt 20 1000 500` on csa2. Then csa2 need a lot of time to wait for the server delay. If csa3 could finish a task before csa2, it will prove that the server could handle multiple connections in the same time.
the server received three CSP messages,

```bash
CSP from /128.197.11.36:41924 => type=rtt N=20 size=1000 delay=500ms
CSP from /128.197.11.45:40036 => type=tput N=20 size=1024 delay=0ms
CSP from /69.31.37.19:59049 => type=tput N=20 size=1024 delay=500ms
```

The first is from csa2, the second is from csa3, and the last is from my own computer. You can see csa3 has finished its job while csa2 and my computer are still processing.Finally, they all finished their job
### 4. Evaluation Enviroment

The server program is on the csa1 mechine and the client program is on the csa2 machine. My own computer is MacBook Air M2 with operation system macOS Tahoe 26.0.1. The network I use is the WIFI of my apartment. I manually collected the output of all the test results and wrote a python code to extract the result data. The probe number I used for each set of experiments is 20.
