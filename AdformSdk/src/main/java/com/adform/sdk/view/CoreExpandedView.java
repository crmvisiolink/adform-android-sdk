package com.adform.sdk.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import com.adform.sdk.utils.AdformEnum;
import com.adform.sdk.utils.Utils;
import com.adform.sdk.utils.entities.ExpandProperties;
import com.adform.sdk.utils.managers.AdformAnimationManager;
import com.adform.sdk.view.base.BaseCoreContainer;
import com.adform.sdk.view.base.BaseInnerContainer;
import com.adform.sdk.view.inner.InnerInterstitialView;

/**
 * Created by mariusm on 27/05/14.
 */
public class CoreExpandedView extends BaseCoreContainer implements AdformAnimationManager.SliderableWidgetProperties,
        AdformAnimationManager.SliderableWidgetCallbacks {
    public static final float TO_ALPHA = 0.44f; // like iOS var
    public static final float FROM_ALPHA = 0.0f;
    public static final int DEFAULT_FADE_DURATION = 500;
    private Animation mAnimation;
    private AdformAnimationManager mAdformAnimationManager;
    private View mDimmingView;
    private Animation mFadeInAnimation;
    private Animation mFadeOutAnimation;
    private AdformEnum.ExpandType mExpandType = AdformEnum.ExpandType.ONE_PART;

    public CoreExpandedView(Context context, BaseInnerContainer innerContainer, Bundle extras) {
        this(context, null, 0, innerContainer, extras);
    }

    public CoreExpandedView(Context context, Bundle extras) {
        this(context, null, 0, null, extras);
    }

    public CoreExpandedView(Context context) {
        this(context, null, 0);
    }

    public CoreExpandedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoreExpandedView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, 0, null, null);
    }

    public CoreExpandedView(Context context, AttributeSet attrs, int defStyle,
                            BaseInnerContainer innerContainer, Bundle extras) {
        super(context, attrs, defStyle, innerContainer, extras);
        mAdformAnimationManager = new AdformAnimationManager(this, new AdformAnimationManager.SlidingAnimationProperties() {
            @Override
            public Animation getCollapseAnimation() {
                return new TranslateAnimation(0.0f, 0.0f, 0.0f, getHeight());
            }

            @Override
            public Animation getExpandAnimation() {
                return new TranslateAnimation(0.0f, 0.0f, getHeight(), 0.0f);
            }

            @Override
            public int getAnimationDuration() {
                return AdformAnimationManager.DEFAULT_DURATION;
            }

            @Override
            public int getAnimationDelay() {
                return AdformAnimationManager.DEFAULT_DELAY;
            }
        });
        mAdformAnimationManager.setListenerCallbacks(this);
        getInnerView().setBaseListener(this);
        getInnerView().getMraidBridge().setMraidListener(this);
        getInnerView().getMraidBridge().setCoreBridgeListener(this);

        mDimmingView = new View(getContext());
        mDimmingView.setBackgroundColor(Color.BLACK);
        mDimmingView.setVisibility(View.INVISIBLE);
        mDimmingView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        addView(mDimmingView, 0);

        setAnimating(false);
        getInnerView().setVisibility(View.INVISIBLE);
        getInnerView().setCloseButtonEnabled(true);
        getInnerView().onUseCustomClose(mExtraParams.getBoolean(INNER_EXTRA_USE_CUSTOM_CLOSE, false));

        mFadeInAnimation = createAlphaAnimation(FROM_ALPHA, TO_ALPHA);
        mFadeOutAnimation = createAlphaAnimation(TO_ALPHA, FROM_ALPHA);

        String extraContent = mExtraParams.getString(INNER_EXTRA_CONTENT);
        if (extraContent != null) {
            mExpandType = AdformEnum.ExpandType.TWO_PART;
            showContent(extraContent);
        }
    }

    public void showContent(String content) {
        // Loaded content will always be loaded and mraid type
        setViewState(AdformEnum.VisibilityGeneralState.LOAD_SUCCESSFUL);
        super.showContent(content);
    }

    @Override
    public BaseInnerContainer getInnerView() {
        if (mInnerContainer == null) {
            Bundle extras = new Bundle();
            extras.putBoolean(BaseInnerContainer.INNER_EXTRA_SKIP_INIT, true);
            mInnerContainer = new InnerInterstitialView(mContext, extras);
        }
        return mInnerContainer;
    }

    @Override
    protected ViewGroup.LayoutParams getInnerViewLayoutParams() {
        if (mExtraParams != null) {
            RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(
                    mExtraParams.getInt(INNER_EXTRA_WIDTH),
                    mExtraParams.getInt(INNER_EXTRA_HEIGHT));
            relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            relativeLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            return relativeLayoutParams;
        }
        return new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public AdformEnum.State getDefaultState() {
        return AdformEnum.State.EXPANDED;
    }

    @Override
    public AdformEnum.PlacementType getDefaultPlacementType() {
        return AdformEnum.PlacementType.INLINE;
    }

    private AlphaAnimation createAlphaAnimation(float from, float to) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(from, to);
        alphaAnimation.setDuration(DEFAULT_FADE_DURATION);
        return alphaAnimation;
    }

    @Override
    public void onContentRender() {
        if (mExpandType == AdformEnum.ExpandType.TWO_PART &&
                !mAdformAnimationManager.isAnimating()) {
            mAdformAnimationManager.turnOn();
        }
        if (mListener != null)
            mListener.onAdShown();
    }

    @Override
    public void onContentRestore(boolean state) {}

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mExpandType == AdformEnum.ExpandType.ONE_PART &&
                !mAdformAnimationManager.isAnimating()) {
            mAdformAnimationManager.turnOn();
        }
    }

    @Override
    public void onMraidClose() {
        if (!mAdformAnimationManager.isAnimating())
            mAdformAnimationManager.turnOff();
    }

    @Override
    public void onSliderFinishedHiding() {
        if (mListener != null)
            mListener.onAdClose();
    }

    @Override
    public void onSliderFinishedShowing() {
        mAdformAnimationManager.setOpen(true);
        getInnerView().setVisibility(View.VISIBLE);
    }

    @Override
    public void onSliderStartedHiding() {
        mDimmingView.setVisibility(View.VISIBLE);
        mDimmingView.startAnimation(mFadeOutAnimation);
    }

    @Override
    public void onSliderStartedShowing() {
        mDimmingView.setVisibility(View.VISIBLE);
        mDimmingView.startAnimation(mFadeInAnimation);
    }

    @Override
    public void onMraidUseCustomClose(boolean shouldUseCustomClose) {
        // Nothing to respond to here anymore
    }

    @Override
    public void onMraidExpand(String url, ExpandProperties expandProperties) {
        // Nothing to do here
    }

    @Override
    public void destroy() {
        if (mAdformAnimationManager != null)
            mAdformAnimationManager.destroy();
        mAdformAnimationManager = null;
        super.destroy();
    }
}
