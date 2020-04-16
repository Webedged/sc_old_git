package de.salait.speechcare.utility;

import android.view.animation.Animation;
import android.widget.RelativeLayout;


/**
 * Created by Azad.Khanalizadeh on 13.09.13.
 */
public final class DisplayNextView implements Animation.AnimationListener {
    RelativeLayout ll_training;
    RelativeLayout rl_blau_1;
    RelativeLayout rl_blau_2;
    RelativeLayout rl_blau_3;
    RelativeLayout ll_info;
    RelativeLayout rl_gruen_1;
    RelativeLayout rl_gruen_2;
    RelativeLayout rl_gruen_3;
    private boolean mCurrentView;


    public DisplayNextView(boolean currentView, RelativeLayout ll_training, RelativeLayout ll_info, RelativeLayout rl_blau_1, RelativeLayout rl_blau_2, RelativeLayout rl_blau_3,
                           RelativeLayout rl_gruen_1, RelativeLayout rl_gruen_2, RelativeLayout rl_gruen_3) {
        mCurrentView = currentView;
        this.ll_training = ll_training;
        this.rl_blau_1 = rl_blau_1;
        this.rl_blau_2 = rl_blau_2;
        this.rl_blau_3 = rl_blau_3;
        this.ll_info = ll_info;
        this.rl_gruen_1 = rl_gruen_1;
        this.rl_gruen_2 = rl_gruen_2;
        this.rl_gruen_3 = rl_gruen_3;

    }

    public void onAnimationStart(Animation animation) {
    }

    public void onAnimationEnd(Animation animation) {
        ll_training.post(new SwapViews(mCurrentView, ll_training, ll_info, rl_blau_1, rl_blau_2, rl_blau_3, rl_gruen_1, rl_gruen_2, rl_gruen_3));
    }

    public void onAnimationRepeat(Animation animation) {
    }
}
