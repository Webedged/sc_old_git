package de.salait.speechcare.utility;

import android.app.Activity;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by bodo.grossmann on 14.10.13.
 */
public class ButtonHighlighterOnTouchListener implements View.OnTouchListener {

    private static final int TRANSPARENT_GREY = Color.argb(0, 185, 185, 185);
    private static final int FILTERED_GREY = Color.argb(155, 185, 185, 185);

    ImageView imageView;
    ImageButton imageButton;
    TextView textView;
    RelativeLayout layout;
    RelativeLayout highlightLO;

    public ButtonHighlighterOnTouchListener(final ImageView imageView) {
        super();
        this.imageView = imageView;
    }

    public ButtonHighlighterOnTouchListener(final ImageButton imageButton) {
        super();
        this.imageButton = imageButton;
    }

    public ButtonHighlighterOnTouchListener(final TextView textView) {
        super();
        this.textView = textView;
    }

    public ButtonHighlighterOnTouchListener(final RelativeLayout layout, Activity act) {
        super();
        this.layout = layout;

        RelativeLayout lo = new RelativeLayout(act);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lo.setLayoutParams(layoutParams);
        lo.setBackgroundColor(Color.BLACK);
        lo.setAlpha(0.5f);
        this.highlightLO = lo;
    }

    public boolean onTouch(final View view, final MotionEvent motionEvent) {

        if (imageView != null) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                imageView.setColorFilter(FILTERED_GREY);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                imageView.setColorFilter(TRANSPARENT_GREY);
            }
        } else if (imageButton != null) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                imageButton.setBackgroundColor(FILTERED_GREY);
                imageButton.setColorFilter(FILTERED_GREY);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                imageButton.setBackgroundColor(TRANSPARENT_GREY);
                imageButton.setColorFilter(TRANSPARENT_GREY);
            }
        } else if (textView != null) {

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                textView.setBackgroundColor(Color.LTGRAY);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                textView.setBackgroundColor(Color.WHITE);
            }
        } else {

            if (layout == null) {
                return false;
            }

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                layout.addView(highlightLO);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                layout.removeView(highlightLO);
            }
        }
        return false;
    }

}
