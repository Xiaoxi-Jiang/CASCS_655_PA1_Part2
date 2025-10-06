import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    public static void main(String[] args) throws Exception{
//        Scanner sc = new Scanner(System.in);
        String host;
        int port;
        String type;        // rtt or tput
        String num;        // e.g., 20
        String size;        // bytes (e.g., 100, 8192)
        String delay;
        if (args.length < 6) {
            if (args.length < 2) {
                System.out.println("Usage: java Client <server_host> <server_port> <type> <num> <size> <delay>");
                return;
            } else {
                host = args[0];
                port = Integer.parseInt(args[1]);
            }
            try {type = args[2];}catch (Exception ignored){type = null;}
            try {num  = args[3];}catch (Exception ignored){num = null;}
            try {size = args[4];}catch (Exception ignored){size = null;}
            try {delay= args[5];}catch (Exception ignored){delay = "0";}

        }else{
            host = args[0];
            port = Integer.parseInt(args[1]);
            type = args[2];        // rtt or tput
            num  = args[3];        // e.g., 20
            size = args[4];        // bytes (e.g., 100, 8192)
            delay= args[5];        // ms (e.g., 0,10,50)
        }


        Socket socket = new Socket(host, port);
//        new ClientReaderThread(socket).start();

        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        System.out.println("Server connected.");

        String csp = "s " + type + " " + num + " " + size + " " + delay;
        out.write(csp);
        out.write('\n');
        out.flush();
        System.out.println("[CSP SENT] " + csp);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        String resp = in.readLine(); // expecting "200 OK: Ready" or "404 ERROR: ..."
        System.out.println("[CSP RESP] " + resp);

        if (resp == null || !resp.startsWith("200 OK")){
            out.close();
            socket.close();
            return;
        }
        String payload = genPayload(size);
        long[] rttNs = new long[Integer.parseInt(num)];
        for (int i = 1; i <= Integer.parseInt(num); i++) {
            String msg = "m " + i + " " + payload;
            long start = System.nanoTime();
            out.write(msg);
            out.write('\n');
            out.flush();
            String mResp = in.readLine(); // expect for "200 OK: Received <seq>"
            long end = System.nanoTime();
            if (mResp == null || !mResp.equals(msg)) {
                System.err.println("Probe " + i + " invalid echo or connection closed.");
                return;
            }
            rttNs[i-1] = end - start;
        }
        double avgRttMs = avg(rttNs) / 1_000_000.0;
        System.out.printf("[RESULT] avg RTT over %d probes (size=%dB): %.3f ms%n", Integer.parseInt(num), Integer.parseInt(size), avgRttMs);

        if ("tput".equals(type)) {
            // per-probe throughput = payload_bits / rtt_seconds；取平均
            double[] tputsMbps = new double[Integer.parseInt(num)];
            for (int i = 0; i < Integer.parseInt(num); i++) {
                double rttSec = rttNs[i] / 1_000_000_000.0;
                tputsMbps[i] = (Integer.parseInt(size) * 8.0) / rttSec / 1_000_000.0;
            }
            double avgTput = avg(tputsMbps);
            System.out.printf("[RESULT] avg Throughput over %d probes (size=%dB): %.3f Mbps%n", Integer.parseInt(num), Integer.parseInt(size), avgTput);
        }
        out.write("t\n"); out.flush();
        String closeResp = in.readLine();
        System.out.println("[CTP RESP] " + closeResp);
    }

    private static double avg(long[] ns) {
        long sum = 0;
        for (long x : ns) sum += x;
        return sum * 1.0 / ns.length;
    }
    private static double avg(double[] xs) {
        double s = 0;
        for (double x : xs) s += x;
        return s / xs.length;
    }

    private static String genPayload(String size) {
        int sz = Integer.parseInt(size);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sz; i++) {
            sb.append('a');
        }
        return sb.toString();
    }
}
