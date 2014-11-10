package com.example.kazuki.myview7;

/**
 * Created by kazuki on 2014/11/05.
 */
        import android.app.AlertDialog;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.graphics.Bitmap;
        import android.graphics.Canvas;
        import android.graphics.ImageFormat;
        import android.graphics.Matrix;
        import android.hardware.Camera;
        import android.util.AttributeSet;
        import android.util.Log;
        import android.view.*;

        import java.io.IOException;
        import java.util.ArrayList;
        import java.util.List;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback{
    private SurfaceHolder holder = null;
    private Camera camera;
    public byte[] mFrameBuffer;
    public int[] mGrayResult;
    public Bitmap bitmap;
    public Bitmap bitmap2;
    public Camera.Size size;//カメラのサイズ
    public int mCurrentIndex = 0;//mGrayListを回すインデックス
    //public final int SLIT_HEIGHT = 64;//スリットの高さ
    public ArrayList<int[]> mGrayList;//最近のやつ数枚が全て入っている
    public CameraPreview camerapreview = new CameraPreview();
    public Matrix m = new Matrix();
    public final int width1  = 320;
    public final int height1 = 240;

    public MySurfaceView(Context context){
        super(context);
        initSurface();
    }

    public MySurfaceView(Context context, AttributeSet attrs){
        super(context, attrs);
        initSurface();
    }
    public MySurfaceView(Context context, AttributeSet attrs,int defStyle){
        super(context, attrs, defStyle);
        initSurface();
    }

    private void initSurface() {
        holder = this.getHolder();
        holder.addCallback(this);
        //Log.d("mysv", "initSurface ok");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
        try {
//            camera.setPreviewDisplay(holder);
            camera.setPreviewDisplay(null);
        } catch (IOException e) {
            e.printStackTrace();
            camera.release();
            camera = null;
        }
        camera.setPreviewCallbackWithBuffer(this);
        camera.startPreview();
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> supportedSizes = parameters.getSupportedPreviewSizes();
        //size = supportedSizes.get(0);
        //size = supportedSizes.get(0);
        size = camera.getParameters().getSupportedPreviewSizes().get(0);

        // 保持しておく画像たち
        int len = 2;//size.height/SLIT_HEIGHT;
        mGrayList = new ArrayList<int[]>(len);
        for (int i = 0; i < len; i++) {
            mGrayList.add(new int[width1 * height1]);
        }
        m.postRotate(90);
        m.postScale(4,6);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("WIDTH=",String.valueOf(width));
        Log.d("SIZE.WIDTH=",String.valueOf(size.width));
        Log.d("HEIGHT=",String.valueOf(height));
        Log.d("SIZE.HEIGHT",String.valueOf(size.height));
        Log.d("mysv", "surfaceChanged start");
        camera.stopPreview();
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(width1, height1);
        camera.setParameters(parameters);

        // 縦画面の場合回転させる
        //camera.setDisplayOrientation(90);
        //グレースケール画像　byte (NV21) -> int (ARGB_B888)
        mGrayResult = new int[width1 * height1];
        //描画するBitmap int (ARGB_8888) -> Bitmap
        bitmap = Bitmap.createBitmap(
                width1, height1, Bitmap.Config.RGB_565);
        bitmap2 = Bitmap.createBitmap(
                width1, height1, Bitmap.Config.RGB_565);
        int num = width1 * height1 *
                ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8;
        mFrameBuffer = new byte[num];
        camera.addCallbackBuffer(mFrameBuffer);
        camera.startPreview();

        Log.d("mysv", "surfaceChanged finish");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {//data.length == 1382,400
        Log.d("mysv", "onPreviewFrame start");
        int[] frame = mGrayList.get(mCurrentIndex);
        // frame.length == 921,600
        // mGrayListは[I@44750810]のような写真の保存場所が格納されている
        // mCurrentIndexは1ずつ増えていく
        int[] rgb = new int[(width1 * height1)];
        camerapreview.decodeYUV420SP(rgb, data, width1, height1);
        frame = camerapreview.changeFrame(frame,rgb,width1,height1);

        //ここだけ変えればいいんじゃないか？
        //
        camerapreview.feature(mGrayResult, mGrayList, mCurrentIndex, width1, height1);

        final SurfaceHolder myHolder = getHolder();


        // int[]をBitmapに変換する
        bitmap.setPixels(mGrayResult, 0, width1, 0, 0, width1, height1);
        // SurfaceViewにBitmapをただ描画する
        Canvas canvas = null;
        Log.d("BITMAP(w,h)",String.valueOf(bitmap.getWidth()) + "," + String.valueOf(bitmap.getHeight()));
        bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, width1, height1, m, true);
        Log.d("BITMAP2(w,h)",String.valueOf(bitmap2.getWidth()) + "," + String.valueOf(bitmap2.getHeight()));

        try {
            canvas = myHolder.lockCanvas();
            if (canvas != null) {
                canvas.drawBitmap(bitmap2,0,0,null);
            }
        } finally {
            if (canvas != null) {
                myHolder.unlockCanvasAndPost(canvas);
            }
        }
        Log.d("mフレームバッファー",String.valueOf(mFrameBuffer));
        Log.d("mグレイリスト",String.valueOf(mGrayList));
        camera.addCallbackBuffer(mFrameBuffer);
        Log.d("mysv", "onPreviewFrame finish");
        mCurrentIndex = (mCurrentIndex + 1) % mGrayList.size();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("onTouchEvent","CLICK POINT");
        bitmap.setPixels(mGrayResult, 0, width1, 0, 0, width1, height1);
        return true;
    }

}