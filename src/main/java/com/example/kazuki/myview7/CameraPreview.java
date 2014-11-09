package com.example.kazuki.myview7;

import android.hardware.Camera;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by kazuki on 2014/11/05.
 * Byte配列を用いて画像処理を行うClass
 */

public class CameraPreview {


    private static final int SLIT_HEIGHT = 64;
    private static final int[] GRAY_ARGB_LUT = new int[256];//Grayスケールにしたい場合
    static {
        for (int i = 0; i < 256; i++) {
            GRAY_ARGB_LUT[i] = 0xff000000 | i << 16 | i << 8 | i;
            //例えばi=64(2進数で01000000,16進数で40)の場合、0xff404040となる
        }
    }

    //data(Yuv型)を受け取りRGBの配列で返す
    public static final void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);
                if (r < 0) r = 0; else if (r > 262143) r = 262143;
                if (g < 0) g = 0; else if (g > 262143) g = 262143;
                if (b < 0) b = 0; else if (b > 262143) b = 262143;
                rgb[yp] = 0xf8000000 | ((r << 6) & 0xf80000)|((g >> 2) & 0xf800)|((b >> 10) & 0xf8);
                //Log.d("rgb8=",String.valueOf(rgb[yp]));
                //rgb[yp] = 0xffff & ((rgb[yp] & 0xf80000)>>8 | (rgb[yp] & 0xfc00)>>5|(rgb[yp] & 0xf8)>>3);
                //Log.d("rgb4=",String.valueOf(rgb[yp]));
            }
        }
    }

    //フォーマットを合わせる
    public static int[] changeFrame(int frame[], int rgb[], int width1, int height1){

        for (int i = 0; i < frame.length; i ++) {
            frame[i] = rgb[i] & 0xffffffff;
            /*Grayスケール化したい場合はこちらを使う
            int gray = rgb[i] & 0xff;//0~255の値が入る
            // 明度をARGBに変換します
            // 例えば明度が30なら、A=255, R=30, G=30, B=30としています
            frame[i] = GRAY_ARGB_LUT[gray];
            */
            //以降に変換処理したい内容を入れる
            //average(frame,width1,height1);
        }
        return frame;
    }

    //上下左右の4点を取得して計算する
    public static int[] average(int frame[], int width1, int height1){
        int above, under, left, right,j;


        for(j = width1; j < frame.length - width1;j++){
            above = frame[j - width1];
            left = frame[j -1];
            right = frame[j + 1];
            under = frame[j + width1];
        }


        return frame;
    }

    //画像を組み合わせていくモジュール
    public static void conbine(int[] mGrayResult,ArrayList<int[]> mGrayList, int mCurrentIndex, Camera.Size size){
        int n = size.width * SLIT_HEIGHT;
        int p = 0;

        for (int i = 0; i < mGrayList.size(); i ++) {
            int index = (mCurrentIndex + 1 + i) % mGrayList.size();
            int[] f = mGrayList.get(index);
            System.arraycopy(f, p, mGrayResult, p, n);
            p += n;
        }
    }

    public static void feature(int[] mGrayResult,ArrayList<int[]> mGrayList, int mCurrentIndex, int width1, int height1){
        int n = width1* height1;
        int p = 0;

        //for (int i = 0; i < mGrayList.size(); i ++) {
            int index = (mCurrentIndex) % mGrayList.size();
            int[] f = mGrayList.get(index);
            System.arraycopy(f, p, mGrayResult, p, n);
            Log.d("フレームlength", String.valueOf(f.length));
            Log.d("Nの数", String.valueOf(n));
            Log.d("mGrayList.get(index)", String.valueOf(f[100]));
            //p += n;
        //}
    }
}
