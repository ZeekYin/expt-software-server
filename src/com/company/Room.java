package com.company;

import java.net.Socket;
import java.util.*;

public class Room {

    String name;
    String address;
    String port;
    int liver;
    List<Socket> listeners = new ArrayList<Socket>();

    public Room(int uid, String roomname, String ip, String port) {
        name = roomname;
        address = ip;
        this.port = port;
        liver = uid;
    }
}
