package io.github.daawa.debugview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import io.github.daawa.debug.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(BuildConfig.debugSwitched) {
            DebugFloatView lv = DebugFloatView.getInstance(getApplication());
            final DebugMonitor monitor = lv.getMonitor();
            lv.show();
            findViewById(R.id.debug).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    monitor.responseDescription("tst response");
                }
            });
        }
    }
}
