package com.rhat.r_hat.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.rhat.r_hat.R;

public class LoginActivity extends AppCompatActivity {
    private Context context;
    private ImageButton imgbtn_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //隐藏标题栏
        getSupportActionBar().hide();

        setContentView(R.layout.activity_update);

        imgbtn_save = (ImageButton) findViewById(R.id.new_imgbtn_save);

        imgbtn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 开启一个子线程，进行本地储存日记，等待有返回结果，使用handler通知UI
                new Thread(userInfoSave).start();
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String result = data.getString("result");
            String value[] = result.split("_");
            Log.i("mylog", "请求结果为-->" + result);
            // TODO
            // UI界面的更新等相关操作
            if("edit".equals(value[0])){
                switch (Integer.parseInt(value[1])){
                    case 1:break;
                    case 0:break;
                    case -1:break;
                    default:
                }
            }
        }
    };

    /**
     * 子线程
     */
    //编辑日记
    Runnable userInfoSave = new Runnable() {

        @Override
        public void run() {
            // TODO
            String value = "save_1";
            Message msg = new Message();
            Bundle data = new Bundle();
            context = getBaseContext();
            //本地保存日记
            SharedPreferences spUser = context.getSharedPreferences("userInfo", context.MODE_PRIVATE);
            SharedPreferences.Editor editor = spUser.edit();
            editor.putString("username", "");
            //向Handle返回消息
            data.putString("result", value);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

}
