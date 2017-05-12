package io.github.daawa.debug;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by mae on 15/11/25.
 */

public class DebugInfoPane extends LinearLayout {

    private WindowManager wm;
    private WindowManager.LayoutParams wmParams;

    private ScrollView container;
    private TextView infoView;

    private TextView clear;
    private TextView copy;
    private TextView formal;
    private TextView test;

    public DebugInfoPane(Context context) {
        super(context);
        wm = (WindowManager) getContext().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wmParams = new WindowManager.LayoutParams();
        initView();
    }

    private void initView() {
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.debug_info_pane, this, true);
        container = (ScrollView)findViewById(R.id.scroll_container);
        container.isSmoothScrollingEnabled();
        infoView = (TextView) findViewById(R.id.info_view);

        clear = (TextView) findViewById(R.id.clear);
        clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugFloatView.curInfo.delete(0, DebugFloatView.curInfo.length());
                infoView.setText(null);

            }
        });

        copy = (TextView) findViewById(R.id.copy);
        copy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int start = infoView.getSelectionStart();
                int end = infoView.getSelectionEnd();
                if(start > 0 && end > 0){
                    CharSequence selected = infoView.getText().subSequence(start,end);
                    ClipboardManager cm = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    //cm.setText(selected);
                    cm.setPrimaryClip(ClipData.newPlainText("DebugLog", selected));
                }
            }
        });

        formal = (TextView) findViewById(R.id.formal_env);
//        formal.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // BaseRequest.BaseUrl = APIs.host_online;
//                BaseRequest.mBaseUrl = APIs.host_online;
//                infoView.setText("BASE:" + BaseRequest.mBaseUrl);
//
//            }
//        });

        test = (TextView) findViewById(R.id.test_env);
//        test.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                BaseRequest.mBaseUrl = APIs.host_debug;
//                infoView.setText("BASE:" + BaseRequest.mBaseUrl);
//
//            }
//        });

        setOrientation(VERTICAL);
        setBackgroundColor(Color.DKGRAY);
        setAlpha(0.7f);

    }

    /**
     * 显示悬浮窗
     */
    public void show() {
        if (isShown()) {
            return;
        }
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);
        int screenWidth = metric.widthPixels;
        int screenHeight = metric.heightPixels;
        //wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        // wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        // wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.width = (int) (screenWidth * 0.9);
        wmParams.height = (int) (screenHeight * 0.7);

        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = (int) ((screenWidth * 0.1) / 2);
        wmParams.y = (screenHeight - wmParams.height) / 2;
        wm.addView(this, wmParams);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        infoView.setText(DebugFloatView.curInfo.toString());
    }

    /**
     * 隐藏悬浮窗
     */
    public void hide() {
        wm.removeView(this);
    }




    public void updateContent(final String content){
        if(infoView == null)
            return;

        infoView.post(new Runnable() {
            @Override
            public void run() {
                if(infoView == null)
                    return;

                infoView.setText(content);
                container.post(new Runnable() {
                    @Override
                    public void run() {
                        container.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

}


