package com.xiateng.mutidownload;

import android.app.Activity;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Button mBtnDownload;
    private ProgressBar progressBar;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                long progress = intent.getIntExtra("progress",0);
                progressBar.setProgress((int) progress);
                Log.e(TAG,"***************** 下载进度："+progress+"%");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.xiaoteng.download");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);

        mBtnDownload = (Button) findViewById(R.id.btn_download);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        mBtnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,DownloadService.class);
                intent.putExtra("url","http://gdown.baidu.com/data/wisegame/0904344dee4a2d92/QQ_718.apk");
                startService(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
}
