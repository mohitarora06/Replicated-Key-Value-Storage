package edu.buffalo.cse.cse486586.simpledynamo.Network;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.buffalo.cse.cse486586.simpledynamo.Global.Message;
import edu.buffalo.cse.cse486586.simpledynamo.Global.Methods;
import edu.buffalo.cse.cse486586.simpledynamo.Global.Variables;

/**
 * Created by mohit on 4/13/15.
 */
public class ServerTask extends AsyncTask<ServerSocket, Message, Void> {
   // SimpleClient client= new SimpleClient();
    final String TAG = ServerTask.class.getSimpleName();
    @Override
    protected Void doInBackground(ServerSocket... sockets) {
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        ServerSocket serverSocket = sockets[0];

            while(true) {
                try {
                Socket socketS= serverSocket.accept();
                ObjectInputStream inFromClient = new ObjectInputStream(socketS.getInputStream());
                Message m= (Message) inFromClient.readObject();
                Message mToSendBack= new Message();
                Log.e("Came here in server task", m.getMessageType()+"  "+m.getKey());
                switch (m.getMessageType()){
                    case("Start"):
                    break;
                    case("NewPorts"):
                        Log.e(TAG, "Came here for ports");
                        Methods.setSuccPredPort(m.getPorts());
                        /*m.setMessageType("Got the ports");
                        ObjectOutputStream outToServerp0 = new ObjectOutputStream(socketS.getOutputStream());
                        outToServerp0.writeObject(m);
                        outToServerp0.close();*/
                    break;
                    case("InsertThisPair"):
                    case("ReplicateThis"):
                    case("ReplicateThis1"):
                        if(m.getMainPort().equals(Variables.myPortForHash)){
                            Log.e(TAG, "Came here for insertion");
                            Methods.insertIn(m.getKey(), m.getValue(), "insert");
                            m.setMessageType("ReplicateThis");
                            SimpleClient client = new SimpleClient();
                            mToSendBack= client.doInBackground(m);
                            try {
                                ObjectOutputStream outToServerp = new ObjectOutputStream(socketS.getOutputStream());
                                outToServerp.writeObject(mToSendBack);
                                outToServerp.close();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else if(m.getMainPort().equals(Variables.predPort1)) {
                            Log.e(TAG, "Came here for replication 1 in server" + " " + Variables.predPort1 + "  " + m.getMyPort());
                            Methods.insertIn(m.getKey(), m.getValue(), "replicate1");
                            m.setMessageType("ReplicateThis1");
                            SimpleClient client1 = new SimpleClient();
                            mToSendBack = client1.doInBackground(m);
                            try {
                                ObjectOutputStream outToServerp1 = new ObjectOutputStream(socketS.getOutputStream());
                                outToServerp1.writeObject(mToSendBack);
                                outToServerp1.close();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else if(m.getMainPort().equals(Variables.predPort2)){
                            Log.e(TAG, "Came here for replication 2 in server"+" "+Variables.predPort2+"  "+m.getMyPort());
                            Methods.insertIn(m.getKey(),m.getValue(),"replicate2");
                            m.setSender("success");
                            try{
                                ObjectOutputStream outToServerp2 = new ObjectOutputStream(socketS.getOutputStream());
                                outToServerp2.writeObject(m);
                                outToServerp2.close();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                    break;
                    case("QueryForU"):
                        Log.e(TAG, "Came here for query Sending it to second succ" +" "+m.getKey());
                        m.setKeyValuePairs(Methods.query(m.getKey(), m));
                        m.setMessageType("Result");
                        //mToSendBack= client.doInBackground(m);
                        try {
                            ObjectOutputStream outToServerpQ = new ObjectOutputStream(socketS.getOutputStream());
                            outToServerpQ.writeObject(m);
                            outToServerpQ.close();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        //new SimpleClient().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, m, null);
                    break;
                    case("QueryToSucc2"):
                        Log.v(TAG, "Came here finally for query");
                        m.setKeyValuePairs(Methods.query(m.getKey(), m));
                        m.setMessageType("Result");
                        ObjectOutputStream outToServerp2 = new ObjectOutputStream(socketS.getOutputStream());
                        outToServerp2.writeObject(m);
                        outToServerp2.close();
                    break;
                    case("Result"):
                        if(m.getKeyValuePairs().size()!=0){
                            Variables.keyValuePair= m.getKeyValuePairs();
                        }
                        Variables.waiting=false;
                    break;
                    case("GlobalQuery"):
                        if(m.getMyPort().equals(Variables.myPortForHash)){
                            Log.v(TAG, "FINALLY REACHED BACK");
                            Variables.keyValuePair= m.getKeyValuePairs();
                            Variables.waiting= false;
                        }
                        else{
                            Log.v(TAG, Variables.myPortForHash);
                            Methods.globalQuery(m.getKey(), m);
                        }
                    break;
                    case("Recovery"):
                        if(m.getMainPort().equals(Variables.succPort1)){
                            m.setKeyValuePairs(Methods.recoveryMethod("pred1"));
                        }
                        else if(m.getMainPort().equals(Variables.succPort2)){
                            m.setKeyValuePairs(Methods.recoveryMethod("pred2"));
                        }
                        else if(m.getMainPort().equals(Variables.predPort1)){
                            m.setKeyValuePairs(Methods.recoveryMethod("succ1"));
                        }
                        else if(m.getMainPort().equals(Variables.predPort2)){
                            m.setKeyValuePairs(Methods.recoveryMethod("succ2"));
                        }
                        try {
                            ObjectOutputStream outToServerp3 = new ObjectOutputStream(socketS.getOutputStream());
                            outToServerp3.writeObject(m);
                            outToServerp3.close();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                    break;
                    case("delete"):
                        Methods.delete("anything");
                    break;
                }
            }
                catch (Exception e) {
                    e.printStackTrace();


                }
        }

    }


}
