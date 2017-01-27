package com.equationl.videoshoter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button button_self, button_user;
    AlertDialog.Builder dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button_self = (Button)findViewById(R.id.button_inform);
        button_user = (Button)findViewById(R.id.button_byUser);
       dialog = new AlertDialog.Builder(this);  //先得到构造器

        button_self.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String infor_text = getResources().getString(R.string.main_information);
                String content = String.format(infor_text, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString());
                dialog.setTitle("说明");
                dialog.setMessage(content);
                dialog.setIcon(R.mipmap.ic_launcher);
                dialog.create().show();
            }
        });

        button_user.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"手动模式",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                intent.addCategory(intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "请选择视频文件"),1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = uri.getPath();
            Intent intent = new Intent(MainActivity.this, chooseActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("path", path);
            intent.putExtras(bundle);
            intent.setData(uri);
            startActivity(intent);
        }
        else {
            Toast.makeText(getApplicationContext(),"未选择文件！",Toast.LENGTH_LONG).show();
        }
    }
}
