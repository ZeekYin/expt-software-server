package com.company;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public int PORT = 8080;
    List<Room> rooms = new ArrayList<Room>();
    List<Socket> clients = new ArrayList<Socket>();

    public static void main(String[] args) {

        Server server = new Server();
        try {
            ServerSocket s = new ServerSocket(server.PORT);

            new Thread(() -> {
                while (true) {
                    Socket socket;
                    try {
                        socket = s.accept();
                        server.clients.add(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).run();

            while (true) {
                for (Socket socket : server.clients) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()));
                    PrintWriter out = new PrintWriter(
                            new BufferedWriter(
                                    new OutputStreamWriter(
                                            socket.getOutputStream())),
                            true);
                    String request = in.readLine();
                    out.println(server.handleRequest(request, socket));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Server() {

        try {
            Account.init("users.json");

        } catch (Exception e) {
            System.out.println("initialization failed\n");
        }
    }

    public int makeNewRoom(String name, String IP, String port) {
        Room room = new Room(name, IP, port);
        this.rooms.add(room);
        return rooms.size();// this is roomID
    }

    public String getRoomList() {
        String ls = "";
        ls = ls + "{";
        int cnt = 0;
        for (Room r : rooms) {
            ls = ls + "\"";
            ls += cnt++;
            ls = ls + "\":";
            ls = ls + "\"" + r.name + "\"";
            if (cnt != rooms.size())
                ls += ",";
        }
        ls += "}";
        return ls;
    }// {"1":"vsinger","2":"vtuber"}

    public String getRoomIP(int index) {
        Room r = rooms.get(index);
        return r.address;
    }

    public String getRoomPort(int index) {
        Room r = rooms.get(index);
        return r.port;
    }

    public void stopStreamming(int index) {
        Room r = rooms.get(index);
        rooms.remove(r);
        return;
    }

    public String handleRequest(String request, Socket socket) {
        if (request.matches("#registration#")) {
            String ujson = request.substring("#registration#{\"".length(), request.length() - 3);
            String[] info = ujson.split("\":\"");
            int uid = -1;
            try {
                uid = Account.reg(info[0], info[1]);
            } catch (Exception e) {
                return "#failed#";
            }
            return "{\"" + uid + "\"}";
        }
        if (request.matches("#login#")) {
            String ujson = request.substring("#login#{\"".length(), request.length() - 3);
            String[] info = ujson.split("\":\"");
            int uid = Account.logIn(info[0], info[1]);
            if (uid == -1) {
                return "#notexist#";
            } else if (uid == -2) {
                return "#wrongpasswd#";
            } else {
                return "{\"" + uid + "\"}";
            }
        }
        if (request.matches("#getroooms#")) {
            return this.getRoomList();
        }
        if (request.matches("#getroomip#")) {
            // todo:
            // add this listener to this room

            //
            String ujson = request.substring("#getroomip#{\"".length(), request.length() - 3);
            int index = Integer.parseInt(ujson);
            return "{\"" + this.getRoomIP(index) + "\":\"" + this.getRoomPort(index) + "\"";
        }
        if (request.matches("#comment#")) {
            String ujson = request.substring("#comment#{\"".length(), request.length() - 3);
            String info[] = ujson.split("\",\"");
            int user = Integer.parseInt(info[0]);
            int roomid = Integer.parseInt(info[1]);
            // todo:
            // send this comment to all listeners in
            // this room
            return "#success#";
        }
        if (request.matches("#tip#")) {
            String ujson = request.substring("#tip#{\"".length(), request.length() - 3);
            String info[] = ujson.split("\",\"");
            int user = Integer.parseInt(info[0]);
            int roomid = Integer.parseInt(info[1]);
            int amount = Integer.parseInt(info[2]);
            try {

                int balance = Account.reduceBalace(user, amount);
                if (balance >= 0) {
                    // todo:
                    // send this tip action to all listeners in
                    // this room
                    return "{\"" + balance + "\"}";
                } else {
                    return "#notenough#";
                }
            } catch (Exception e) {
                return "#IllegalUID#";
            }

        }
        if (request.matches("#quitroom#")) {
            // todo:
            // remove this listener from this room

            //
            String ujson = request.substring("#quitroom#{\"".length(), request.length() - 3);
            int index = Integer.parseInt(ujson);
            return "#bye#";
        }
        if (request.matches("#startstreamming#")) {
            String ujson = request.substring("#startstreamming#{\"".length(), request.length() - 3);
            String[] info = ujson.split("\":\"");
            String ip = socket.getRemoteSocketAddress().toString();
            int uid = this.makeNewRoom(info[0], ip, info[1]);
        }
        if (request.matches("#stop#")) {

        }
    }

}
