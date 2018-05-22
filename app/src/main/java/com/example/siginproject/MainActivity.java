package com.example.siginproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    EditText Euesrname;
    EditText Epassword;
    TextView login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Euesrname = findViewById(R.id.M_username);
        Epassword = findViewById(R.id.M_password);
        login = (TextView) findViewById(R.id.M_login);
        //设置自带toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);
        //点击登录按钮
        login.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putString("value", loginByPost(Euesrname.getText().toString(), Epassword.getText().toString()));
                        data.putString("studentNo", Euesrname.getText().toString());
                        msg.setData(data);
                        handler.sendMessage(msg);
                    }
                }).start();

            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String value = data.getString("value");
            //匹配密码
            if ("success".equals(value)) {
                Toast.makeText(MainActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("studentNo", data.getString("studentNo"));
                intent.setClass(MainActivity.this, MyActivity.class);
                startActivity(intent);
                MainActivity.this.finish();
            } else{
                Toast.makeText(MainActivity.this, "请检查密码是否正确", Toast.LENGTH_SHORT).show();
            }
        }
    };


    public String loginByPost(String studentNo, String password) {
        String path = Server.SERVER_HOST + "login";
        //参数
        String data = "studentNo=" + studentNo + "&password=" + password;
        return HttpURLConnectionUtils.connection(path, data);
    }

    //设置再按一次结束程序
    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //标题栏返回箭头设置
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
