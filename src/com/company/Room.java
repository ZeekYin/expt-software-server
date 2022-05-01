package com.company;

import java.util.LinkedList;
import java.util.Queue;

public class Room {

    String name;
    String address;
    String port;
    //Queue<> comments = new LinkedList<>()
    public Room(String roomname,String ip,String port){
        name=roomname;
        address=ip;
        this.port=port;
    }
}

class comment{
    String comment;
    int uid;
}
