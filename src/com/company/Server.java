package com.company;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public int PORT=8080;
    List<Room> rooms = new ArrayList<Room>();
    public static void main(String[] args){
        Server server=new Server();
        try{
            ServerSocket s = new ServerSocket(server.PORT);
            while(true){
                Socket socket= s.accept();
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(
                                        socket.getInputStream()));
                PrintWriter out =
                        new PrintWriter(
                                new BufferedWriter(
                                        new OutputStreamWriter(
                                                socket.getOutputStream())), true);
                String request=in.readLine();
                if(request.matches("#registration#")){
                    String ujson=request.substring("#registration#{\"".length(),request.length()-3);
                    String[] info=ujson.split("\":\"");
                    int uid=-1;
                    try {
                        uid=Account.reg(info[0], info[1]);
                    }catch (Exception e) {
                        out.println("#failed#");
                        socket.close();
                        continue;
                    }finally {
                        out.println("{\""+uid+"\"}");
                        socket.close();
                        continue;
                    }
                }
                if(request.matches("#login#")){
                    String ujson=request.substring("#login#{\"".length(),request.length()-3);
                    String[] info=ujson.split("\":\"");
                    int uid=Account.logIn(info[0], info[1]);
                    if(uid==-1){
                        out.println("#notexist#");
                    }
                    else if(uid==-2){
                        out.println("#wrongpasswd#");
                    }else {
                        out.println("{\"" + uid + "\"}");
                    }
                    socket.close();
                    continue;
                }
                if(request.matches("#getroooms#")){
                    out.println(server.getRoomList());
                    socket.close();
                    continue;
                }
                if (request.matches("#getroomip#")){
                    //todo:
                    //add this listener to this room

                    //
                    String ujson=request.substring("#getroomip#{\"".length(),request.length()-3);
                    int index=Integer.parseInt(ujson);
                    out.println("{\""+server.getRoomIP(index)+"\":\""+server.getRoomPort(index)+"\"");
                    socket.close();
                    continue;
                }
                if (request.matches("#comment#")){

                }
                if (request.matches("#quitroom#")){
                    //todo:
                    //remove this listener from this room

                    //
                    String ujson=request.substring("#quitroom#{\"".length(),request.length()-3);
                    int index=Integer.parseInt(ujson);
                    out.println("#bye#");
                    socket.close();
                    continue;
                }
                if(request.matches("#startstreamming#")){
                    String ujson=request.substring("#startstreamming#{\"".length(),request.length()-3);
                    String[] info=ujson.split("\":\"");
                    String ip=socket.getRemoteSocketAddress().toString();
                    int uid=server.makeNewRoom(info[0],ip,info[1]);
                    socket.close();
                    continue;
                }
            }

        }
        catch(IOException e){
            e.printStackTrace();
        }


    }

    public Server(){

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
    }//{"1":"vsinger","2":"vtuber"}

    public String getRoomIP(int index){
        Room r = rooms.get(index);
        return r.address;
    }
    public String getRoomPort(int index){
        Room r = rooms.get(index);
        return r.port;
    }
    public void stopStreamming(int index){
        Room r = rooms.get(index);
        rooms.remove(r);
        return;
    }


}
