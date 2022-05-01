package com.company;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        Server s = new Server();
        //s.makeNewRoom("东雪莲", "10.0.0.1");
        //s.makeNewRoom("罕见见", "10.0.0.2");
        System.out.println(s.getRoomList());
    }
}
/*
serverSocket.bind(new InetSocketAddress(8888));
while(true){
                var client = serverSocket.accept();
                new Thread(()->{
                    while (true){
                        client.getInputStream()
                    }
                }).run();
            }
* */