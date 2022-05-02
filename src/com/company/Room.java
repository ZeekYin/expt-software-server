package com.company;

import java.util.*;

public class Room {

    String name;
    String address;
    String port;
    List<Account> listeners = new ArrayList<Account>();

    public Room(String roomname, String ip, String port) {
        name = roomname;
        address = ip;
        this.port = port;
    }
}
