package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Map;
import java.util.TreeMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import edu.buffalo.cse.cse486586.simpledynamo.Global.Message;
import edu.buffalo.cse.cse486586.simpledynamo.Global.Methods;
import edu.buffalo.cse.cse486586.simpledynamo.Global.Variables;
import edu.buffalo.cse.cse486586.simpledynamo.Network.ClientTask;
import edu.buffalo.cse.cse486586.simpledynamo.Network.ServerTask;
import edu.buffalo.cse.cse486586.simpledynamo.Network.SimpleClient;

public class SimpleDynamoProvider extends ContentProvider {
    static final String TAG = SimpleDynamoProvider.class.getSimpleName();
    //SimpleClient client= new SimpleClient();
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
        ArrayList<String> portsToSend= Methods.findLocationOfKey(selection);
        Log.v("Array list of ports", portsToSend.toString());
        Methods.delete(selection);
        Message message= new Message();
        message.setMessageType("delete");
        message.setMainPort(Variables.myPortForHash);
        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, message, null);
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public synchronized Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
        String fileName= values.get("key").toString();
        String value= values.get("value").toString();
        ArrayList<String> portsToSend= Methods.findLocationOfKey(fileName);
        Message m= new Message();
        try{
            if(portsToSend.get(0).equals(Variables.myPortForHash)){
                Methods.insertIn(fileName,value,"insert");
                m.setMessageType("ReplicateThis");
                portsToSend.remove(0);
                m.setMainPort(Variables.myPortForHash);
            }
            else{
                m.setMessageType("InsertThisPair");
                m.setMainPort(portsToSend.get(0));
            }

            m.setMyPort(Variables.myPortForHash);
            m.setKey(fileName);
            m.setValue(value);
            m.setPortsTosend(portsToSend);
            SimpleClient client= new SimpleClient();
            client.doInBackground(m);

        }
        catch (Exception e){
            e.printStackTrace();
        }
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
        try {
            TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
            String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
            Variables.myPort = String.valueOf((Integer.parseInt(portStr) * 2));
            Variables.myHash= Methods.genHash(portStr);
            Variables.myPortForHash= portStr;
            ServerSocket serverSocket = new ServerSocket(Variables.SERVER_PORT);
            Variables.myContext= getContext();
            Methods.setSuccPredPort(Methods.processSuccPredPort());
            Methods.checkReplication();
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
            Message message= new Message();
            if(Variables.startedBefore){
                message.setMessageType("Recovery");
                message.setMainPort(Variables.myPortForHash);
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, message, null);
            }

        } catch (IOException e) {

            Log.e(TAG, "Can't create a ServerSocket");
            return false;
        }catch (NoSuchAlgorithmException i){
            Log.e(TAG, "Hash Failed");
        }
		return false;
	}

	@Override
	public synchronized Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
        try{
            MatrixCursor matrixCursor= new MatrixCursor(new String[] {"key", "value"});
            Message m= new Message();
            Log.v("query", selection);
            if(selection.equals("\"*\"") || selection.equals("\"@\"")){
                if(selection.equals("\"@\"")){
                    Log.v("querying", "@");
                    Methods.globalQuery("@", m);
                }
                else{
                    m.setMessageType("GlobalQuery");
                    m.setMyPort(Variables.myPortForHash);
                    m.setKey("*");
                    Methods.globalQuery("*", m);
                    Variables.waiting= true;
                    while(Variables.waiting){

                    }
                }
            }
            else{
                ArrayList<String> portsToSend= Methods.findLocationOfKey(selection);
                if(portsToSend.get(0).equals(Variables.myPortForHash)){
                    m.setMainPort(Variables.myPortForHash);
                    Variables.keyValuePair= Methods.query(selection, m);
                    portsToSend.remove(0);
                }
                else{
                    m.setMessageType("QueryForU");
                    m.setPortsTosend(portsToSend);
                    m.setMyPort(Variables.myPortForHash);
                    m.setKey(selection);
                    m.setMainPort(portsToSend.get(0));
                    Log.v("Sending for query", m.getPortsTosend().get(0)+" "+m.getKey());
                    SimpleClient client = new SimpleClient();
                    client.doInBackground(m);
                }

                //Variables.waiting= true;
            }
            /*while(Variables.waiting){

            }*/
            //Variables.waiting= true;
            Log.v("hashmap size before", Variables.keyValuePair.size()+"");
            for(Map.Entry<String, String> entry: Variables.keyValuePair.entrySet()){
                Log.v("values in hashmap", entry.getKey()+" "+entry.getValue());
                matrixCursor.addRow(new String[] {entry.getKey(), entry.getValue()});
            }
            Variables.keyValuePair.clear();
            Log.v("hashmap size after", Variables.keyValuePair.size()+"");
            return matrixCursor;
        }
        catch (Exception e){
            e.printStackTrace();
        }
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}




}
