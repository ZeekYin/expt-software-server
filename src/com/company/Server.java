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
            System.out.println(e);
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

    public int makeNewRoom(int uid, Client client,String name, String port) {
        Room room = new Room(uid, client,name, port);
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
            return "{"+"\"uid\":"+"\"" + uid + "\"}";
        }
        if (request.matches("#login#(.*)")) {
            String ujson = request.substring("#login#{\"".length(), request.length() - 2);
            String[] info = ujson.split("\":\"");
            System.out.println(ujson);
            try {
                Account account = Account.logIn(info[0], info[1]);
                final int uid = account.id;
                userLogin(client, uid);
                return "{"+"\"uid\":"+"\"" + uid + "\",\"deposit\":"+"\"" + account.deposit + "\"}";
            } catch (AccountNotExistException e) {
                return "#notexist#";
            } catch (WrongPasswordException e) {
                return "#wrongpasswd#";
            }
        }
        if (request.matches("#getrooms#(.*)")) {
            return this.getRoomList();
        }
        if (request.matches("#listenroom#(.*)")) {
            String ujson = request.substring("#listenroom#{\"".length(), request.length() - 2);
            int index = Integer.parseInt(ujson);
            Room r = rooms.get(index);
            r.listeners.add(client);
            return "#success#";
        }
        if (request.matches("#comment#(.*)")) {
            String ujson = request.substring("#comment#{\"".length(), request.length() - 2);
            String info[] = ujson.split("\",\"");
            int user = Integer.parseInt(info[0]);
            String username = info[1];
            int roomID = Integer.parseInt(info[2]);
            String message = info[3];
            return broadcast(user, roomID, "#comment#{\"username\":\"" + username + "\",\"message\":\"" + message + "\"}");
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
            final int uid = Integer.parseInt(info[0]);
            final String roomname = info[1];
            final String port  = info[2];
            int roomID = this.makeNewRoom(uid, client, roomname, port);
            System.out.println(roomID);
            return "{\"roomID\":\"" + roomID + "\"}";
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