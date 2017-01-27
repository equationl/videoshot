package com.equationl.videoshoter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.equationl.videoshoter.videoImg.tools;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class chooseActivity extends AppCompatActivity {
    VideoView videoview;
    View videoview_contianer;
    Button btn_status,btn_done,btn_shot;
    TextView text_count,video_time;
    Uri uri;
    Queue<Long> mark_time = new LinkedList<Long>();
    int pic_num=0, isFirstPlay=1, shot_num=0;
    GestureDetector mGestureDetector;
    Thread thread = new Thread(new MyThread());
    Boolean isDone=false;
    SharedPreferences settings;
    Boolean isHideBtn = false;
    Boolean isORIENTATION_LANDSCAPE = false;
    tools tool = new com.equationl.videoshoter.videoImg.tools();
    RelativeLayout.LayoutParams params;

    private static final int HandlerStatusHideTime = 10010;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
        setContentView(R.layout.activity_choose);

        videoview = (VideoView) findViewById(R.id.videoView);
        btn_status = (Button) findViewById(R.id.button_change_status);
        btn_done   = (Button) findViewById(R.id.button_done);
        btn_shot   = (Button) findViewById(R.id.button_shot);
        text_count = (TextView) findViewById(R.id.text_count);
        videoview_contianer = findViewById(R.id.main_videoview_contianer);
        video_time = (TextView) findViewById(R.id.video_time);

        params = (RelativeLayout.LayoutParams) btn_shot.getLayoutParams();

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        cleanExternalCache(this);    //清除上次产生的缓存图片

        /*Bundle bundle = this.getIntent().getExtras();
        String path = bundle.getString("path");  */

        uri = getIntent().getData();
        videoview.setMediaController(new MediaController(this));
        videoview.setVideoURI(uri);

        MediaMetadataRetriever rev = new MediaMetadataRetriever();
        rev.setDataSource(getApplicationContext(),uri);
        String meta_duration = rev.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
        long duration = Long.parseLong(meta_duration);
        Bitmap bitmap = rev.getFrameAtTime(((duration/2)*1000),
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        videoview.setBackground(new BitmapDrawable(bitmap));

        mGestureDetector = new GestureDetector(this, mGestureListener);
        videoview.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });


        btn_done   .setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (pic_num < 1) {
                    Toast.makeText(getApplicationContext(),"至少需要截取两张图片！",Toast.LENGTH_LONG).show();
                }
                else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    if (!thread.isAlive()) {
                        thread = new Thread(new MyThread());
                        thread.start();
                    }
                    isDone = true;
                    btn_shot.setClickable(false);
                    btn_done.setClickable(false);
                }
            }
        });
        btn_status .setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                /*if (videoview.isPlaying()) {
                    videoview.pause();
                    btn_status.setText("继续");
                }
                else {
                    videoview.start();
                    btn_status.setText("暂停");
                }*/
                if (isFirstPlay==1) {
                    videoview.setBackgroundResource(0);
                    videoview.start();
                    btn_status.setText("翻转");
                    isFirstPlay = 0;
                }
                else {
                    if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        isORIENTATION_LANDSCAPE = false;
                    } else {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        isORIENTATION_LANDSCAPE = true;
                    }
                }
            }
        });
        btn_shot   .setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               /* MediaMetadataRetriever rev = new MediaMetadataRetriever();
                rev.setDataSource(getApplicationContext(),uri);	//这里第一个参数需要Context，传this指针
                Bitmap bitmap = rev.getFrameAtTime(videoview.getCurrentPosition() * 1000,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                try {
                   if(saveMyBitmap(bitmap,pic_num+"")) {
                       text_count.setText("已截取 "+(pic_num+1)+" 张");
                       pic_num++;
                   }
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(),"保存截图失败"+e,Toast.LENGTH_LONG).show();
                }   */
                text_count.setText("标记/已截取："+ (pic_num+1) +"/"+ (shot_num+1));
                mark_time.offer((long)videoview.getCurrentPosition());
                pic_num++;
                if (!thread.isAlive()) {
                    thread = new Thread(new MyThread());
                    thread.start();
                }
            }
        });

        videoview.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if(what==MediaPlayer.MEDIA_ERROR_SERVER_DIED){
                    Toast.makeText(getApplicationContext(),"Media Error,Server Died"+extra,Toast.LENGTH_LONG).show();
                }else if(what==MediaPlayer.MEDIA_ERROR_UNKNOWN){
                    Toast.makeText(getApplicationContext(),"Media Error,Error Unknown "+extra,Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });

        videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btn_status.setText("重新播放");
                isFirstPlay = 1;
            }
        });
    }



    @Override
    protected void onRestart() {
        Log.i("el_test", "onRestart");
        super.onRestart();
        isDone = false;
        btn_shot.setClickable(true);
        btn_done.setClickable(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (videoview == null) {
            return;
        }
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){//横屏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().getDecorView().invalidate();
            float width = getWidthInPx(this);
            float height = getHeightInPx(this);
            videoview_contianer.getLayoutParams().height = (int) height;
            videoview_contianer.getLayoutParams().width = (int) width;
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            btn_shot.setLayoutParams(params);
            Log.i("TEST","width="+width+" height="+height);
        } else {
            final WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            float width = getWidthInPx(this);
            float height = getHeightInPx(this);
            videoview_contianer.getLayoutParams().height = (int) height;
            videoview_contianer.getLayoutParams().width = (int) width;
            params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            btn_shot.setLayoutParams(params);
        }
    }

    public static float getHeightInPx(Context context) {
        float height = context.getResources().getDisplayMetrics().heightPixels;
        return height;
    }
    public static float getWidthInPx(Context context) {
        float width = context.getResources().getDisplayMetrics().widthPixels;
        return width;
    }

    public boolean saveMyBitmap(Bitmap bmp, String bitName) throws IOException {
       /* File f = new File(getExternalCacheDir(),bitName + ".png");
        boolean flag = false;
        f.createNewFile();
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            flag = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;*/

        boolean flag;
        try {
            tool.saveBitmap2png(bmp,bitName, getExternalCacheDir());
            flag = true;
        } catch (Exception e) {
            flag = false;
        }

        return flag;
    }

    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }

    public static void cleanExternalCache(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            deleteFilesByDirectory(context.getExternalCacheDir());
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    text_count.setText(msg.obj.toString());
                    break;
                case 2:
                    text_count.setText(msg.obj.toString());
                    break;
                case 3:
                    Intent intent = new Intent(chooseActivity.this, makePictureActivity.class);
                    startActivity(intent);
                    break;
                case HandlerStatusHideTime:
                    video_time.setVisibility(View.GONE);
                    break;
            }

        }
    };

    public class MyThread implements Runnable {
        @Override
        public void run() {
            Message msg;

            MediaMetadataRetriever rev = new MediaMetadataRetriever();
            rev.setDataSource(getApplicationContext(),uri);
            Long time;

            //*********************************************TEST******************
            /*for (long el=2000000;el<999999999;el+=1000000) {
                Log.i("dead_test",el+" ");
                Bitmap bitmap = rev.getFrameAtTime(el
                        ,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                try {
                    if(saveMyBitmap(bitmap,shot_num+"")) {
                        msg = Message.obtain();
                        msg.obj = "标记/已截取："+(pic_num)+"/"+(shot_num+1);
                        msg.what = 1;
                        handler.sendMessage(msg);
                        shot_num++;
                    }
                } catch (IOException e) {
                    msg = Message.obtain();
                    msg.obj = "保存截图失败"+e;
                    msg.what = 2;
                    handler.sendMessage(msg);
                }
            }   */

            //*********************************************TEST END******************


            while ((time = mark_time.poll()) != null) {
                Log.i("el_test,获取截图 timeUs=", time+"");
                Bitmap bitmap = rev.getFrameAtTime(time * 1000
                        ,MediaMetadataRetriever.OPTION_CLOSEST);
                try {
                    if(saveMyBitmap(bitmap,shot_num+"")) {
                        msg = Message.obtain();
                        msg.obj = "标记/已截取："+(pic_num)+"/"+(shot_num+1);
                        msg.what = 1;
                        handler.sendMessage(msg);
                        shot_num++;
                    }
                } catch (IOException e) {
                    msg = Message.obtain();
                    msg.obj = "保存截图失败"+e;
                    msg.what = 2;
                    handler.sendMessage(msg);
                }
            }
            if (isDone) {
                msg = Message.obtain();
                msg.obj = "";
                msg.what = 3;
                handler.sendMessage(msg);
            }
        }
    }

    private android.view.GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (videoview.isPlaying()) {
                videoview.pause();
            }
            else {
                videoview.setBackgroundResource(0);
                videoview.start();
                btn_status.setText("翻转");
            }

            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i("test", "单击屏幕");
            if (settings.getBoolean("isHideButton", false) && isORIENTATION_LANDSCAPE) {
                if (isHideBtn) {
                    btn_status.setVisibility(View.VISIBLE);
                    btn_done.setVisibility(View.  VISIBLE);
                    btn_shot.setVisibility(View.  VISIBLE);
                    isHideBtn = false;
                }
                else {
                    btn_status.setVisibility(View.INVISIBLE);
                    btn_done.setVisibility(View.  INVISIBLE);
                    btn_shot.setVisibility(View.  INVISIBLE);
                    isHideBtn = true;
                }
            }

            return false;
        }


        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            video_time.setVisibility(View.VISIBLE);
            int px2ime = 500;
            videoview.seekTo(videoview.getCurrentPosition()-(int)distanceX*px2ime);
            String res;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            long lt = new Long(videoview.getCurrentPosition());
            Date date = new Date(lt);
            res = simpleDateFormat.format(date);
            video_time.setText(res);
            autoHideTime();

            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //video_time.setVisibility(View.GONE);
            return true;
        }
    };

    Timer tHide = null;
    private void autoHideTime() {
        if (tHide == null) {
            Log.i("test","call in autoHideTime with tHide is null");
            tHide = new Timer();
            tHide.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(HandlerStatusHideTime);
                    tHide = null;
                }
            }, 1000);
        }
    }

}
