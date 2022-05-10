package com.company;

import java.net.Socket;
import java.util.*;

public class Room{

    String name;
    String address;
    String port;
    int liver;
    List<Client> listeners = new ArrayList<Client>();

    public Room(int uid, String roomname, String ip, String port) {
        name = roomname;
        address = ip;
        this.port = port;
        liver = uid;
    }
    public void checkConnection(){
        for(Client client : listeners){
            if (client.socket.getRemoteSocketAddress() == null)
                listeners.remove(client);
        }
    }

}
