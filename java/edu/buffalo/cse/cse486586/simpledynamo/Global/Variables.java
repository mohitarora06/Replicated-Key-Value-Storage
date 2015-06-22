package edu.buffalo.cse.cse486586.simpledynamo.Global;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by mohit on 4/13/15.
 */
public class Variables {
    static public String myPort;
    static public String myHash;
    static public final int SERVER_PORT = 10000;
    public static ArrayList<String> REMOTE_PORTS= new ArrayList<String>(){{
        add("11108");
        add("11112");
        add("11116");
        add("11120");
        add("11124");
    }};
    static public String succPort1;
    static public String succPort2;
    static public String predPort1;
    static public String predPort2;
    static public String myPortForHash;
    static public TreeMap<String, String> ports;
    static public ArrayList<String> notAvailablePorts= new ArrayList<String>();
    static public Context myContext;
    static public HashMap<String, String> keyValuePair= new HashMap<String, String>();
    static public boolean waiting= false;
    static public ReentrantLock commonLock= new ReentrantLock();
    static public boolean startedBefore= false;
}
