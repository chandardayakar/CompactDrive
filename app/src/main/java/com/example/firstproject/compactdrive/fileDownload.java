package com.example.firstproject.compactdrive;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class fileDownload extends AppCompatActivity {

    private String fileURL=null;
    private String filename=null;
    private String Mtype = null;
    File my_file;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileURL = getIntent().getStringExtra("downURL");
        filename = getIntent().getStringExtra("filename");
        Mtype = getIntent().getStringExtra("MType");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        new My_Task().execute();
        setContentView(R.layout.activity_file_download);

    }
    class My_Task extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params) {
            if (Client.aceToken != null) {
                try {
                    URL temp = new URL(fileURL);
                    HttpURLConnection con = (HttpURLConnection) temp.openConnection();
                    con.setRequestMethod("GET");
                    String authToken = "OAuth " + Client.aceToken;
                    con.setRequestProperty("Authorization", authToken);
                    int resCode = con.getResponseCode();
                    if (resCode == 200) {
                        String storagePath = Library.context.getFilesDir().getPath();
                        File dir = new File(storagePath +"/compact drive");
                        if(dir.exists()) {
                            String filePath = storagePath + "/compact drive/"+filename;
                            my_file = new File(filePath);
                            boolean fileExists = my_file.exists();
                            if(con.getContentLength() != -1) {
                                if (fileExists) {
                                    return null;
                                } else {
                                    try {
                                        DataInputStream in = new DataInputStream(con.getInputStream());
                                        byte[] buffer = new byte[con.getContentLength()];
                                        in.readFully(buffer);
                                        in.close();
                                        if(buffer.length > 0) {
                                            DataOutputStream out = new DataOutputStream(new FileOutputStream(my_file));
                                            out.write(buffer);
                                            out.flush();
                                            out.close();
                                        }
                                    } catch (Exception e) {
                                        Log.i("file store problem", e.getMessage());
                                    }
                               }
                            }
                        }
                    } else if (resCode == 401) {

                        Client.refreshToken();
                        HttpURLConnection con2 = (HttpURLConnection) temp.openConnection();
                        con2.setRequestMethod("GET");
                        authToken = "OAuth " + Client.aceToken;
                        con2.setRequestProperty("Authorization", authToken);
                        resCode = con2.getResponseCode();
                        if (resCode == 200) {
                            String storagePath = Library.context.getFilesDir().getPath();
                            File dir = new File(storagePath +"/compact drive");
                            if(dir.exists()) {
                                String filePath = storagePath + "/compact drive/"+filename;
                                my_file = new File(filePath);
                                boolean fileExists = my_file.exists();
                                if (fileExists) {
                                    return null;
                                } else {
                                    try {
                                        ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
                                        FileOutputStream fos = new FileOutputStream(my_file);
                                        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                                    } catch (Exception e) {
                                        Log.i("file store problem", e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(Mtype.equals("text/plain")){
                TextView d_file = (TextView)findViewById(R.id.displayText);
                d_file.setBackgroundResource(R.color.white);
                StringBuilder text = new StringBuilder();
                try {
                    BufferedReader br = new BufferedReader(new FileReader(my_file));
                    String line;
                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        text.append('\n');
                    }
                    br.close();
                }
                catch (Exception e) {
                    Log.i("displayFile",e.getMessage());
                }
                d_file.setText(text);
            }
            else if(Mtype.startsWith("image\\")) {
                ImageView imaged = (ImageView) findViewById(R.id.displayImage);
                Bitmap myBitmap = BitmapFactory.decodeFile(my_file.getAbsolutePath());
                imaged.setImageBitmap(myBitmap);
            }
            else{
                
            }
        }
    }

}
