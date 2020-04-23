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


public class Impressum extends Activity {
    public WebView webViewimpressum;
    private TextView textViewimpressum;
    private TextView textViewimpressumunten;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.impressum2);

        Point displaySize;
        Display display = getWindowManager().getDefaultDisplay();
        displaySize = new Point();
        display.getSize(displaySize);

        LinearLayout backg = (LinearLayout) findViewById(R.id.impBackground2);
        FrameLayout.LayoutParams bparams = (FrameLayout.LayoutParams) backg.getLayoutParams();
        bparams.height = displaySize.y;
        bparams.width = displaySize.x;
        backg.setLayoutParams(bparams);

        backg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LinearLayout content = (LinearLayout) findViewById(R.id.contentImpressum);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) content.getLayoutParams();
        params.height = (int) (displaySize.y * 0.8);
        params.width = (int) (displaySize.x * 0.8);
        content.setLayoutParams(params);

        webViewimpressum = (WebView) findViewById(R.id.webview);

        webViewimpressum.setInitialScale(1);

        webViewimpressum.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webViewimpressum.getSettings().setUseWideViewPort(true);
        webViewimpressum.setBackgroundColor(0);
        webViewimpressum.loadUrl("file:///android_asset/impressum_de.html");
//Schriftart


        /*textViewimpressum = (TextView) findViewById(R.id.textViewImpressum);
        textViewimpressumunten = (TextView) findViewById(R.id.textViewimpressumunten);

        Typeface font = Typeface.createFromAsset(getAssets(), "ltelight.ttf");
        textViewimpressum.setTypeface(font);
        textViewimpressumunten.setTypeface(font);

        textViewimpressum.setPaintFlags(textViewimpressum.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textViewimpressumunten.setPaintFlags(textViewimpressumunten.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);

        webViewimpressum = (WebView) findViewById(R.id.webViewimpressum);

        webViewimpressum.setInitialScale(1);

        webViewimpressum.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webViewimpressum.getSettings().setUseWideViewPort(true);
        webViewimpressum.setBackgroundColor(0);
        webViewimpressum.loadUrl("file:///android_asset/impressum_de.html");*/

    }
}