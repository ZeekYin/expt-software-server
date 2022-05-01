package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;
import com.google.gson.Gson;

public class Account {
    public static int userCount = 0;
    public static String Directory="users.json";
    public static List<Account> users = new ArrayList<Account>();

    int deposit;
    //int uuid;
    String name, pass;

    public static void init(String f) throws Exception {
        File file=new File(f);
        BufferedReader reader=new BufferedReader(new FileReader(file));
        String str=" ";
        while(1==1){
            str=reader.readLine();
            if(str==null)
                break;
            if(str.equals("{")||str.equals("}"))
                continue;
            int i=0;
            for(i=0;i<str.length();i++){
                if(str.charAt(i)==':')
                    break;
            }
            Gson gson=new Gson();
            int indent=0;
            if(str.charAt(str.length()-1)==','){
                indent=1;
            }
            System.out.println(str.substring(i+1,str.length()-indent));
            Account u=gson.fromJson(str.substring(i+1,str.length()-indent),Account.class);
            users.add(u);
            userCount++;
        }
    }

    public static void write(int uid, Account u,String f, Boolean isAppend, Boolean isLast) throws Exception {
        FileWriter writer=new FileWriter(f,isAppend);
        Gson gson = new Gson();
        String ojson=gson.toJson(u);
        if(!isAppend){
            writer.append("{\n");
        }
        writer.append("\""+uid+"\":");
        writer.append(ojson);
        if(!isLast){
            writer.append(",");
        }
        writer.append("\n");
        if(isLast){
            writer.append("}");
        }
        writer.close();
    }

    public Account(String username, String passwd) {
        userCount++;
        //this.uuid = userCount;
        this.deposit = 100;
        this.name = username;
        this.pass = passwd;
    }

    private static int CheckAccountExist(String username) {
        int cnt=0;
        for (Account i : users) {
            cnt++;
            if (i.name.equals(username)) {
                return cnt;
            }
        }
        //users.stream().anyMatch(i -> i.name.equals(username));
        return -1;
    }

    private static void update() throws Exception {
        int i=0;
        for(Account u : users){
            i++;
            if(i==users.size()){
                write(i,u,Directory,true,true);
            }
            else if(i==1){
                write(i,u,Directory,false,false);
            }
            else{
                write(i,u,Directory,true,false);;
            }
        }
    }
    public static int reg(String username, String passwd) throws Exception {
        if (CheckAccountExist(username) == -1) {
            Account newUser = new Account(username, passwd);
            users.add(newUser);
            update();
            return userCount;//uuid is current userCount
        } else {
            throw new AccountExistException();//account is already exists
        }
    }

    public static int logIn(String username, String passwd) {
        Account user = null;
        int cnt=0;
        for (Account i : users) {
            cnt++;
            if (i.name.equals(username)) {
                user = i;
                break;
            }
        }
        if (user == null) {
            return -1;//this account does not exist
        } else if (!user.pass.equals(passwd)) {
            return -2;//passwd is incorrect
        } else return cnt;
    }

    public static int reduceBalace(int uid, int amount) throws Exception {
        if (uid>userCount){
            throw new IndexOutOfBoundsException();
        }
        Account u= users.get(uid-1);
        return u.reduce(amount);
    }

    private int reduce(int amount) throws Exception {
        if(this.deposit>=amount){
            this.deposit-=amount;
            update();
        return this.deposit;}
        else return -1;// balance is not sufficient
    }


}
