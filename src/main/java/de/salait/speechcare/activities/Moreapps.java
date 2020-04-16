package de.salait.speechcare.activities;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import de.salait.speechcare.R;


public class Moreapps extends Activity {
    public WebView webViewweitereapps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moreapps);

        Point displaySize;
        Display display = getWindowManager().getDefaultDisplay();
        displaySize = new Point();
        display.getSize(displaySize);

        RelativeLayout content = (RelativeLayout) findViewById(R.id.moreContent);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) content.getLayoutParams();
        params.height = (int) (displaySize.y * 0.8);
        params.width = (int) (displaySize.x * 0.8);
        content.setLayoutParams(params);

        RelativeLayout backg = (RelativeLayout) findViewById(R.id.moreBackground);
        FrameLayout.LayoutParams bparams = (FrameLayout.LayoutParams) backg.getLayoutParams();
        bparams.height = (int) (displaySize.y);
        bparams.width = (int) (displaySize.x);
        backg.setLayoutParams(bparams);

        backg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        webViewweitereapps = (WebView) findViewById(R.id.webViewweitereapps);

        webViewweitereapps.setInitialScale(1);

        webViewweitereapps.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webViewweitereapps.getSettings().setUseWideViewPort(true);
        webViewweitereapps.setBackgroundColor(0);
        webViewweitereapps.loadUrl("file:///android_asset/more_apps_de.html");

    }
}
