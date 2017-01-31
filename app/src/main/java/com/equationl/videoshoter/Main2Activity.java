package com.equationl.videoshoter;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
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

import com.equationl.videoshoter.videoImg.tools;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

public class Main2Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Button button_user;
    AlertDialog.Builder dialog;
    AlertDialog dialog2;
    android.support.design.widget.CoordinatorLayout container;
    tools tool;

    public static Main2Activity instance = null;    //FIXME  暂时这样吧，实在找不到更好的办法了

    private static final int HandlerStatusLoadLibsFailure = 0;
    private static final int HandlerStatusFFmpegNotSupported = 1;
    private static final int HandlerStatusPackageNameNotRight = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        instance = this;

        container =  (android.support.design.widget.CoordinatorLayout)findViewById(R.id.container);

        tool = new tools();

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


        Thread t = new Thread(new Main2Activity.MyThread());
        t.start();
        //loadLib();

        //*************************************************************
        /*FFmpeg ffmpeg = FFmpeg.getInstance(this);
        if (!ffmpeg.isFFmpegCommandRunning()) {
            //String videoPath = path;
            //String outPathName = getExternalCacheDir().toString()+"/"+shot_num+".jpg";
            //String posTime = Long.toString(10000);

            //Log.i("el_test: video_path=", path);

            String cmd[] = {"-ss", "300.6","-i","/storage/emulated/0/0.mp4", "-y", "-f", "image2",  "-t", "0.001","/storage/emulated/0/Android/data/com.equationl.videoshoter/cache/10065.png"};
                            //"-ss", ""+(time/1000.0), "-i", path, "-y", "-f", "image2", "-t", "0.001", outPathName
            try {
                ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                    @Override
                    public void onFailure(String message) {
                        Log.i("el_test: FAILURE", message);
                        Snackbar snackbar = Snackbar.make(container, "加载FFmpeg失败", Snackbar.LENGTH_SHORT);
                        snackbar.setAction("重试", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadLib();
                            }
                        });
                        snackbar.setActionTextColor(Color.BLUE);
                        snackbar.show();

                    }
                    @Override
                    public void onSuccess(String message) {
                        Log.i("el_test:  SUCCESS", message);
                        // Handle if FFmpeg is not supported by device
                        Snackbar snackbar = Snackbar.make(container, "很抱歉，无兼容您的手机的so库", Snackbar.LENGTH_LONG);
                        snackbar.setAction("联系开发者", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
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
                            }
                        });
                        snackbar.setActionTextColor(Color.BLUE);
                        snackbar.show();

                    }
                });
            } catch (FFmpegCommandAlreadyRunningException e) {
                Log.i("el_test: RUNNING", ""+e);
            }
        }  */

        //*****************************************************************8

        button_user.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                intent.addCategory(intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "请选择视频文件"),1);
            }
        });
    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //处理返回键，防止从其他activity跳转到这里时按返回键又回去了
            //FIXME 暂时这样，最好还是跳转时杀掉进程
            ActivityManager manager = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE); //获取应用程序管理器
            manager.killBackgroundProcesses(getPackageName()); //强制结束当前应用程序
            return true;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }   */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            //String path = uri.getPath();
            String path = tool.getImageAbsolutePath(this, uri);
            Intent intent = new Intent(Main2Activity.this, chooseActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("path", path);
            intent.putExtras(bundle);
            intent.setData(uri);
            startActivity(intent);
            //finish();
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
//            Toast.makeText(getApplicationContext(),"敬请期待",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Main2Activity.this, commandActivity.class);
            startActivity(intent);
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

    private void loadLib() {
        //加载 ffmpeg-android-java
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    handler.sendEmptyMessage(HandlerStatusLoadLibsFailure);
                }
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
            handler.sendEmptyMessage(HandlerStatusFFmpegNotSupported);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HandlerStatusLoadLibsFailure:
                    Snackbar snackbar = Snackbar.make(container, "加载FFmpeg失败", Snackbar.LENGTH_LONG);
                    snackbar.setAction("重试", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadLib();
                        }
                    });
                    snackbar.setActionTextColor(Color.BLUE);
                    snackbar.show();
                    break;
                case HandlerStatusFFmpegNotSupported:
                    Snackbar snackbar2 = Snackbar.make(container, "很抱歉，无兼容您的手机的so库", Snackbar.LENGTH_SHORT);
                    snackbar2.setAction("联系开发者", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
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
                        }
                    });
                    snackbar2.setActionTextColor(Color.BLUE);
                    snackbar2.show();
                    break;
                case HandlerStatusPackageNameNotRight:
                    //Toast.makeText(getApplicationContext(),"您下载的是非法打包版本，建议您前往正规渠道重新下载", Toast.LENGTH_LONG).show();
                    Snackbar snackbar3 = Snackbar.make(container, "您下载的是非法打包版本，建议您前往正规渠道重新下载", Snackbar.LENGTH_LONG);
                    snackbar3.show();
                    break;
            }

        }
    };

    public class MyThread implements Runnable {
        @Override
        public void run() {
            loadLib();
            try {
                String pkName = getApplicationContext().getPackageName();
                if (!pkName.equals("com.equationl.videoshoter")) {
                    handler.sendEmptyMessage(HandlerStatusPackageNameNotRight);
                }
            } catch (Exception e) {
                handler.sendEmptyMessage(HandlerStatusPackageNameNotRight);
            }
        }
    }
}
