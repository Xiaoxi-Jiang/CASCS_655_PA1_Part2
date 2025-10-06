### 1. How to Build

```bash
cd pa1part1
mkdir out
javac -encoding UTF-8 -d out $(find src -name "*.java")
```

### 2. How to Run

Server (for example using port 58034)

```bash
java -cp out Server 58034
```

Client (for example using host csa1.bu.edu)

```bash
java -cp out Client csa1.bu.edu 58034
```

### 3. Test Cases

**Functional:**
run server:

```bash
[xiaoxij@csa1 ~]$ cd pa1part1
[xiaoxij@csa1 pa1part1]$ mkdir out
[xiaoxij@csa1 pa1part1]$ javac -encoding UTF-8 -d out $(find src -name "*.java")
[xiaoxij@csa1 pa1part1]$ java -cp out Server 58034
Server is listening on port 58034 ...
```

run client: (`mkdir out` will be skipped, because there is already an out folder when I connected to the csa2 server)

```bash
[xiaoxij@csa2 ~]$ cd pa1part1/
[xiaoxij@csa2 pa1part1]$ javac -encoding UTF-8 -d out $(find src -name "*.java") 
[xiaoxij@csa2 pa1part1]$ java -cp out Client csa1.bu.edu 58034
Server connected, send messages or type 'exit' to quit
```

send message from client:

```bash
[xiaoxij@csa2 pa1part1]$ java -cp out Client csa1.bu.edu 58034
Server connected, send messages or type 'exit' to quit
hello, this is a message from csa2 server
hello, this is a message from csa2 server
hello👋
hello👋
test space       there are five space
test space       there are five space
     
     




test empty line and space above              
test empty line and space above
```

output of server :

```bash
[xiaoxij@csa1 pa1part1]$ java -cp out Server 58034
Server is listening on port 58034 ...
message from /128.197.11.36:34426: hello, this is a message from csa2 server
message from /128.197.11.36:34426: hello👋
message from /128.197.11.36:34426: test space       there are five space
message from /128.197.11.36:34426:      
message from /128.197.11.36:34426: 
message from /128.197.11.36:34426: 
message from /128.197.11.36:34426: test empty line and space above
```

Output above covers basic echo messages, emojis, spaces, and empty lines. All these types of messages can be correctly echoed back to the client.

**Concurrency:**

This part I will run client on csa2, csa3 and my own computer then connect to the server on csa1 and send messages.

client on csa3:

```bash
[xiaoxij@csa3 pa1part1]$ javac -encoding UTF-8 -d out $(find src -name "*.java")
[xiaoxij@csa3 pa1part1]$ java -cp out Client csa1.bu.edu 58034
Server connected, send messages or type 'exit' to quit
```

client on my own computer:

```bash
(base) awsomeone@jxx CASCS_655_PA1 % javac -encoding UTF-8 -d out $(find src -name "*.java")
(base) awsomeone@jxx CASCS_655_PA1 % java -cp out Client csa1.bu.edu 58034
Server connected, send messages or type 'exit' to quit
```

Now I will test sending different messages from all these clients.

client on csa3:

```bash
[xiaoxij@csa3 pa1part1]$ java -cp out Client csa1.bu.edu 58034
Server connected, send messages or type 'exit' to quit
this is a message from csa3 server😝
this is a message from csa3 server😝
            
            
test space above
test space above


test empty line above
test empty line above
```

client from my own computer:

```bash
(base) awsomeone@jxx CASCS_655_PA1 % java -cp out Client csa1.bu.edu 58034
Server connected, send messages or type 'exit' to quit
this is a message from my own computer🤓
this is a message from my own computer🤓
     
     
test space
test space


test empty line
test empty line
```

output of server:

```bash
[xiaoxij@csa1 pa1part1]$ java -cp out Server 58034
Server is listening on port 58034 ...
message from /128.197.11.36:34426: hello, this is a message from csa2 server
message from /128.197.11.36:34426: hello👋
message from /128.197.11.36:34426: test space       there are five space
message from /128.197.11.36:34426:      
message from /128.197.11.36:34426: 
message from /128.197.11.36:34426: 
message from /128.197.11.36:34426: test empty line and space above
message from /128.197.11.45:49346: this is a message from csa3 server😝
message from /128.197.11.45:49346:             
message from /128.197.11.45:49346: test space above
message from /128.197.11.45:49346: 
message from /128.197.11.45:49346: test empty line above
message from /69.31.37.19:64559: this is a message from my own computer🤓
message from /69.31.37.19:64559:      
message from /69.31.37.19:64559: test space
message from /69.31.37.19:64559: 
message from /69.31.37.19:64559: test empty line
```

We can see the server successfully receive all messages from different client.

**Robustness:**

When server registers a wrong port:

```bash
[xiaoxij@csa1 pa1part1]$ java -cp out Server 88888
Port must be in 58000–58999 on csa machines.
```

Running client while server not run:

```bash
[xiaoxij@csa2 pa1part1]$ java -cp out Client csa1.bu.edu 58034
Exception in thread "main" java.net.ConnectException: Connection refused (Connection refused)
	at java.net.PlainSocketImpl.socketConnect(Native Method)
	at java.net.AbstractPlainSocketImpl.doConnect(AbstractPlainSocketImpl.java:350)
	at java.net.AbstractPlainSocketImpl.connectToAddress(AbstractPlainSocketImpl.java:206)
	at java.net.AbstractPlainSocketImpl.connect(AbstractPlainSocketImpl.java:188)
	at java.net.SocksSocketImpl.connect(SocksSocketImpl.java:392)
	at java.net.Socket.connect(Socket.java:607)
	at java.net.Socket.connect(Socket.java:556)
	at java.net.Socket.<init>(Socket.java:452)
	at java.net.Socket.<init>(Socket.java:229)
	at Client.main(Client.java:22)
```

Peer closes: `readLine()==null` → handler exits cleanly.

### 4. Known Limitations

- Messages must end with newline (`\n`) due to line-based framing.

- One-thread-per-connection is not for very high concurrency.