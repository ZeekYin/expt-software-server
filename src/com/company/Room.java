package com.company;

import java.net.Socket;
import java.util.*;

public class Room{

    String name;
    String address;
    String port;
    int liver;
    List<Client> listeners = new ArrayList<Client>();

    public Room(int uid, Client client,String roomname, String ip, String port) {
        listeners.add(client);
        name = roomname;
        address = ip;
        this.port = port;
        liver = uid;
        new Thread(()->{
            while(true){
                this.checkConnection();
            }
        }).run();
    }
    public void checkConnection(){
        for(Client client : listeners){
            if (client.socket.getRemoteSocketAddress() == null)
                listeners.remove(client);
        }
    }

}
