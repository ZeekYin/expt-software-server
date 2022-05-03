package com.company;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    public int PORT = 8080;
    List<Room> rooms = new ArrayList<Room>();
    List<Socket> clients = new ArrayList<Socket>();
    Map<String, Integer> findUid = new HashMap<String, Integer>();

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

    public int FindUid(Socket socket) {
        int uid = -1;
        String str = socket.getRemoteSocketAddress().toString();
        uid = findUid.get(str);
        return uid;
    }

    public void userLogin(Socket socket, int uid) {
        String str = socket.getRemoteSocketAddress().toString();
        findUid.put(str, uid);
        return;
    }

    public int makeNewRoom(int uid, String name, String IP, String port) {
        Room room = new Room(uid, name, IP, port);
        this.rooms.add(room);
        return rooms.size() - 1;// this is roomID
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
        broadcast(0, index, "#LiveIsStopped#{\"" + index + "\"}");
        return;
    }

    public String broadcast(int from_uid, int roomID, String message) {
        Room r = rooms.get(roomID);
        for (Socket socket : r.listeners) {
            try {
                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        socket.getOutputStream())),
                        true);
                out.println(message);
            } catch (Exception e) {
                System.out.println("Sending message to user" + FindUid(socket)
                        + " " + socket.getInetAddress() + " in room" + roomID + "failed\n");
            }
        }
        return "succeed";
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
            int uid = Account.logIn(info[0], info[1], socket);
            if (uid == -1) {
                return "#notexist#";
            } else if (uid == -2) {
                return "#wrongpasswd#";
            } else {
                userLogin(socket, uid);
                return "{\"" + uid + "\"}";
            }
        }
        if (request.matches("#getroooms#")) {
            return this.getRoomList();
        }
        if (request.matches("#getroomip#")) {
            String ujson = request.substring("#getroomip#{\"".length(), request.length() - 3);
            int index = Integer.parseInt(ujson);
            Room r = rooms.get(index);
            r.listeners.add(socket);
            return "{\"" + this.getRoomIP(index) + "\":\"" + this.getRoomPort(index) + "\"";
        }
        if (request.matches("#comment#")) {
            String ujson = request.substring("#comment#{\"".length(), request.length() - 3);
            String info[] = ujson.split("\",\"");
            int user = Integer.parseInt(info[0]);
            int roomID = Integer.parseInt(info[1]);
            String message = info[2];
            return broadcast(user, roomID, "#comment#{\"" + user + "\",\"" + roomID + "\",\"" + message + "\"}");
        }
        if (request.matches("#tip#")) {
            String ujson = request.substring("#tip#{\"".length(), request.length() - 3);
            String info[] = ujson.split("\",\"");
            int user = Integer.parseInt(info[0]);
            int roomID = Integer.parseInt(info[1]);
            int amount = Integer.parseInt(info[2]);
            try {

                int balance = Account.reduceBalace(user, amount);
                if (balance >= 0) {
                    broadcast(user, roomID, "#tip#{\"" + user + "\"," + roomID + "\",\"" + amount + "\"}");
                    return "{\"" + balance + "\"}";
                } else {
                    return "#notenough#";
                }
            } catch (Exception e) {
                return "#IllegalUID#";
            }

        }
        if (request.matches("#quitroom#")) {
            String ujson = request.substring("#quitroom#{\"".length(), request.length() - 3);
            int index = Integer.parseInt(ujson);
            Room r = rooms.get(index);
            r.listeners.remove(socket);
            return "#bye#";
        }
        if (request.matches("#startstreamming#")) {
            String ujson = request.substring("#startstreamming#{\"".length(), request.length() - 3);
            String[] info = ujson.split("\":\"");
            String ip = socket.getRemoteSocketAddress().toString();
            int roomID = this.makeNewRoom(FindUid(socket), info[0], ip, info[1]);
            return "{\"" + roomID + "\"}";
        }
        if (request.matches("#stop#")) {
            String ujson = request.substring("#stop#{\"".length(), request.length() - 3);
            int index = Integer.parseInt(ujson);
            stopStreamming(index);
            return "#bye#";
        }
        return "#IllegalRequest#";
    }

}
