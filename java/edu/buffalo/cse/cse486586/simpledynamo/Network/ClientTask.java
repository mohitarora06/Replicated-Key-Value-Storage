package edu.buffalo.cse.cse486586.simpledynamo.Network;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;

import edu.buffalo.cse.cse486586.simpledynamo.Global.Message;
import edu.buffalo.cse.cse486586.simpledynamo.Global.Methods;
import edu.buffalo.cse.cse486586.simpledynamo.Global.Variables;

/**
 * Created by mohit on 4/13/15.
 */
public class ClientTask extends AsyncTask<Message,Void, Void> {

    @Override
    protected Void doInBackground(Message ...msgs) {
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        //Log.e("Came here to client task", "");
        switch (msgs[0].getMessageType()){
            case ("Start"):
            case ("NewPorts"):
                Log.e("New ports client", "");
                int remotePortsToSend = Variables.REMOTE_PORTS.size();
                for (int i = 0; i < remotePortsToSend; i++) {
                    connectionMethod(Variables.REMOTE_PORTS.get(i), msgs[0], "NOWAIT");
                }
            break;
            case("Recovery"):
                Variables.commonLock.lock();
                String result;
                result= RecoveryConnection(Integer.toString(Integer.parseInt(Variables.predPort1)*2), msgs[0]);
                Log.v("Came here or recovery", "Done with pred1");
                if(result.equals("fail")){
                    RecoveryConnection(Integer.toString(Integer.parseInt(Variables.predPort2)*2), msgs[0]);
                    Log.v("Came here or recovery", "Done with pred2");
                }
                String result1;
                result1= RecoveryConnection(Integer.toString(Integer.parseInt(Variables.succPort1)*2), msgs[0]);
                if(result1.equals("fail")){
                    RecoveryConnection(Integer.toString(Integer.parseInt(Variables.succPort2)*2), msgs[0]);
                    Log.v("Came here or recovery", "Done with succ");
                }
                Variables.commonLock.unlock();
            break;
            case("ReplicateThis"):
            case("InsertThisPair"):
            case("QueryForU"):
                Log.e("Sending from here to others", msgs[0].getMessageType());
                    String portHalf= msgs[0].getPortsTosend().get(0);
                    msgs[0].getPortsTosend().remove(0);
                    String port= Integer.toString((Integer.parseInt(portHalf) * 2));
                    connectionMethod(port, msgs[0],"WAIT");
            break;
            case("QueryToSucc2"):
                portHalf= msgs[0].getPortsTosend().get(msgs[0].getPortsTosend().size()-1);
                //msgs[0].getPortsTosend().remove(0);
                port= Integer.toString((Integer.parseInt(portHalf) * 2));
                connectionMethod(port, msgs[0],"WAIT");
            break;
            case("GlobalQuery"):
            case("delete"):
                connectionMethod(Integer.toString(Integer.parseInt(Variables.succPort1)*2), msgs[0],"NOWAIT");
            break;
        }
       return null;
    }



    public void connectionMethod(String portToSend, Message m, String type){
         final String TAG = ClientTask.class.getSimpleName();
        String failed="";
        try{
            failed=portToSend;
            if(type.equals("WAIT")){
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(failed));
                socket.setSoTimeout(10000);
                Log.e("Sending message object", m.getMessageType());
                Log.e("Socket from client", socket.toString());
                ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());
                outToServer.writeObject(m);
                //Log.v("I am waiting here", m.getKey());

                InputStream inputStream= socket.getInputStream();
                ObjectInputStream inFromServer = new ObjectInputStream(inputStream);
                Log.v("I have revived back", inFromServer.toString());
                Message ackMessage= (Message) inFromServer.readObject();
                Log.v("After message", ackMessage.getMessageType() +" "+ackMessage.getSender()+"  "+ackMessage.getKey()+" "+ackMessage.getKeyValuePairs().toString() );
                if(ackMessage.getMessageType().equals("Result")){
                    if(ackMessage.getKeyValuePairs().size()!=0){
                        Log.v(TAG, "Setting the values in the hashmap");
                        Variables.keyValuePair= ackMessage.getKeyValuePairs();
                    }
                    Variables.waiting=false;
                }

                inFromServer.close();
                outToServer.close();
                socket.close();
            }
            else{
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(failed));
                //socket.setSoTimeout(5000);
                Log.d("Client Task Sending without wait", m.getMessageType());
               // Log.e("Socket from client", socket.toString());
                ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());
                outToServer.writeObject(m);
                outToServer.close();
                socket.close();
            }

        }
        catch (SocketTimeoutException e){
            Log.d("Timeout exception", m.getMessageType());
           // Log.d("Port for Timeout exception", m.getMyPort());
            if(m.getMessageType().equals("GlobalQuery") || m.getMessageType().equals("delete"))
            connectionMethod(Integer.toString(Integer.parseInt(Variables.succPort2)*2), m,"NOWAIT");
        }
        catch (UnknownHostException e) {
            Log.d(TAG, "ClientTask UnknownHostException");
        } catch (IOException e) {
            Log.d(TAG, "ClientTask socket IOException");
            if(m.getMessageType().equals("GlobalQuery") || m.getMessageType().equals("delete"))
            connectionMethod(Integer.toString(Integer.parseInt(Variables.succPort2)*2), m,"NOWAIT");
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String RecoveryConnection(String portToSend, Message m){
        String result= "";
    try{
        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                Integer.parseInt(portToSend));
        socket.setSoTimeout(2300);
        Log.e("Sending message object", m.getMessageType());
        Log.e("Socket from client", socket.toString());
        ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());
        outToServer.writeObject(m);
        //Log.v("I am waiting here", m.getKey());

        InputStream inputStream= socket.getInputStream();
        ObjectInputStream inFromServer = new ObjectInputStream(inputStream);
        Log.v("I have revived back", inFromServer.toString());
        Message ackMessage= (Message) inFromServer.readObject();
        Log.v("After message", ackMessage.getMessageType() +" "+ackMessage.getSender()+"  "+ackMessage.getKey()+" "+ackMessage.getKeyValuePairs().toString() );
        if(ackMessage.getMessageType().equals("Recovery")) {
            if (ackMessage.getKeyValuePairs().size() != 0) {
                Log.v("Recovery", "Replicating");
                for (Map.Entry<String, String> entry : ackMessage.getKeyValuePairs().entrySet()) {
                    if (entry.getKey().contains("_&_")) {
                        String parts[] = entry.getKey().split("_&_");
                        if (parts[1].equals("pred1")) {
                            Methods.insertIn(parts[0], entry.getValue(), "replicate1");
                        } else if (parts[1].equals("pred2")) {
                            Methods.insertIn(parts[0], entry.getValue(), "replicate2");
                        }
                    } else {
                        Methods.insertIn(entry.getKey(), entry.getValue(), "insert");
                    }
                }

            }
        }
        inFromServer.close();
        outToServer.close();
        socket.close();
    }
    catch(SocketTimeoutException ie){
        Log.v("Recovery Timeout", "");
        result= "fail";
    }
    catch(IOException ie){
        Log.v("Recovery IO", "");
        result= "fail";
    }
    catch (ClassNotFoundException e) {
        e.printStackTrace();
    }

        return result;
    }

}
