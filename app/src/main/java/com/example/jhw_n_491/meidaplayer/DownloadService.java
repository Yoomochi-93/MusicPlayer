package com.example.jhw_n_491.meidaplayer;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class DownloadService extends IntentService {
    public static final int UPDATE_PROGRESS = 8344;
    public DownloadService() {
        super("DownloadService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        String urlToDownload = intent.getStringExtra("url");
        ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra("receiver");
        try {
            // 서버와 로컬의 mp3 파일의 md5sum을 비교한다.
            if (checkMD5SUM()) {
                Bundle resultData = new Bundle();
                resultData.putInt("progress" ,100);
                receiver.send(UPDATE_PROGRESS, resultData);
                return;
            }

            // md5sum이 다를 경우, 새로운 mp3 파일을 받아옴
            URL url = new URL(urlToDownload);
            URLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(connection.getInputStream());
            OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().toString()
                                                        + "/music.mp3");

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                Bundle resultData = new Bundle();
                resultData.putInt("progress" ,(int) (total * 100 / fileLength));
                receiver.send(UPDATE_PROGRESS, resultData);
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bundle resultData = new Bundle();
        resultData.putInt("progress" ,100);
        receiver.send(UPDATE_PROGRESS, resultData);
    }

    private boolean checkMD5SUM()
    {
        try {
            URL url = new URL("http://35.203.158.180/download/music.mp3.md5sum");
            URLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream input = new BufferedInputStream(connection.getInputStream());
            OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().toString()
                    + "/music.mp3.md5sum");

            byte data[] = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            // TODO: 서버의 md5sum 파일을 로컬 파일로 저장하여 파일에서 다시 읽는데,
            // 이것을 http 서버에서 텍스트를 문자열로 곧바로 가져 오도록 수정 할 것! (using GET method)
            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard,"music.mp3.md5sum");

            //Read text from file
            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            }
            catch (IOException e) {
                text.setLength(0);
            }
            String md5sum_str = text.toString();
            String md5sum = md5sum_str.split("[ ]+")[0];

            SharedPreferences sp = getSharedPreferences("Music_Player", Activity.MODE_PRIVATE);
            String saved_md5sum = sp.getString("md5sum", null);

            if (md5sum.equals(saved_md5sum)) {
                return true;
            } else {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("md5sum", md5sum);
                editor.apply();
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
