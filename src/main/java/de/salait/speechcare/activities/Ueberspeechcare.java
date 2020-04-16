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


public class Ueberspeechcare extends Activity {
    public WebView webViewueberspeechcare;
    private TextView textviewueberspeechcare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ueberspeechcare);

        Point displaySize;
        Display display = getWindowManager().getDefaultDisplay();
        displaySize = new Point();
        display.getSize(displaySize);

        LinearLayout content = (LinearLayout) findViewById(R.id.aboutContent);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) content.getLayoutParams();
        params.height = (int) (displaySize.y * 0.8);
        params.width = (int) (displaySize.x * 0.8);
        content.setLayoutParams(params);

        LinearLayout backg = (LinearLayout) findViewById(R.id.aboutBackground);
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

        textviewueberspeechcare = (TextView) findViewById(R.id.textViewueberspcare);

        Typeface font = Typeface.createFromAsset(getAssets(), "ltelight.ttf");

        textviewueberspeechcare.setTypeface(font);


        textviewueberspeechcare.setPaintFlags(textviewueberspeechcare.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);

        webViewueberspeechcare = (WebView) findViewById(R.id.webViewueberspeechcare);
        webViewueberspeechcare.setInitialScale(1);
        webViewueberspeechcare.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webViewueberspeechcare.getSettings().setUseWideViewPort(true);
        webViewueberspeechcare.setBackgroundColor(0);
        webViewueberspeechcare.loadUrl("file:///android_asset/about_speechcare_de.html");
    }
}
