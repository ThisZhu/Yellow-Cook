package com.guidian104.yellowcook.video.capture.model;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.view.SurfaceView;

/**
 * Created by zhudi on 2018/3/25.算法帮助类，eg YUV数据变换处理
 */

public class AlgorithmHelper {

    /**
     * renderscript将data数据生成bitmap，效果不错
     * @param data
     * @return
     */
    @TargetApi(17)
    public static Bitmap RotateBitmap90(byte[] data, int height, int width, Context context){
        Bitmap bit=Bitmap.createBitmap(height,width,Bitmap.Config.ARGB_8888);
        RenderScript renderScript=RenderScript.create(context);
        Type.Builder builder=new Type.Builder(renderScript, Element.U8(renderScript)).setX(data.length);
        Allocation in=Allocation.createTyped(renderScript,builder.create(),Allocation.USAGE_SCRIPT);
        Type.Builder rgbaBuilder=new Type.Builder(renderScript,Element.RGBA_8888(renderScript)).setX(height).setY(width);
        Allocation out=Allocation.createTyped(renderScript,rgbaBuilder.create(),Allocation.USAGE_SCRIPT);
        in.copyFrom(data);
        ScriptIntrinsicYuvToRGB blur= ScriptIntrinsicYuvToRGB.create(renderScript,Element.U8_4(renderScript));
        blur.setInput(in);
        blur.forEach(out);
        out.copyTo(bit);
        in.destroy();
        blur.destroy();
        renderScript.destroy();
        out.destroy();
        return bit;
    }

    /**
     * 将data数据做90度旋转生成新的data数据
     * @param bytes
     * @return
     */
    public static byte[] RotateYuvData90(byte[] bytes,int width,int height){
        long startTime=System.nanoTime();
        byte[] yu=new byte[bytes.length];
        int h=height;
        int w=0;
        int what=width*h;
        int k=what-1;
        int t=k;
        for(int i=0;i<h;i++){
            k=t;
            for(int j=width;j>0;j--){
                yu[k]=bytes[j+w];
                k-=h;
            }
            --t;
            w+=width;
        }

        int h0=h;
        h=h/2;
        w=what-1;
        k=what*3/2-1;
        t=k;
        for(int i=0;i<h;i++){
            k=t;
            for(int j=width;j>0;j=j-2){
                yu[k-1]=bytes[j-1+w];
                yu[k]=bytes[j+w];
                k-=h0;
            }
            t-=2;
            w+=width;
        }


        long intervalTime=System.nanoTime()-startTime;
        android.util.Log.w("***************what","intervalTime"+Long.toString(intervalTime));
        return yu;
    }

    /**
     * 将YUV数据上下翻转
     * @param bytes
     * @return
     */
    public  static byte[] RotateYuvDataUpandDown(byte[] bytes,int width,int height){
        byte yu[]=new byte[bytes.length];

        int t=width*height;
        int what=t-1;
        int what0=t/2-height;
        int w=what-height+1;
        int h=width/2;
        int k=0;
        for (int i=0;i<h;i++){
            for (int j=w;j<=what;j++){
                yu[k]=bytes[j];
                yu[k+t]=bytes[j+what0];
                ++k;
            }
            w-=height;
            what-=height;
        }

        for (int i=h;i<width;i++){
            for (int j=w;j<=what;j++){
                yu[k]=bytes[j];
                ++k;
            }
            w-=height;
            what-=height;
        }

        return yu;
    }

    /**
     * 将bitmap做90度旋转生成新的bitmap,效果不错
     * @param bitmap
     * @return
     */
    public static Bitmap RotateBitmap90(Bitmap bitmap,int rotateDegree,int cameraPostion){
        if(bitmap==null)
            return null;
        Bitmap tagBitmap=null;
        Matrix matrix=new Matrix();
        matrix.postRotate(rotateDegree);//前后摄像头捕捉到的图像都是水平的，需要进行顺时针旋转90度
        if(cameraPostion==1)
            matrix.postScale(1,-1);//前置摄像头捕捉到的图像是水平左右反转的，需要进行左右反转，（-1，1）是上下反转的
        tagBitmap=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        if(tagBitmap==null)
            return bitmap;
        bitmap.recycle();
        return tagBitmap;
    }

    /**
     * 图片缩放，以适合小窗口
     * @param bitmap
     * @return
     */
    public static Bitmap ScaleBitmap(Bitmap bitmap, SurfaceView surfaceViewSmall){
        if(bitmap.getWidth()<=surfaceViewSmall.getWidth())
            return bitmap;
        Bitmap bitmap1=Bitmap.createScaledBitmap(bitmap,surfaceViewSmall.getWidth(),surfaceViewSmall.getHeight(),true);
        bitmap.recycle();
        return bitmap1;
    }

}
