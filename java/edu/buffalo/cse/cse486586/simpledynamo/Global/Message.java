package edu.buffalo.cse.cse486586.simpledynamo.Global;

import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by mohit on 4/13/15.
 */
public class Message implements Serializable {
    String messageType;
    TreeMap<String, String> ports= new TreeMap<String, String>();
    String myPort;
    String key;
    String value;
    ArrayList<String> portsTosend;
    String mainPort;
    String sender;
    HashMap<String, String> keyValuePairs= new HashMap<String, String>();

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public HashMap<String, String> getKeyValuePairs() {
        return keyValuePairs;
    }

    public void setKeyValuePairs(HashMap<String, String> keyValuePairs) {
        this.keyValuePairs = keyValuePairs;
    }

    public String getMainPort() {
        return mainPort;
    }

    public void setMainPort(String mainPort) {
        this.mainPort = mainPort;
    }

    public ArrayList<String> getPortsTosend() {
        return portsTosend;
    }

    public void setPortsTosend(ArrayList<String> portsTosend) {
        this.portsTosend = portsTosend;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMyPort() {
        return myPort;
    }

    public void setMyPort(String myPort) {
        this.myPort = myPort;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public TreeMap<String, String> getPorts() {
        return ports;
    }

    public void setPorts(TreeMap<String, String> ports) {
        this.ports = ports;
    }
}
