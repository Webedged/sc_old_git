package de.salait.speechcare.activities;

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.salait.speechcare.R;


public class Trainingstipps extends Activity {
    public WebView webViewtrainingstipps;
    private TextView textViewTrainingstipps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);


        Point displaySize;
        Display display = getWindowManager().getDefaultDisplay();
        displaySize = new Point();
        display.getSize(displaySize);

        LinearLayout content = (LinearLayout) findViewById(R.id.ttipsContent);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) content.getLayoutParams();
        params.height = (int) (displaySize.y * 0.8);
        params.width = (int) (displaySize.x * 0.8);
        content.setLayoutParams(params);

        LinearLayout backg = (LinearLayout) findViewById(R.id.ttipsBackground);
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

//Schriftart
        textViewTrainingstipps = (TextView) findViewById(R.id.textViewTrainigstipps);

        Typeface font = Typeface.createFromAsset(getAssets(), "ltelight.ttf");
        textViewTrainingstipps.setTypeface(font);

        textViewTrainingstipps.setPaintFlags(textViewTrainingstipps.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);


        webViewtrainingstipps = (WebView) findViewById(R.id.webViewtrainingstipps);
        webViewtrainingstipps.setInitialScale(1);
        webViewtrainingstipps.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webViewtrainingstipps.getSettings().setUseWideViewPort(true);
        webViewtrainingstipps.setBackgroundColor(0);
        webViewtrainingstipps.loadUrl("file:///android_asset/training_tipps_de.html");
    }
}

