package com.equationl.videoshoter;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Button button_user;
    AlertDialog.Builder dialog;
    AlertDialog dialog2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //判断权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            int i = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (i != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                showDialogTipUserRequestPermission();
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        button_user = (Button)findViewById(R.id.button_byUser);
        dialog = new AlertDialog.Builder(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        button_user.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                intent.addCategory(intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "请选择视频文件"),1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = uri.getPath();
            Intent intent = new Intent(Main2Activity.this, chooseActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("path", path);
            intent.putExtras(bundle);
            intent.setData(uri);
            startActivity(intent);
        }

        //权限判断
        else if (requestCode == 123) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 检查该权限是否已经获取
                int i = ContextCompat.checkSelfPermission(this,  Manifest.permission.WRITE_EXTERNAL_STORAGE);
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                if (i != PackageManager.PERMISSION_GRANTED) {
                    // 提示用户应该去应用设置界面手动开启权限
                    showDialogTipUserGoToAppSettting();
                } else {
                    if (dialog2 != null && dialog2.isShowing()) {
                        dialog2.dismiss();
                    }
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }

        else {
            Toast.makeText(getApplicationContext(),"未选择文件！",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_help) {
            String infor_text = getResources().getString(R.string.main_information);
            String content = String.format(infor_text, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString());
            dialog.setTitle("说明");
            dialog.setMessage(content);
            dialog.setIcon(R.mipmap.ic_launcher);
            dialog.create().show();
        } else if (id == R.id.nav_more) {
            Toast.makeText(getApplicationContext(),"敬请期待",Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_feedback) {
            String versionName;
            int currentapiVersion=0;
            try {
                PackageManager packageManager = getPackageManager();
                PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),0);
                versionName = packInfo.versionName;
                currentapiVersion=android.os.Build.VERSION.SDK_INT;
            }
            catch (Exception ex) {
                versionName = "NULL";
            }
            //Log.i("TEST","请在此描述您遇到的问题。\n---------请勿删除下面的内容------\n应用版本："+versionName+"\n"+"系统版本："+currentapiVersion);
            Intent data=new Intent(Intent.ACTION_SENDTO);
            data.setData(Uri.parse("mailto:admin@likehide.com"));
            data.putExtra(Intent.EXTRA_SUBJECT, "《视频截图》意见反馈");
            data.putExtra(Intent.EXTRA_TEXT, "请在此描述您遇到的问题。\n---------请勿删除下面的内容------\n应用版本："+versionName+"\n"+"系统版本："+currentapiVersion+"\n手机型号："+android.os.Build.MODEL);
            startActivity(data);
        } else if (id == R.id.nav_setting) {
            Intent intent = new Intent(Main2Activity.this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showDialogTipUserRequestPermission() {
        new AlertDialog.Builder(this)
                .setTitle("未授予读写储存权限")
                .setMessage("我们需要读取/写入储存的权限来保存您制作的作品")
                .setPositiveButton("立即授权 ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startRequestPermission();
                    }
                })
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }

    private void startRequestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                , 321);
    }

    // 用户权限 申请 的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 321) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                    boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                    if (!b) {
                        // 用户还是想用我的 APP 的
                        // 提示用户去应用设置界面手动开启权限
                        showDialogTipUserGoToAppSettting();
                    } else
                        finish();
                } else {
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // 提示用户去应用设置界面手动开启权限

    private void showDialogTipUserGoToAppSettting() {
        dialog2 = new AlertDialog.Builder(this)
                .setTitle("存储权限不可用")
                .setMessage("请在-应用设置-权限-中，允许视频截图使用存储权限来保存图片")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到应用设置界面
                        goToAppSetting();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }

    // 跳转到当前应用的设置界面
    private void goToAppSetting() {
        Intent intent = new Intent();

        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);

        startActivityForResult(intent, 123);
    }

}
