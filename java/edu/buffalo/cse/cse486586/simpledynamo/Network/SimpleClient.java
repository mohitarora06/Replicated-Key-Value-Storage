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

import edu.buffalo.cse.cse486586.simpledynamo.Global.Message;
import edu.buffalo.cse.cse486586.simpledynamo.Global.Methods;
import edu.buffalo.cse.cse486586.simpledynamo.Global.Variables;

/**
 * Created by mohit on 4/19/15.
 */
public class SimpleClient {

    public Message doInBackground(Message m){
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        Message message= new Message();
    try {
        switch (m.getMessageType()) {
            case("InsertThisPair"):
                if(m.getPortsTosend().size()!=0){
                    String portHalf= m.getPortsTosend().get(0);
                    m.getPortsTosend().remove(0);
                    String port= Integer.toString((Integer.parseInt(portHalf) * 2));
                    message= connect(4000, m, port);
                }
            break;
            case("QueryForU"):
                if(m.getPortsTosend().size()!=0){
                    String portHalf= m.getPortsTosend().get(0);
                    m.getPortsTosend().remove(0);
                    String port= Integer.toString((Integer.parseInt(portHalf) * 2));
                    message= connect(2000, m, port);
                }
                break;
            case("ReplicateThis"):
            case("ReplicateThis1"):
                if(m.getPortsTosend().size()!=0){
                    String portHalf= m.getPortsTosend().get(0);
                    m.getPortsTosend().remove(0);
                    String port= Integer.toString((Integer.parseInt(portHalf) * 2));
                    message= connect(1500, m, port);
                }
            break;
            case("QueryToSucc2"):
                String portHalf= m.getPortsTosend().get(m.getPortsTosend().size()-1);
                //m.getPortsTosend().remove(0);
                String port= Integer.toString((Integer.parseInt(portHalf) * 2));
                message= connect(0, m,port);
            break;
            /*case ("Ack2"):
                Log.e("Sending back ack from succ2", messages[0].getAckSocketSucc2().toString());
                connect(messages[0].getAckSocketSucc2(),messages[0], null);
            break;
            case ("Ack1"):
                Log.e("Sending back ack from succ 1", messages[0].getAckSocketSucc1().toString());
                connect(messages[0].getAckScketSucc1(),messages[0], null);
            break;
            case ("AckMain"):
                Log.e("Sending back ack from coordinator", messages[0].getAckSocketMain().toString());
                connect(messages[0].getAckSocketMain(),messages[0], null);
            break;
            case("QueryToSucc2"):
                Log.e("Sending back query from succ 2", messages[0].getAckSocketSucc2().toString());
                connect(messages[0].getAckSocketSucc2(), messages[0], null);
            break;
            case("Result"):
                Log.e("Sending back query finally", messages[0].getAckSocketMain().toString());
                connect(messages[0].getAckSocketMain(), messages[0],null);
            break;*/
            case("GlobalQuery"):
                message= connect(0, m, Integer.toString(Integer.parseInt(Variables.succPort1)*2));

        }
    }catch(Exception e){
        e.printStackTrace();
    }

        return message;
    }

    public Message connect(int time, Message m, String port){
        final String TAG = SimpleClient.class.getSimpleName();
        String failed="";
        Message message= new Message();
        try{
            /*if(socket!= null){
                Log.e("Socket is not null", socket.getOutputStream().toString());
                ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());
                outToServer.writeObject(m);
                outToServer.close();
            }
            else{*/
                failed= port;
                Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(failed));
                socket1.setSoTimeout(time);
                Log.e(TAG, "Sending message object"+" "+m.getMessageType()+" "+m.getKey()+"  "+port);
                //Log.e("Socket from client", socket1.toString());
                ObjectOutputStream outToServer = new ObjectOutputStream(socket1.getOutputStream());
                outToServer.writeObject(m);
                Log.v(TAG, "I am waiting here");

                InputStream inputStream= socket1.getInputStream();
                ObjectInputStream inFromServer = new ObjectInputStream(inputStream);
                //Log.v(TAG,"I have revived back"+" "+ inFromServer.toString());
                message= (Message) inFromServer.readObject();
                Log.v(TAG, "After message"+" "+message.getSender()+"  "+message.getKey()+"   "+ message.getKeyValuePairs().toString());
                if(message.getMessageType()!= null){
                    if(message.getMessageType().equals("Result")){
                        if(message.getKeyValuePairs().size()!=0){
                            Log.v(TAG, "Setting the values in the hashmap");
                            Variables.keyValuePair= message.getKeyValuePairs();
                        }
                        Variables.waiting=false;
                    }
                }

                inFromServer.close();
                outToServer.close();
                socket1.close();
            //}


        }
        catch (SocketTimeoutException e){
            Log.d("Timeout exception", m.getMessageType());
            Log.d("Port for Timeout exception", m.getMyPort());
            doInBackground(m);
        }
        catch(IOException e) {
            Log.d(TAG, "SimpleClient socket IOException");
            doInBackground(m);

        }

        catch(ClassNotFoundException CE){
            CE.printStackTrace();
        }
        return message;
    }

}
