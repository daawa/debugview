package io.github.daawa.debug;

import android.content.Context;
import android.util.DisplayMetrics;

public class Screen {

    public Screen(Context app){
        DisplayMetrics metrics = app.getResources().getDisplayMetrics();
        this.DENSITY_RATE = metrics.density;
        this.DENSITY_DPI = metrics.densityDpi;
        this.PIXEL_WIDTH = metrics.widthPixels;
        this.PIXEL_HEIGHT = metrics.heightPixels;
    }

    /** 像素宽度 */
    public int PIXEL_WIDTH;
    /** 像素高度 */
    public int PIXEL_HEIGHT;

    /**
     * 屏幕点密度，DPI is Dots Per Inch.
     * 支持四种：ldpi (low), mdpi (medium), hdpi (high), and xhdpi (extra high)。
     */
    public int DENSITY_DPI;
    /** 屏幕密度比，等于DPI/160，也等于DP除以Pixel，即在此屏幕上每个像素相当于多少DP */
    public float DENSITY_RATE;

    /**
     * 根据设备密度从 dp 的单位 转成为 px
     */
    public int dp2px(int dpValue) {
        return (int) (dpValue * DENSITY_RATE + 0.5f);  //0.5是为了Fix取整误差
    }

    /**
     * 根据设备密度从 px 的单位 转成为 dp
     */
    public int px2dp(int pxValue) {
        return (int) (pxValue / DENSITY_RATE + 0.5f);  //0.5是为了Fix取整误差
    }

    /**
     * 判断当前是否是横竖屏，返回值为true为横屏，false为竖屏
     * @return
     */
    public boolean isScreenLand(){
        return PIXEL_WIDTH > PIXEL_HEIGHT ? true : false;
    }

}