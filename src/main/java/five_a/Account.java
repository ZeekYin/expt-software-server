package five_a;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.*;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class Account {
    public static int userCount = 0;
    public static String Directory = "users.json";
    public static List<Account> users = new ArrayList<Account>();

    // int uuid;
    int deposit, id;
    String name, pass;
    // Socket socket;

    private static int defaultDeposit = 300;

    public static void init(String f) throws Exception {
        JsonReader reader = new JsonReader(new FileReader(f));
        System.out.println(reader);
        Gson gson = new Gson();
        Map<String, Map<String, ?>> usersMap = gson.fromJson(reader, Map.class);
        usersMap.forEach((idStr, userData) -> {
            final int id = Integer.parseInt(idStr);
            final String name = (String) userData.get("name");
            final String pass = (String) userData.get("pass");
            final int deposit = ((Double) userData.get("deposit")).intValue();
            final Account user = new Account(name, pass, deposit, id);
            System.out.println("id: " + id + ", name: " + name + ", pass: " + pass + ", deposit: " + deposit);
            userCount++;
            users.add(user);
        });
    }

    public static void write(int uid, Account u, String f, Boolean isAppend, Boolean isLast) throws Exception {
        FileWriter writer = new FileWriter(f, isAppend);
        Gson gson = new Gson();
        String ojson = gson.toJson(u);
        if (!isAppend) {
            writer.append("{\n");
        }
        writer.append("\"" + uid + "\":");
        writer.append(ojson);
        if (!isLast) {
            writer.append(",");
        }
        writer.append("\n");
        if (isLast) {
            writer.append("}");
        }
        writer.close();
    }

    public Account(String username, String passwd, int deposit, int id) {
        userCount++;
        this.deposit = deposit;
        this.pass = passwd;
        this.name = username;
        this.id = id;
    }

    private static int CheckAccountExist(String username) {
        int cnt = 0;
        for (Account i : users) {
            cnt++;
            if (i.name.equals(username)) {
                return cnt;
            }
        }
        // users.stream().anyMatch(i -> i.name.equals(username));
        return -1;
    }

    private static void update() throws Exception {
        int i = 0;
        for (Account u : users) {
            i++;
            if (i == users.size()) {
                write(i, u, Directory, true, true);
            } else if (i == 1) {
                write(i, u, Directory, false, false);
            } else {
                write(i, u, Directory, true, false);
                ;
            }
        }
    }

    public static Account reg(String username, String passwd) throws Exception {
        if (CheckAccountExist(username) == -1) {
            userCount++;
            Account newUser = new Account(username, passwd, defaultDeposit, userCount);
            users.add(newUser);
            update();
            return newUser;// uuid is current userCount
        } else {
            throw new AccountExistException();// account is already exists
        }
    }

    public static Account logIn(String username, String passwd)
            throws AccountNotExistException, WrongPasswordException {
        Account user = null;
        int cnt = 0;
        for (Account i : users) {
            cnt++;
            if (i.name.equals(username)) {
                user = i;
                // user.socket = socket;
                break;
            }
        }
        if (user == null) {
            throw new AccountNotExistException();
        } else if (!user.pass.equals(passwd)) {
            throw new WrongPasswordException();
        } else
            return user;
    }

    public static int reduceBalace(int uid, int amount) throws Exception {
        if (uid > userCount) {
            throw new IndexOutOfBoundsException();
        }
        Account u = users.get(uid - 1);
        return u.reduce(amount);
    }

    private int reduce(int amount) throws Exception {
        if (this.deposit >= amount) {
            this.deposit -= amount;
            update();
            return this.deposit;
        } else
            return -1;// balance is not sufficient
    }

}
