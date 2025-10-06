import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerThread extends Thread {
    private final Socket socket;

    static class Session{
        String type; //rtt or tput
        int numProbes;
        int msgSizeBytes;
        int serverDelayMs;

    }

    public ServerThread(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            String first = in.readLine();
            if (first == null){
                safeClose(in, out);
                socket.close();
                return;
            }
            Session sess = parseAndValidateCSP(first);
            if (sess == null){
                out.write("404 ERROR: Invalid Connection Setup Message");
                out.flush();
                safeClose(in,out);
                socket.close();
                return;
            }
            else {
                System.out.println("CSP from " + socket.getRemoteSocketAddress()
                        + " => type=" + sess.type
                        + " N=" + sess.numProbes
                        + " size=" + sess.msgSizeBytes
                        + " delay=" + sess.serverDelayMs + "ms");
                out.write("200 OK: Ready\n");
                out.flush();
            }

            // MP/CTP loop
            int expectedSeq = 1;
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("t")) {
                    // CTP: close the connection
                    out.write("200 OK: Closing Connection\n");
                    out.flush();
                    break; // exit
                }

                // MP
                if (!line.startsWith("m ")) {
                    sendMeasurementErrorAndClose(out, in);
                    return;
                }

                // split into 3 parts
                String[] parts = line.split(" ", 3);
                if (parts.length != 3) {
                    sendMeasurementErrorAndClose(out, in);
                    return;
                }

                int seq;
                try {
                    seq = Integer.parseInt(parts[1]);
                } catch (NumberFormatException nfe) {
                    sendMeasurementErrorAndClose(out, in);
                    return;
                }
                String payload = parts[2];


                if (seq != expectedSeq || seq < 1 || seq > sess.numProbes) {
                    sendMeasurementErrorAndClose(out, in);
                    return;
                }


                int actualBytes = payload.getBytes(StandardCharsets.UTF_8).length;
                if (actualBytes != sess.msgSizeBytes) {
                    sendMeasurementErrorAndClose(out, in);
                    return;
                }

                // server delay
                if (sess.serverDelayMs > 0) {
                    try { Thread.sleep(sess.serverDelayMs); } catch (InterruptedException ignored) {}
                }

                // echo back
                out.write(line);
                out.write('\n');
                out.flush();

                expectedSeq++;
            }

            safeClose(in, out);
            socket.close();

        }  catch (IOException e) {
            try {
                socket.close();
            } catch (IOException ignore) {}
        }
    }

    private void sendMeasurementErrorAndClose(BufferedWriter out, BufferedReader in) throws IOException {
        out.write("404 ERROR: Invalid Measurement Message\n");
        out.flush();
        safeClose(in, out);
        socket.close();
    }

    private Session parseAndValidateCSP(String first) {
        String[] parts = first.trim().split("\\s+");
        if (parts.length != 5) return null;
        if (!parts[0].equals("s")) return null;

        String type = parts[1];
        if (!(type.equals("rtt") || type.equals("tput"))) return null;

        try {
            int num = Integer.parseInt(parts[2]);      // NUMBER OF PROBES
            int size = Integer.parseInt(parts[3]);     // MESSAGE SIZE in BYTES
            int delay = Integer.parseInt(parts[4]);    // SERVER DELAY in ms

            if (num <= 0) return null;
            if (size <= 0) return null;
            if (delay < 0) return null;

            Session s = new Session();
            s.type = type;
            s.numProbes = num;
            s.msgSizeBytes = size;
            s.serverDelayMs = delay;
            return s;
        } catch (NumberFormatException nfe) {
            return null;
        }
    }
    private void safeClose(BufferedReader in, BufferedWriter out) {
        try {
            if (in != null)
                in.close();
        }catch (IOException ignore){}
        try {
            if (out != null)
                out.close();
        }catch (IOException ignore){}
    }
}



