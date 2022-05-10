package com.company;

import java.io.*;
import java.net.*;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public int PORT = 8080;
    List<Room> rooms = new ArrayList<Room>();
    List<Client> clients = new ArrayList<Client>();
    Map<String, Integer> findUid = new HashMap<String, Integer>();
    ExecutorService executor = Executors.newCachedThreadPool();

    public static void main(String[] args) {

        Server server = new Server();
        try {
            ServerSocket s = new ServerSocket(server.PORT);
            server.executor.submit(() -> {
                for (Client client : server.clients) {
                    if (client.socket.getRemoteSocketAddress() == null)
                        server.clients.remove(client);
                }
            });
            while (true) {
                Socket socket;
                try {
                    socket = s.accept();
                    socket.setOption(StandardSocketOptions.TCP_NODELAY, true);
                    socket.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
                    Client client = new Client(socket);
                    server.clients.add(client);
                    System.out.println(socket + " is connected");
                    server.executor.submit(() -> {
                        while (true) {
                            try {
                                String request = client.in.readLine();
                                System.out.println(request + "is received");
                                client.out.println(server.handleRequest(request, client));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
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

    public int FindUid(Client client) {
        int uid = -1;
        String str = client.socket.getRemoteSocketAddress().toString();
        uid = findUid.get(str);
        return uid;
    }

    public void userLogin(Client client, int uid) {
        String str = client.socket.getRemoteSocketAddress().toString();
        findUid.put(str, uid);
        return;
    }

    public int makeNewRoom(int uid, String name, String IP, String port) {
        Room room = new Room(uid, name, IP, port);
        this.rooms.add(room);
        executor.submit(()->{

        });
        executor.submit(()->{
            while(rooms.contains(room)){
                room.checkConnection();
            }
        });

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

    public String stopStreamming(int index) {
        Room r = rooms.get(index);
        rooms.remove(r);
        new Thread(() -> {
            broadcast(0, index, "#LiveIsStopped#{\"" + index + "\"}");
        }).start();
        return "#bye#";
    }

    public String broadcast(int from_uid, int roomID, String message) {
        Room r = rooms.get(roomID);
        for (Client client : r.listeners) {
            try {
                var out = client.out;
                out.println(message);
            } catch (Exception e) {
                System.out.println("Sending message to user" + FindUid(client)
                        + " " + client.socket.getInetAddress() + " in room" + roomID + "failed\n");
            }
        }
        return "succeed";
    }

    public String handleRequest(String request, Client client) {
        if (request.matches("#registration#(.*)")) {
            String ujson = request.substring("#registration#{\"".length(), request.length() - 2);
            String[] info = ujson.split("\":\"");
            int uid = -1;
            try {
                uid = Account.reg(info[0], info[1]);
            } catch (Exception e) {
                return "#failed#";
            }
            return "{\"" + uid + "\"}";
        }
        if (request.matches("#login#(.*)")) {
            String ujson = request.substring("#login#{\"".length(), request.length() - 2);
            String[] info = ujson.split("\":\"");
            System.out.println(ujson);
            int uid = Account.logIn(info[0], info[1]);
            if (uid == -1) {
                return "#notexist#";
            } else if (uid == -2) {
                return "#wrongpasswd#";
            } else {
                userLogin(client, uid);
                return "{\"" + uid + "\"}";
            }
        }
        if (request.matches("#getrooms#(.*)")) {
            return this.getRoomList();
        }
        if (request.matches("#getroomip#(.*)")) {
            String ujson = request.substring("#getroomip#{\"".length(), request.length() - 2);
            int index = Integer.parseInt(ujson);
            Room r = rooms.get(index);
            r.listeners.add(client);
            return "{\"" + this.getRoomIP(index) + "\":\"" + this.getRoomPort(index) + "\"}";
        }
        if (request.matches("#comment#(.*)")) {
            String ujson = request.substring("#comment#{\"".length(), request.length() - 2);
            String info[] = ujson.split("\",\"");
            int user = Integer.parseInt(info[0]);
            int roomID = Integer.parseInt(info[1]);
            String message = info[2];
            return broadcast(user, roomID, "#comment#{\"" + user + "\",\"" + roomID + "\",\"" + message + "\"}");
        }
        if (request.matches("#tip#(.*)")) {
            String ujson = request.substring("#tip#{\"".length(), request.length() - 2);
            String info[] = ujson.split("\",\"");
            int user = Integer.parseInt(info[0]);
            int roomID = Integer.parseInt(info[1]);
            int amount = Integer.parseInt(info[2]);
            Room r = rooms.get(roomID);
            try {
                int balance = Account.reduceBalace(user, amount);
                if (balance >= 0) {
                    Account.reduceBalace(r.liver, 0 - amount);
                    broadcast(user, roomID, "#tip#{\"" + user + "\"," + roomID + "\",\"" + amount + "\"}");
                    return "{\"" + balance + "\"}";
                } else {
                    return "#notenough#";
                }
            } catch (Exception e) {
                return "#IllegalUID#";
            }

        }
        if (request.matches("#quitroom#(.*)")) {
            String ujson = request.substring("#quitroom#{\"".length(), request.length() - 2);
            int index = Integer.parseInt(ujson);
            Room r = rooms.get(index);
            r.listeners.remove(client);
            return "#bye#";
        }
        if (request.matches("#startstreamming#(.*)")) {
            String ujson = request.substring("#startstreamming#{\"".length(), request.length() - 2);
            String[] info = ujson.split("\":\"");
            String ip = client.socket.getRemoteSocketAddress().toString();
            int roomID = this.makeNewRoom(FindUid(client), info[0], ip, info[1]);
            return "{\"" + roomID + "\"}";
        }
        if (request.matches("#stop#(.*)")) {
            String ujson = request.substring("#stop#{\"".length(), request.length() - 2);
            int index = Integer.parseInt(ujson);
            stopStreamming(index);
            return "#bye#";
        }
        return "#IllegalRequest#";
    }

}
