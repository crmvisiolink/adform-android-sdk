package com.adform.sdk.resources;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.adform.sdk.utils.Utils;

/**
 * Created by mariusm on 28/05/14.
 */
public class CloseImageView extends ImageView {
    public static final int CLOSE_IMAGE_DIMEN = 64;
    private Context mContext;
    private Bitmap mBitmap;
    public static final String CLOSE_INTERSTITIAL_XXHDPI = "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";
    public static final String CLOSE_INTERSTITIAL_XHDPI = "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";
    public static final String CLOSE_INTERSTITIAL_HDPI = "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";
    public static final String CLOSE_INTERSTITIAL_MDPI = "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";
    public static final String CLOSE_INTERSTITIAL_LDPI = "iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3gUcCxouVmGoXQAABFFJREFUaN7tmT2IXFUUx387sxM3QjarS5zoCtpkjdklWogxMahFLCS9RVBCIljYaKmCBtEUURDWJZU2VmqXYBewC0oWbUQbwQ9MoZKgScjOi87sjM3/yN3jfTP3fcxuBC9c3uO9e8/9n+9z74X/eJsYE80G0NQToA+s6Tm4GRmYDAD3gG7OuJbGDcRQr46FqzBv0twObAMeAnYB83pf1P9vgC+B79S/Aq4Bv0dobajZ7QaWgB8FwPeueuzfD5q7e8wmva41pLVF4DPgigPVD+w81mP/r4jWomiPlYltwLNaeHWIdIv0rmgNgGe0xljaNHBeC92oAbjvRvO81qqttUVwNTCDwZi60b4uTbRTbHqUs2bA1WDsxAYEiKaiVFZ1zdtEJBuj1PN6BnSEoVS0mQYubAJw3y8IS6MI+AZwPMHm+zXa/bB/xwJcSe3hhGhzLZBQ0cj0p54rjtawsfuKONGKIkEe0at6tuVwbwV2mwp+WRLdkcDEdQkqyaH3CmB3hFrbwB4VaACvFDCJU5qzRaXEHSPmdsXg3hTpf5wgxU+ArcHcW/R8KcjSfk5Hz9c11s//MGHdj4ZpYRKYBS4mOt1OYCEigGOB2geOoRcjABakzZSgcFEYo1X0FPBgIiEr4u5ygIzwkUjx9nxgNmES3en8apTgHgg0/i8GjhYIj8bE4zl7jCPB2OcceGuPOlopDBx15rdOGsslY/i92sh4c3oBeC1iNvdLe2VyyVKsDLJI8kWJRGSqn3dAGxEB2b97Ckje988d5nURYVCy7jEmnopotum+PVkBfBZsPbfmMdCpWBIclj/5ditwqGIJ0vEMJNcWBTb5x1VWtJyJdoIgUXurqgErD046+5wI7N5n7KwODeDC27cVwL/tHHVC9t903wBersDE17GQbI72QUm7X3JSMdN8Wj38ZknoREl/eN9h/kcyU8qWqURN8u/GVKoEZ5n4sUjSBHijgCbCjD4Vq4emgP2JDBj405EktQe4O1JKzOXUTm8m7ieMzv5YJjYn2wFcTlTl6RyzaUfivL3f6caaJk4mCu6yMLbywuAkcCYB/DkHwNqBIUnKvh10c8wZP01Y90zK6d0jKoV7I4jNBtXoQlBVpmxo5mRqiMbtI9bqCdO+1Lzwfc6mxPvAjCuJU8qDP1wpPuNoxvqqMCW3gwmOdUMSfbVEbWNjT4hGlnDkeCAVvMXXd2o6GqljzqlY7B918TED/HoTHGz9IiylLmPmnd1uZLc1d1U9aN2+CUzYWtOjzoKGldODIMzNBBFnI5qtN+ewVL4Pu5QQ8sp2o3kJuK/uezMjMgu8F8TmtRqArwU5Z1lrjO0ewmqQw8DPLn73E0OjH5cBPwX76da47TMMZ08AZ4G/ci4nMvfuTeasaJS+t65DTU1VoPM6lj+kk4dYO6cr1RVdeP8mE6rl0rrK3XFLtPrSxrC2RXPs1Lm/2Qx4es3AFPouXPcCx/+/AfwN1dlOXbyUyFUAAAAASUVORK5CYII=";

    public CloseImageView(Context context) {
        this(context, null);
    }

    public CloseImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CloseImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public void setVisible(boolean isVisible) {
        if (isVisible) {
            setImageBitmap(getBitmapByDensity(mContext));
        } else {
            setImageBitmap(null);
        }
    }

    public RelativeLayout.LayoutParams getStandardLayoutParams() {
        int closeDimen = getCloseButtonDimen();
        final RelativeLayout.LayoutParams closeImageViewParams = new RelativeLayout.LayoutParams(closeDimen,
                closeDimen);
        closeImageViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        closeImageViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        return closeImageViewParams;
    }

    private Bitmap getBitmapByDensity(Context context) {
        if (mBitmap == null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            byte[] bytes;
            switch (metrics.densityDpi) {
                case DisplayMetrics.DENSITY_LOW:
                    bytes = Base64.decode(CLOSE_INTERSTITIAL_LDPI, Base64.DEFAULT);
                    break;
                case DisplayMetrics.DENSITY_HIGH:
                case DisplayMetrics.DENSITY_TV:
                    bytes = Base64.decode(CLOSE_INTERSTITIAL_HDPI, Base64.DEFAULT);
                    break;
                case DisplayMetrics.DENSITY_XHIGH:
                case DisplayMetrics.DENSITY_400:
                    bytes = Base64.decode(CLOSE_INTERSTITIAL_XHDPI, Base64.DEFAULT);
                    break;
                case DisplayMetrics.DENSITY_XXHIGH:
                case DisplayMetrics.DENSITY_XXXHIGH:
                    bytes = Base64.decode(CLOSE_INTERSTITIAL_XXHDPI, Base64.DEFAULT);
                    break;
                default:
                    bytes = Base64.decode(CLOSE_INTERSTITIAL_MDPI, Base64.DEFAULT);
            }
            mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        return mBitmap;
    }

    public int getCloseButtonDimen(){
        Resources r = getResources();
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CLOSE_IMAGE_DIMEN, r.getDisplayMetrics());
    }
}
