package com.equationl.videoshoter.videoImg;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.preference.PreferenceManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
public class tools {
    /**
    * 将 bitmap 保存为png
     *
     * @return File 返回保存的文件
    * */
    public File saveBitmap2png(Bitmap bmp, String bitName,File savePath,boolean isReduce, int quality) throws Exception {
        File f;
        Bitmap.CompressFormat imgFormat;

        if (isReduce) {
            f = new File(savePath,bitName + ".jpg");
            imgFormat = Bitmap.CompressFormat.JPEG;
        }
        else {
            f = new File(savePath,bitName + ".png");
            imgFormat = Bitmap.CompressFormat.PNG;
        }

        f.createNewFile();
        FileOutputStream fOut = null;
        fOut = new FileOutputStream(f);
        bmp.compress(imgFormat, quality, fOut);
        fOut.flush();
        fOut.close();

        return f;
    }
    public File saveBitmap2png(Bitmap bmp, String bitName, File savePath) throws Exception {
        return this.saveBitmap2png(bmp, bitName, savePath, false, 100);
    }

    /**
     * 拼接两个 bitmap
    * */
    public Bitmap jointBitmap(Bitmap first, Bitmap second) {
        int width = Math.max(first.getWidth(),second.getWidth());
        int height = first.getHeight() + second.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(first, 0, 0, null);
        canvas.drawBitmap(second, 0, first.getHeight(), null);
        return result;
    }

    /**
     * 从文件获取图片的bitmap
    * */
    public Bitmap getBitmapFromFile(int no, File dirPath, String extension) throws Exception {
        File path = new File(dirPath, +no+""+"."+extension);
        FileInputStream f;
        Bitmap bm = null;
        f = new FileInputStream(path);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        BufferedInputStream bis = new BufferedInputStream(f);
        bm = BitmapFactory.decodeStream(bis, null, options);

        return bm;
    }

    /**
    * 裁切图片
    * */
    public Bitmap cutBimap(Bitmap bm, int startY, int width) {
        return Bitmap.createBitmap(bm, 0, startY, width, bm.getHeight()-startY);
    }



}
