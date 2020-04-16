package de.salait.speechcare.utility;

import android.app.Activity;

/**
 * Created by Christian.Ramthun on 11.09.13.
 */
public class DeviceInfo {
    private int xDim;
    private int yDim;

    /**
     * returns the device height in pixels
     *
     * @param activity where this function should be used
     * @return int pixels
     */
    public static int getX(Activity activity) {
        return activity.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * returns the device width in pixels
     *
     * @param activity where this function should be used
     * @return int pixels
     */
    public static int getY(Activity activity) {
        return activity.getResources().getDisplayMetrics().widthPixels;
    }


}
