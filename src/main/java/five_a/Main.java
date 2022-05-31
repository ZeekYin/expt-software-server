package five_a;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        int PORT = 8080;
        InetAddress addr = InetAddress.getByName("localhost"); // IP アドレスへの変換
        System.out.println("addr = " + addr);
        Socket socket = new Socket(addr, 8080); // ソケットの生成
        try {
            System.out.println("socket = " + socket);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream())); // データ受信用バッファの設
            PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream())),
                    true); // 送信バッファ設定
            while (true) {
                String request = scanner.nextLine();
                System.out.println("send:" + request);
                out.println(request);
                String response = in.readLine();
                System.out.println(response);
            }
        } catch (Exception e) {
            return;
        } finally {
            System.out.println("closing...");
            socket.close();
        }
    }
}
/*
 * serverSocket.bind(new InetSocketAddress(8888));
 * while(true){
 * var client = serverSocket.accept();
 * new Thread(()->{
 * while (true){
 * client.getInputStream()
 * }
 * }).run();
 * }
 */