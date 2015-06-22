package edu.buffalo.cse.cse486586.simpledynamo.Global;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import edu.buffalo.cse.cse486586.simpledynamo.Network.ClientTask;
import edu.buffalo.cse.cse486586.simpledynamo.Network.SimpleClient;

/**
 * Created by mohit on 4/13/15.
 */
public class Methods {
    static final String TAG = Methods.class.getSimpleName();

    //Method to generate hash
    static public String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    //Method to compute successor and predecessor ports
    static public void setSuccPredPort(TreeMap<String, String> map){
        Set<String> key = map.keySet();
        List<String> portList = new ArrayList<String>(key);
        int index=0;
        for(int i=0; i<portList.size();i++){
            if(map.get(portList.get(i)).equals(Variables.myPortForHash)){
                index= i;
                break;
            }
        }
        int indexForsucc1= (index+1)%5;
        int indexForsucc2= (index+2)%5;
        int indexForpred1= (index+4)%5;
        int indexForpred2= (index+3)%5;
        Variables.succPort1= map.get(portList.get(indexForsucc1));
        Variables.succPort2= map.get(portList.get(indexForsucc2));
        Variables.predPort1= map.get(portList.get(indexForpred1));
        Variables.predPort2= map.get(portList.get(indexForpred2));
        Log.e("First Successor", Variables.myPortForHash+" "+Variables.succPort1);
        Log.e("Second Successor",Variables.myPortForHash+" "+ Variables.succPort2);
        Log.e("First Predecessor",Variables.myPortForHash+" "+ Variables.predPort1);
        Log.e("Second Predecessor",Variables.myPortForHash+" "+ Variables.predPort2);
        Variables.ports= map;
    }

    //Method for pre processing to compute ports
    static public TreeMap<String, String> processSuccPredPort(){
        TreeMap<String, String> ports= new TreeMap<String, String>();
        try{
            if(Variables.notAvailablePorts.size()==0){
                /*Message message= new Message();
                message.setMessageType("NewPorts");*/
                for(int i=0; i<Variables.REMOTE_PORTS.size();i++){
                        String otherPort= Variables.REMOTE_PORTS.get(i);
                        int value= Integer.parseInt(otherPort);
                        String hash= genHash(Integer.toString((value/2)));
                        ports.put(Variables.myHash, Variables.myPortForHash);
                        ports.put(hash, Integer.toString((value/2)));
                }
                //new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, message, null);
            }

        }
        catch (NoSuchAlgorithmException ne){
            Log.e(TAG, "Hash Failed");
        }
return ports;
    }

    static public ArrayList<String> findLocationOfKey(String key){

        try {
            ArrayList<String> portsToSend= new ArrayList<String>();
            if (Variables.succPort1 == null) {
                portsToSend.add(Variables.myPortForHash);
                return portsToSend;
            } /*else if (Variables.succPort.equals(Variables.myPortForHash)) {
                return Variables.myPortForHash;
            }*/
            else{
                Set<String> portHashes = Variables.ports.keySet();
                List<String> portHashesList = new ArrayList<String>(portHashes);
                String keyHash= genHash(key);
                if(keyHash.compareTo(Variables.ports.lastKey())>0 || keyHash.compareTo(Variables.ports.firstKey())<0){
                    portsToSend.add(Variables.ports.get(portHashesList.get(0)));
                    portsToSend.add(Variables.ports.get(portHashesList.get(1)));
                    portsToSend.add(Variables.ports.get(portHashesList.get(2)));
                    return portsToSend;
                }
                else{
                    for(int i=1; i<portHashesList.size(); i++){
                        if(keyHash.compareTo(portHashesList.get(i))<0 && keyHash.compareTo(portHashesList.get(i-1))>0){
                            if(i==(portHashesList.size()-1)){
                                portsToSend.add(Variables.ports.get(portHashesList.get(i)));
                                portsToSend.add(Variables.ports.get(portHashesList.get(0)));
                                portsToSend.add(Variables.ports.get(portHashesList.get(1)));
                            }
                            else if(i==(portHashesList.size()-2)){
                                portsToSend.add(Variables.ports.get(portHashesList.get(i)));
                                portsToSend.add(Variables.ports.get(portHashesList.get(i+1)));
                                portsToSend.add(Variables.ports.get(portHashesList.get(0)));
                            }
                            else{
                                portsToSend.add(Variables.ports.get(portHashesList.get(i)));
                                portsToSend.add(Variables.ports.get(portHashesList.get(i+1)));
                                portsToSend.add(Variables.ports.get(portHashesList.get(i+2)));
                            }

                            return portsToSend;
                        }
                        else{
                            continue;
                        }
                    }
                }
            }
        }
        catch(Exception e){

        }
        return null;
    }

    static public void insertIn(String key, String value, String type){
        Variables.commonLock.lock();
        try{
            FileOutputStream outputStream;
            if(type.equals("insert") || type.equals("Already")){
                Log.e(TAG,"Came here for insertion");
                Log.e("Directory 0", Variables.myContext.getFilesDir().toString() +"  " + key);
                outputStream = Variables.myContext.openFileOutput(key, Context.MODE_PRIVATE);
                outputStream.write(value.getBytes());
                outputStream.close();
            }
            else if(type.equals("replicate1")){
                Log.e(TAG,"Came here for replication 1 for insertion");
                File replicate1 = Variables.myContext.getDir("pred1", Context.MODE_PRIVATE);
                if(!replicate1.exists()){
                    replicate1.mkdir();
                    Log.e(TAG,"Making directory 1");
                }
                Log.e("Directory 1", replicate1.toString() +"  "+ key);
                File fileWithinMyDir = new File(replicate1, key);
                outputStream= new FileOutputStream(fileWithinMyDir);
                outputStream.write(value.getBytes());
                outputStream.close();

            }
            else if(type.equals("replicate2")){
                Log.e(TAG,"Came here for replication 2 for insertion");
                File replicate2 = Variables.myContext.getDir("pred2", Context.MODE_PRIVATE);
                if(!replicate2.exists()){
                    replicate2.mkdir();
                    Log.e(TAG,"Making directory 2");
                }
                Log.e("Directory 2", replicate2.toString()+"  "+ key);
                File fileWithinMyDir = new File(replicate2, key);
                outputStream= new FileOutputStream(fileWithinMyDir);
                outputStream.write(value.getBytes());
                outputStream.close();
            }


        }
        catch (IOException e){
            e.printStackTrace();
        }
       Variables.commonLock.unlock();
    }

    static public HashMap<String, String> query(String selection, Message m){
        Variables.commonLock.lock();
        HashMap<String, String> keyValue= new HashMap<String, String>();
        boolean found_key= false;
        try { String value= "";
            Log.v(TAG, "Key for query"+ "  "+ selection);
            String path=Variables.myContext.getFilesDir().getAbsolutePath()+"/"+selection;
            File file = new File ( path );
            if (file.exists()) {

                FileInputStream inputStream = Variables.myContext.openFileInput(selection);
                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = reader.readLine()) != null) {
                        value = "" + line;
                    }
                }
            }
            else{
                    Log.v(TAG, "came here when key not found in itself");
                    File directory= Variables.myContext.getDir("pred1", Context.MODE_PRIVATE);
                    Log.v("files ", selection);
                    File file1= new File(directory.getAbsolutePath()+"/"+selection);
                    if(file1.exists()){
                        FileInputStream inputStream= new FileInputStream(file1);
                        if (inputStream != null) {
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                            BufferedReader reader = new BufferedReader(inputStreamReader);
                            String line;
                            while ((line = reader.readLine()) != null) {
                                value = "" + line;
                            }
                            found_key= true;
                        }
                    }

                if(!found_key){
                    Log.v(TAG, "came here when key not found in pred1");
                    File directory1= Variables.myContext.getDir("pred2", Context.MODE_PRIVATE);
                    Log.v("files ", selection);
                    File file2= new File(directory1.getAbsolutePath()+"/"+selection);
                    FileInputStream inputStream1= new FileInputStream(file2);
                    if (inputStream1 != null) {
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream1);
                        BufferedReader reader = new BufferedReader(inputStreamReader);
                        String line;
                        while ((line = reader.readLine()) != null) {
                            value = "" + line;
                        }
                    }
                }
            }



            /*File directory= Variables.myContext.getDir("pred2", Context.MODE_PRIVATE);
            String files[] = directory.list();
            for(String f:files){
                if(f.equals(selection)){
                    Log.v("files ", f);
                    File file= new File(directory.getAbsolutePath()+"/"+f);
                    FileInputStream inputStream= new FileInputStream(file);
                    if (inputStream != null) {
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        BufferedReader reader = new BufferedReader(inputStreamReader);
                        String line;
                        while ((line = reader.readLine()) != null) {
                            value = "" + line;
                        }
                    }
                }
            }*/
            keyValue.put(selection, value);


        }
        catch(IOException ie){
            ie.printStackTrace();
        }
        Variables.commonLock.unlock();
        return keyValue;
    }

    static public void globalQuery(String selection, Message m){
        try{
            File directory= Variables.myContext.getFilesDir();
            String files[]= directory.list();
            String value= "";
            HashMap<String, String> keyValue= new HashMap<String, String>();
            for(String f:files) {
                if(!f.equals("recovery")){
                    FileInputStream inputStream = Variables.myContext.openFileInput(f);
                    if (inputStream != null) {
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        BufferedReader reader = new BufferedReader(inputStreamReader);
                        String line;
                        while ((line = reader.readLine()) != null) {
                            value = "" + line;
                        }
                    }
                    keyValue.put(f, value);
                    Log.v("putting values", f+" "+ value);
                }
            }
                Log.v("Sending for query @","");
                File directory_pred2= Variables.myContext.getDir("pred2", Context.MODE_PRIVATE);
                String files_pred2[] = directory_pred2.list();
                for(String f:files_pred2){
                    if(!f.equals("recovery")){
                        File file= new File(directory_pred2.getAbsolutePath()+"/"+f);
                        FileInputStream inputStream= new FileInputStream(file);
                        if (inputStream != null) {
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                            BufferedReader reader = new BufferedReader(inputStreamReader);
                            String line;
                            while ((line = reader.readLine()) != null) {
                                value = "" + line;
                            }
                        }
                        keyValue.put(f, value);
                        Log.v("putting values", f+" "+ value);
                    }

                }
                File directory_pred1= Variables.myContext.getDir("pred1", Context.MODE_PRIVATE);
                String files_pred1[] = directory_pred1.list();
                for(String f:files_pred1){
                    if(!f.equals("recovery")){
                        File file= new File(directory_pred1.getAbsolutePath()+"/"+f);
                        FileInputStream inputStream= new FileInputStream(file);
                        if (inputStream != null) {
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                            BufferedReader reader = new BufferedReader(inputStreamReader);
                            String line;
                            while ((line = reader.readLine()) != null) {
                                value = "" + line;
                            }
                        }
                        keyValue.put(f, value);
                        Log.v("putting values", f+" "+ value);
                    }

                }

            if(selection.equals("*")){
                Log.v("Sending for query *","");
                keyValue.putAll(m.getKeyValuePairs());
                m.setKeyValuePairs(keyValue);
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, m, null);
            }
            else{
                Variables.keyValuePair= keyValue;
            }

        }
        catch(IOException ie){
            ie.printStackTrace();
        }
    }
    static public void delete(String selection){
            boolean flagForDeletion= false;
            Log.v(TAG, "For my pred1");
            File directory= Variables.myContext.getDir("pred1", Context.MODE_PRIVATE);
            String files[] = directory.list();
            for(String f:files) {
                if(!f.equals("recovery")){
                    Log.v("files ", f);
                    File file = new File(directory.getAbsolutePath() + "/" + f);
                    file.delete();
                    flagForDeletion= true;;
                }

            }

            Log.v(TAG, "For my pred2");
            File directory1= Variables.myContext.getDir("pred2", Context.MODE_PRIVATE);
            String files1[] = directory1.list();
            for(String f:files1) {
                if(!f.equals("recovery")){
                    Log.v("files ", f);
                    File file = new File(directory1.getAbsolutePath() + "/" + f);
                    file.delete();
                    flagForDeletion= true;
                }

            }
            Log.v(TAG, "In myself");

            File directory_main= Variables.myContext.getFilesDir();
            String files_main[]= directory_main.list();
            for(String files2:files_main) {
                if(!files2.equals("recovery")){
                    Variables.myContext.deleteFile(files2);
                }

            }


    }

    static public void checkReplication(){
            String path=Variables.myContext.getFilesDir().getAbsolutePath()+"/recovery";
            File file = new File ( path );
            if (file.exists()) {
                Log.v(TAG, "Recovery exists");
                Variables.startedBefore= true;
            }
            else{
                insertIn("recovery", "anything", "Already");
            }
    }

    static public HashMap<String, String> recoveryMethod(String type){
        Variables.commonLock.lock();
        HashMap<String, String> keyValue= new HashMap<String, String>();
        try {
            String value="";
            if(type.equals("pred1")){
                File directory_pred1 = Variables.myContext.getDir("pred1", Context.MODE_PRIVATE);
                String files_pred1[] = directory_pred1.list();
                for (String f : files_pred1) {
                    if(!f.equals("recovery")){
                        File file = new File(directory_pred1.getAbsolutePath() + "/" + f);
                        FileInputStream inputStream = new FileInputStream(file);
                        if (inputStream != null) {
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                            BufferedReader reader = new BufferedReader(inputStreamReader);
                            String line;
                            while ((line = reader.readLine()) != null) {
                                value = "" + line;
                            }
                        }
                        keyValue.put(f+"_&_pred2", value);
                        Log.v("putting values", f+"_&_pred2"+" "+ value);
                    }

                }
                File directory= Variables.myContext.getFilesDir();
                String files[]= directory.list();
                for(String f:files) {
                    if(!f.equals("recovery")){
                        FileInputStream inputStream = Variables.myContext.openFileInput(f);
                        if (inputStream != null) {
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                            BufferedReader reader = new BufferedReader(inputStreamReader);
                            String line;
                            while ((line = reader.readLine()) != null) {
                                value = "" + line;
                            }
                        }
                        keyValue.put(f+"_&_pred1", value);
                        Log.v("putting values", f+"_&_pred1"+" "+ value);
                    }

                }
            }
            else if(type.equals("pred2")){
                File directory= Variables.myContext.getFilesDir();
                String files[]= directory.list();
                for(String f:files) {
                    if(!f.equals("recovery")){
                        FileInputStream inputStream = Variables.myContext.openFileInput(f);
                        if (inputStream != null) {
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                            BufferedReader reader = new BufferedReader(inputStreamReader);
                            String line;
                            while ((line = reader.readLine()) != null) {
                                value = "" + line;
                            }
                        }
                        keyValue.put(f+"_&_pred2", value);
                        Log.v("putting values", f+"_&_pred2"+" "+ value);
                    }

                }
            }
            else if(type.equals("succ2")){
                File directory_pred2 = Variables.myContext.getDir("pred2", Context.MODE_PRIVATE);
                String files_pred2[] = directory_pred2.list();
                for (String f : files_pred2) {
                    if(!f.equals("recovery")){
                        File file = new File(directory_pred2.getAbsolutePath() + "/" + f);
                        FileInputStream inputStream = new FileInputStream(file);
                        if (inputStream != null) {
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                            BufferedReader reader = new BufferedReader(inputStreamReader);
                            String line;
                            while ((line = reader.readLine()) != null) {
                                value = "" + line;
                            }
                        }
                        keyValue.put(f, value);
                        Log.v("putting values", f+"  myself"+" "+ value);
                    }

                }
            }
            else{
                File directory_pred1 = Variables.myContext.getDir("pred1", Context.MODE_PRIVATE);
                String files_pred1[] = directory_pred1.list();
                for (String f : files_pred1) {
                    if(!f.equals("recovery")){
                        File file = new File(directory_pred1.getAbsolutePath() + "/" + f);
                        FileInputStream inputStream = new FileInputStream(file);
                        if (inputStream != null) {
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                            BufferedReader reader = new BufferedReader(inputStreamReader);
                            String line;
                            while ((line = reader.readLine()) != null) {
                                value = "" + line;
                            }
                        }
                        keyValue.put(f, value);
                        Log.v("putting values", f+"_&_succ"+" "+ value);
                    }

                }
                File directory_pred2 = Variables.myContext.getDir("pred2", Context.MODE_PRIVATE);
                String files_pred2[] = directory_pred2.list();
                for (String f : files_pred2) {
                    if(!f.equals("recovery")){
                        File file = new File(directory_pred2.getAbsolutePath() + "/" + f);
                        FileInputStream inputStream = new FileInputStream(file);
                        if (inputStream != null) {
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                            BufferedReader reader = new BufferedReader(inputStreamReader);
                            String line;
                            while ((line = reader.readLine()) != null) {
                                value = "" + line;
                            }
                        }
                        keyValue.put(f+"_&_pred1", value);
                        Log.v("putting values", f+"  myself"+" "+ value);
                    }

                }
            }


        }
        catch(IOException ie){
            Log.e(TAG, "Exception Caught");
        }
        Variables.commonLock.unlock();
        return keyValue;
    }
}
