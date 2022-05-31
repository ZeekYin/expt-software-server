package five_a;

import java.io.*;
import java.net.Socket;

public class Client {
    int uid;
    Socket socket;
    PrintWriter out;
    BufferedReader in;

    public Client(Socket _socket) {
        this.socket = _socket;
        try {
            in = new BufferedReader(
                    new InputStreamReader(
                            this.socket.getInputStream()));
            out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    this.socket.getOutputStream())),
                    true);
        } catch (IOException e) {
            System.out.println("Creating socket" + socket + "failed");
        }
    }
}
