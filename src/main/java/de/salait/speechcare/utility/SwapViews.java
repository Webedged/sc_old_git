package de.salait.speechcare.utility;

import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;


/**
 * Created by Azad.Khanalizadeh on 13.09.13.
 */
public class SwapViews implements Runnable {
    private final boolean mIsFirstBox;
    private final RelativeLayout ll_training;
    private final RelativeLayout ll_info;
    private final RelativeLayout rl_blau_1;
    private final RelativeLayout rl_blau_2;
    private final RelativeLayout rl_blau_3;
    private final RelativeLayout rl_gruen_1;
    private final RelativeLayout rl_gruen_2;
    private final RelativeLayout rl_gruen_3;


    public SwapViews(boolean isFirstBox, RelativeLayout ll_training, RelativeLayout ll_info, RelativeLayout rl_blau_1, RelativeLayout rl_blau_2, RelativeLayout rl_blau_3,
                     RelativeLayout rl_gruen_1, RelativeLayout rl_gruen_2, RelativeLayout rl_gruen_3) {
        mIsFirstBox = isFirstBox;
        this.ll_training = ll_training;
        this.ll_info = ll_info;
        this.rl_blau_1 = rl_blau_1;
        this.rl_blau_2 = rl_blau_2;
        this.rl_blau_3 = rl_blau_3;
        this.rl_gruen_1 = rl_gruen_1;
        this.rl_gruen_2 = rl_gruen_2;
        this.rl_gruen_3 = rl_gruen_3;

    }

    public void run() {
        final float centerX = ll_info.getWidth() / 2.0f;
        final float centerY = ll_training.getHeight() / 2.0f;
        FlipAnimation rotation;

        if (mIsFirstBox) {
            ll_training.setVisibility(View.GONE);
            rl_blau_1.setVisibility(View.GONE);
            rl_blau_2.setVisibility(View.GONE);
            rl_blau_3.setVisibility(View.GONE);


            ll_info.setVisibility(View.VISIBLE);
            rl_gruen_1.setVisibility(View.VISIBLE);
            rl_gruen_2.setVisibility(View.VISIBLE);
            rl_gruen_3.setVisibility(View.VISIBLE);
            ll_info.bringToFront();
            rl_gruen_1.bringToFront();
            rl_gruen_2.bringToFront();
            rl_gruen_3.bringToFront();
            ll_info.requestFocus();
            rl_gruen_1.requestFocus();
            rl_gruen_2.requestFocus();
            rl_gruen_3.requestFocus();

            rotation = new FlipAnimation(90, 0, centerX, centerY);


        } else {
            ll_info.setVisibility(View.GONE);
            rl_gruen_1.setVisibility(View.GONE);
            rl_gruen_2.setVisibility(View.GONE);
            rl_gruen_3.setVisibility(View.GONE);

            ll_training.setVisibility(View.VISIBLE);
            rl_blau_1.setVisibility(View.VISIBLE);
            rl_blau_2.setVisibility(View.VISIBLE);
            rl_blau_3.setVisibility(View.VISIBLE);

            ll_training.bringToFront();
            ll_training.requestFocus();
            rl_blau_1.requestFocus();
            rl_blau_2.requestFocus();
            rl_blau_3.requestFocus();
            rl_blau_1.bringToFront();
            rl_blau_2.bringToFront();
            rl_blau_3.bringToFront();


            rotation = new FlipAnimation(-90, 0, centerX, centerY);
        }
        rotation.setDuration(500);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new DecelerateInterpolator());
        if (mIsFirstBox) {
            ll_info.startAnimation(rotation);
            rl_gruen_1.startAnimation(rotation);
            rl_gruen_2.startAnimation(rotation);
            rl_gruen_3.startAnimation(rotation);
        } else {
            ll_training.startAnimation(rotation);
            rl_blau_1.startAnimation(rotation);
            rl_blau_2.startAnimation(rotation);
            rl_blau_3.startAnimation(rotation);
        }
    }
}
