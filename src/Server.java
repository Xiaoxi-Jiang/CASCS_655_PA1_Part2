import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) throws Exception{
        int port;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        } else {
            System.out.println("Which port will you register?");
            Scanner sc = new Scanner(System.in);
            port = sc.nextInt();
        }

        // check port number
        if (port < 58000 || port > 58999) {
            System.err.println("Port must be in 58000–58999 on csa machines.");
            return;
        }

        System.out.println("Server is listening on port " + port + " ...");
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            Socket socket = serverSocket.accept();
            new ServerThread(socket).start();
        }
    }
}
