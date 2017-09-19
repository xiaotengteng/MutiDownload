package com.xiateng.mutidownload;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadService extends Service {
    private static final String TAG = DownloadService.class.getSimpleName();
    private long total = 0;
    private long length;
    private long time;

    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        final String urls = intent.getStringExtra("url");
        if (!TextUtils.isEmpty(urls)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(urls);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(5000);
                        length = connection.getContentLength();
                        if (length <= 0) {
                            Toast.makeText(DownloadService.this, "文件不存在", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        File file = new File(Environment.getExternalStorageDirectory(), getFileName(urls));

                        RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
                        randomFile.setLength(length);

                        long blockSize = length / 3;
                        for (int i = 0; i < 3; i++) {
                            long begin = i * blockSize;
                            long end = (i + 1) * blockSize;
                            if (i == 2) {
                                end = length;
                            }
                            new Thread(new DownloadThread(begin,end,file,url,i)).start();

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
        return super.onStartCommand(intent, flags, startId);
    }

    private String getFileName(String urls) {
        int index = urls.lastIndexOf("/") + 1;
        return urls.substring(index);
    }

    class DownloadThread implements Runnable {

        private long begin;
        private long end;
        private File file;
        private URL url;
        private int id;

        public DownloadThread(long begin, long end, File file, URL url, int id) {
            this.begin = begin;
            this.end = end;
            this.file = file;
            this.url = url;
            this.id = id;
        }

        @Override
        public void run() {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setRequestProperty("Range","bytes="+begin+"-"+end);
                InputStream in = connection.getInputStream();

                byte[] buffer = new byte[1024 * 1024];
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(begin);
                int len = 0;
                while ((len = in.read(buffer)) != -1) {
                    randomAccessFile.write(buffer, 0, len);
                    updateProgress(len);
//                    Log.e(TAG,"*********** 下载中："+len);
                }

                in.close();
                randomAccessFile.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    synchronized private void updateProgress(int len) {
        total += len;
        // TODO: 2017/9/11 发广播
        if (length-total<1000){
            Intent intent = new Intent("com.xiaoteng.download");
            intent.putExtra("progress",100);
            Log.e(TAG,"******************** 下载完成："+100+"%");
            LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
        }
        if (System.currentTimeMillis()-time>1000){
            time = System.currentTimeMillis();
            Log.e(TAG,"******************** 下载完成："+total+"%"+length);
            Intent intent = new Intent("com.xiaoteng.download");
            int progress = (int) (total*100/length);
            intent.putExtra("progress",progress);
            Log.e(TAG,"******************** 下载完成："+progress+"%");
            LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
        }
    }
}
