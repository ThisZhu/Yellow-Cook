package com.guidian104.yellowcook.video.capture.helper;

import android.os.Build;

/**
 * Created by zhudi on 2018/3/25.
 */

public final class SDKHelper {

    /***
     * api是否大于等于14，安卓4.0
     * @return
     */
    public static boolean isMoreIceCreamVersion(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            return true;
        return false;
    }

    /***
     * api是否大于等于15，安卓4.0.3
     * @return
     */
    public static boolean isMoreIceCreamMr1Version(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            return true;
        return false;
    }

    /***
     * api是否大于等于16，安卓4.1
     * @return
     */
    public static boolean isMoreJellyBeanVersion(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN)
            return true;
        return false;
    }

    /***
     * api是否大于等于17，安卓4.2
     * @return
     */
    public static boolean isMoreJellyBeanMr1Version(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR1)
            return true;
        return false;
    }

    /***
     * api是否大于等于18，安卓4.3
     * @return
     */
    public static boolean isMoreJellyBeanMr2Version(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR2)
            return true;
        return false;
    }

    /***
     * api是否大于等于19，安卓4.4
     * @return
     */
    public static boolean isMoreKitKatVersion(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT)
            return true;
        return false;
    }

    /***
     * api是否大于等于20，安卓4.4W,智能手表系统等
     * @return
     */
    public static boolean isMoreKitKatWatchVersion(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT_WATCH)
            return true;
        return false;
    }

    /***
     * api是否大于等于21，安卓5.0
     * @return
     */
    public static boolean isMoreLollipopVersion(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
            return true;
        return false;
    }

    /***
     * api是否大于等于22，安卓5.1
     * @return
     */
    public static boolean isMoreLollipopMr1Version(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP_MR1)
            return true;
        return false;
    }

    /***
     * api是否大于等于23，安卓6.0
     * @return
     */
    public static boolean isMoreAndroidMVersion(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
            return true;
        return false;
    }

    /***
     * api是否大于等于24，安卓7.0
     * @return
     */
    public static boolean isMoreAndroidNVersion(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
            return true;
        return false;
    }

}
