package io.github.daawa.debug;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.concurrent.atomic.AtomicLong;

import io.github.daawa.debug.logger.LogNode;

/**
 * Created by mae on 15/11/25.
 */

public class DebugFloatView extends RelativeLayout {

    private float xInScreen;
    private float yInScreen;
    private float xInView;
    private float yInView;
    private float xFirstTouch;
    private float yFirstTouch;

    private WindowManager wm;
    private WindowManager.LayoutParams wmParams;

    private DebugInfoPane infoPane;// = new DebugInfoPane(getContext());
    static StringBuilder curInfo = new StringBuilder(120);
    static AtomicLong msgCount = new AtomicLong(0);

    private static DebugFloatView one;

    public static DebugFloatView getInstance(Context context) {
        if (one == null) {
            one = new DebugFloatView(context);
        } else if (!one.getContext().equals(context)) {
            one.hide();
            one = new DebugFloatView(context);
        }

        return one;
    }


    private DebugFloatView(Context context) {
        super(context);
        wm = (WindowManager) getContext().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wmParams = new WindowManager.LayoutParams();
        initView();

        infoPane = new DebugInfoPane(getContext());
    }

    private void initView() {
        ImageView view = new ImageView(getContext());
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        view.setImageResource(R.drawable.debug_icon);
        Screen screen = new Screen(getContext());
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(screen.dp2px(32), screen.dp2px(32));
        addView(view, lp);
    }

    private int statusBarHeight = 0;

    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

    public DebugInfoPane getInfoPane(){
        return infoPane;
    }

    public DebugMonitor getMonitor(){
        return listener;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        xInScreen = event.getRawX();
        yInScreen = event.getRawY() - getStatusBarHeight();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
                xInView = event.getX();
                yInView = event.getY();
                xFirstTouch = xInScreen;
                yFirstTouch = yInScreen;
                break;
            case MotionEvent.ACTION_MOVE:
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                // 手指松开后，自动回到最近的边
                autoComplete();
                if (Math.abs(xFirstTouch - xInScreen) <= 10.0f
                        && Math.abs(yFirstTouch - yInScreen) <= 10.0f) {
                    performClick();
                }
                break;
            default:
                break;
        }
        return true;
    }

    // 更新位置
    private void updateViewPosition() {
        wmParams.x = (int) (xInScreen - xInView);
        wmParams.y = (int) (yInScreen - yInView);
        wm.updateViewLayout(this, wmParams);
    }

    // 自动完成移动
    private void startMove(final int xDirection, final int yDirection) {
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);
        int screenWidth = metric.widthPixels;
        int screenHeight = metric.heightPixels - getStatusBarHeight();

        final int moveX = Math.abs(xDirection)
                * ((screenWidth + getWidth()) * (xDirection + 1) / 2 - wmParams.x * xDirection);// X方向需要移动的距离
        final int moveY = Math.abs(yDirection)
                * ((screenHeight + getHeight()) * (yDirection + 1) / 2 - wmParams.y * yDirection);// Y方向需要移动的距离

        final int startX = wmParams.x;
        final int startY = wmParams.y;
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1.0f).setDuration(400);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (Float) animation.getAnimatedValue();
                wmParams.x = startX + (int) (xDirection * moveX * f * f);
                wmParams.y = startY + (int) (yDirection * moveY * f * f);
                wm.updateViewLayout(DebugFloatView.this, wmParams);
            }
        });
        animator.start();
    }

    // 自动靠到最近的一边
    private void autoComplete() {
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;
        int height = metric.heightPixels - getStatusBarHeight();
        int curViewCenterX = wmParams.x + getWidth() / 2;
        int curViewCenterY = wmParams.y + getHeight() / 2;
        // 先判断大概靠向那两个边
        int xDirection = curViewCenterX > width / 2 ? 1 : -1;
        int yDirection = curViewCenterY > height / 2 ? 1 : -1;
        // 在比较此时x，y方向上那边更近
        int x = (width * (xDirection + 1) / 2 - curViewCenterX) * xDirection;
        int y = (height * (yDirection + 1) / 2 - curViewCenterY) * yDirection;
        if (x < y) {
            yDirection = 0;
        } else {
            xDirection = 0;
        }
        startMove(xDirection, yDirection);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
//        CallBackWrapper.setDebugListener(listener);
//        BaseRequest.setDebugListener(listener);
//        BaseApplication.logWrapper.setNext(logNode);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        CallBackWrapper.setDebugListener(null);
//        BaseRequest.setDebugListener(null);
//        if (BaseApplication.logWrapper.getNext() == logNode)
//            BaseApplication.logWrapper.setNext(null);

    }

    /**
     * 显示悬浮窗
     */
    public void show() {
        if (isShown()) {
            return;
        }

        //wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //'TYPE_TOAST' does not need 'android.Manifest.permission.SYSTEM_ALERT_WINDOW'
        wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = 300;
        wmParams.y = 0;
        wm.addView(this, wmParams);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (infoPane == null) {
                    infoPane = new DebugInfoPane(getContext());
                }

                if (infoPane.isShown()) {
                    infoPane.hide();
                } else {
                    infoPane.show();
                }

            }
        });
    }

    /**
     * 隐藏悬浮窗
     */
    public void hide() {
        if (wm != null)
            wm.removeView(this);
    }


    final DebugMonitor listener = new DebugMonitor() {
        @Override
        public void requestDescription(String msg) {
            update(msg, 1);
        }

        @Override
        public void responseDescription(String msg) {
            update(msg, 2);
        }

        @Override
        public void crashDes(final String msg) {
            update(msg, 3);
        }
    };


    LogNode logNode = new LogNode() {
        @Override
        public void println(int priority, String tag, String msg, Throwable tr) {
            String logMsg = "==>LOG:" + "--" + tag + "--" + msg;
            update(logMsg, 3);
        }
    };

    private void update(String str, int type) {
        String decodedjson;
        try {
            decodedjson = URLDecoder.decode(str, "UTF-8");
        } catch (Exception e) {
            decodedjson = str;
        }

        String typeStr;
        switch (type) {
            case 1:
                typeStr = "REQUEST";
                break;
            case 2:
                typeStr = "RESPONSE";
                break;
            case 3:
                typeStr = "LOG";
                break;
            default:
                typeStr = "";
        }

        curInfo.append("\n\n======== ").append(msgCount.incrementAndGet()).append(" =======\n")
                .append(typeStr).append("\n").append(decodedjson);

        //String decodedjson = (new JSONObject(jsonString)).toString(4);
        //decodedjson = JsonFormater.formatJson(jsonString);
        if (infoPane == null)
            return;

        infoPane.updateContent(curInfo.toString());
    }
}
