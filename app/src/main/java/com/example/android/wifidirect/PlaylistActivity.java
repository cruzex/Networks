package com.example.android.wifidirect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.provider.SyncStateContract;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static android.content.ContentValues.TAG;

/**
 * Created by Simarjit on 21-Mar-17.
 */

public class PlaylistActivity extends Activity {

    ListView listView;
    private Thread workingthread = null;
    public DatagramSocket socket;
    public Socket socket1;
    public String ip;


    final String MEDIA_PATH = Environment.getExternalStorageDirectory().getPath()+ "/";
    ArrayList<String> songList = new ArrayList<String>();
    ArrayList<String> songPath = new ArrayList<String>();
    ArrayList<String> ipAdd = new ArrayList<String>();
    String mp3Pattern = ".mp4";

    //message handler of the main UI thread
    //the handler will be passed once the background thread is created
    //and it will be triggered once a message is received
    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            // This is where you do your work in the UI thread.
            // Your worker tells you in the message what to do.
            Toast.makeText(getApplicationContext(), "File Recieved", Toast.LENGTH_LONG);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist);

        listView = (ListView)findViewById(R.id.listview);

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        Toast.makeText(getApplicationContext(), ip, Toast.LENGTH_LONG);

        int start = 100;
        for(int i=0; i<10; i++){
            String temp = Integer.toString(start);

            ipAdd.add(i, "192.168.1."+temp);
            start++;
        }
        workingthread = new JobThread(mHandler, getApplicationContext());
        workingthread.start();

    }

    public void refresh(View view)throws IOException{
        final View vv = view;



        getPlayList();
        updateList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String absolutePath = songPath.get(position);//parent.getItemAtPosition(position).toString();
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(),Video.class);
                intent.putExtra("video" , absolutePath);
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, final View v,
                                           int index, long arg3) {
                //final View v = view;
                final int i = index;
                AlertDialog.Builder builder = new AlertDialog.Builder(PlaylistActivity.this);
                builder.setMessage("do you want to share it?");
                builder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        /*File f = new File(songPath.get(i));
                        f.delete();
                        songList.remove(i);
                        songPath.remove(i);
                        updateList();*/
                        String pp = songPath.get(i);
                        //ddf.videoSyncer(vv, "file://" + pp, getApplicationContext());

                        sendBroadcast("file://"+pp);


                    }
                });

                builder.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

                return true;
            }
        });

    }

    public void sendBroadcast(String messageStr) {
        //Toast.makeText(getApplicationContext(), "done", Toast.LENGTH_LONG).show();
        // Hack Prevent crash (sending should be done using an async task)
        StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        for(int i=0; i<5; i++) {
            socket1 = new Socket();

            try {
                InetAddress address = InetAddress.getByName(ipAdd.get(i));
                boolean reachable = address.isReachable(1000);

                if (!ipAdd.get(i).equals(ip) && reachable) {
                    try {
                        socket1.bind(null);
                        socket1.connect((new InetSocketAddress(ipAdd.get(i), 8988)), 5000);

                        OutputStream stream = socket1.getOutputStream();
                        ContentResolver cr = getApplicationContext().getContentResolver();
                        InputStream is = null;
                        try {
                            is = cr.openInputStream(Uri.parse(messageStr));
                        } catch (FileNotFoundException e) {
                            //Log.d(WiFiDirectActivity.TAG, e.toString());
                            Log.d("TAG", e.toString());
                        }
                        DeviceDetailFragment.copyFile(is, stream); // custom comment
                        Log.d(WiFiDirectActivity.TAG, "Client: Data written");
                    } catch (IOException e) {
                        Log.e("hell", e.getMessage());
                    } finally {
                        if (socket1 != null) {
                            if (socket1.isConnected()) {
                                try {
                                    socket1.close();
                                } catch (IOException e) {
                                    // Give up
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    //socket1.close();
                }

            }catch (Exception e){
                Log.e("Lull", e.toString());
            }


        }

    }

    public void updateList()
    {
        if(songList.size()>0)
        {

            String [] videoString = songList.toArray(new String[0]);

            ArrayAdapter<String> VideoList = new CustomAdapter(this,videoString);
            listView.setAdapter(VideoList);
        }

    }

    public void getPlayList() {
        System.out.println(MEDIA_PATH);
        if (MEDIA_PATH != null) {
            File home = new File(MEDIA_PATH);
            File[] listFiles = home.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file : listFiles) {
                    System.out.println(file.getAbsolutePath());
                    if (file.isDirectory()) {
                        scanDirectory(file);
                    } else {
                        addSongToList(file);
                    }
                }
            }
        }
    }

    private void scanDirectory(File directory) {
        if (directory != null) {
            File[] listFiles = directory.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file : listFiles) {
                    if (file.isDirectory()) {
                        scanDirectory(file);
                    } else {
                        addSongToList(file);
                    }

                }
            }
        }
    }

    private void addSongToList(File song) {
        if (song.getName().endsWith(mp3Pattern)) {


            long fileSizeInBytes = song.length();
            double fileSizeInKB = fileSizeInBytes / 1024.0;
            String len = "";
            if(fileSizeInKB>400)
            {
                double fileSizeInMB = fileSizeInKB / 1024.0;
                if(fileSizeInMB>=1)
                    len = new DecimalFormat(".##").format(fileSizeInMB)+"MB";
                else
                    len ="0"+new DecimalFormat(".##").format(fileSizeInMB)+"MB";
            }
            else
                len = new DecimalFormat(".##").format(fileSizeInKB)+"KB";
            String name = song.getName().substring(0,song.getName().length()-4);

            songList.add(name+"\n"+len);
            songPath.add(song.getPath().toString());


        }
    }

    public void left_tab(View view) {
        Intent intent = new Intent(PlaylistActivity.this, WiFiDirectActivity.class);
        startActivity(intent);
    }

}

class JobThread extends Thread{

    private Handler hd;
    private Context context;

    public JobThread(Handler msgHandler, Context c){
        //constructor
        //store a reference of the message handler
        hd = msgHandler;
        context = c;
    }
    public void run() {
        Log.e("lull", "runned");

        while(true) {
            try {
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(WiFiDirectActivity.TAG, "Server: connection done");
                final File f = new File(Environment.getExternalStorageDirectory() + "/"
                        + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
                        + ".mp4");

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

                Log.d(WiFiDirectActivity.TAG, "server: copying files " + f.toString());
                InputStream inputstream = client.getInputStream();

                copyFile(inputstream, new FileOutputStream(f));
                //Toast.makeText(context, "File Recieved", Toast.LENGTH_LONG);
                serverSocket.close();

            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());

            }
        }
        //create the bundle
        /*Bundle b = new Bundle(4);

        //add integer data to the bundle, everyone with a key
        b.putInt("key1", 4);
        b.putInt("key2", 7);
        b.putInt("key3", 91);

        //create a message from the message handler to send it back to the main UI
        Message msg = hd.obtainMessage();

        //specify the type of message
        msg.what = 1;

        //attach the bundle to the message
        msg.setData(b);

        //send the message back to main UI thread
        hd.sendMessage(msg);

        try {
            Thread.sleep(5000);
        }catch (Exception ee){
            Log.e("Lull", ee.toString());
        }*/
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[4096];
        int len;
        long startTime=System.currentTimeMillis();

        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            inputStream.close();
            long endTime=System.currentTimeMillis()-startTime;
            Log.v("","Time taken to transfer all bytes is : "+endTime);

        } catch (IOException e) {
            Log.d(WiFiDirectActivity.TAG, e.toString());
            return false;
        }

        return true;
    }

}
