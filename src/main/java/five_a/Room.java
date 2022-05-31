package five_a;

import java.util.*;

public class Room {

    String name;
    String port;
    int streamerID;
    Client streamer;
    List<Client> listeners = new ArrayList<Client>();

    public Room(int uid, Client client, String roomname, String port) {
        listeners.add(client);
        name = roomname;
        this.port = port;
        streamer = client;
        streamerID = uid;
    }

    public void checkConnection() {
        for (Client client : listeners) {
            if (client.socket.getRemoteSocketAddress() == null)
                listeners.remove(client);
        }
    }

}
