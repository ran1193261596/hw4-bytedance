package com.bytedance.practice5;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.bytedance.practice5.model.Message;
import com.bytedance.practice5.model.MessageListResponse;
import com.bytedance.practice5.socket.SocketActivity;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "chapter5";
    private FeedAdapter adapter = new FeedAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Fresco.initialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.rv_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        findViewById(R.id.btn_upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,UploadActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.btn_mine).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData(Constants.STUDENT_ID);
            }
        });

        findViewById(R.id.btn_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData(null);
            }
        });
        findViewById(R.id.btn_socket).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SocketActivity.class);
                startActivity(intent);
            }
        });



    }

    //TODO 2
    // ???HttpUrlConnection????????????????????????????????????Gson?????????????????????UI?????????adapter.setData()?????????
    // ?????????????????????UI???????????????????????????????????????
    private void getData( final String studentId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Message> repos = baseGetReposFromRemote(studentId);
                if (repos != null && !repos.isEmpty()) {
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setData(repos);
                        }
                    });

                }
            }
        }).start();
    }
    public List<Message> baseGetReposFromRemote(String studentId) {
        if (studentId ==null){
            studentId="   ";
        }

        String urlStr = String.format("https://api-sjtu-camp-2021.bytedance.com/homework/invoke/messages?student_id=%s",studentId);
        List<Message> result = null;
        MessageListResponse begin=null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(6000);

            conn.setRequestMethod("GET");
            String token=Constants.token;
            conn.setRequestProperty("token",token);

            if (conn.getResponseCode() == 200) {

                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                begin = new Gson().fromJson(reader, new TypeToken<MessageListResponse>() {
                }.getType());


                reader.close();
                in.close();

            } else {
                // ????????????
            }
            conn.disconnect();
            result=begin.feeds;

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "????????????" + e.toString(), Toast.LENGTH_SHORT).show();
        }
        return result;
    }
}


