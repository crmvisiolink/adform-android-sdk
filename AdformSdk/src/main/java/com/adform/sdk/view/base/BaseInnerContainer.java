package com.adform.sdk.view.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.*;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.adform.sdk.mraid.MraidWebViewClient;
import com.adform.sdk.resources.MraidJavascript;
import com.adform.sdk.utils.*;
import com.adform.sdk.view.inner.AdWebView;
import com.adform.sdk.view.CoreAdView;

import java.util.HashMap;

/**
 * Created by mariusm on 21/05/14.
 * Base view that is showing content in webview.
 * It handles content loading, saving, restoring, mraid functions.
 */
public abstract class BaseInnerContainer extends RelativeLayout implements JsLoadBridge.LoadBridgeHandler {
    public static final String MRAID_JS_INTERFACE = "mraid";

    /**
     * A callback interface for main container
     */
    public interface BaseAdViewListener {
        /**
         * An indicator that is called when content is restored from instance
         * @param state provided restore state. If false, content is null
         */
        public void onContentRestore(boolean state);

        /**
         * An indicator that is called when content is rendered for the first time.
         * This is needed for, that first time whole container is drawn with animation,
         * later on a mViewAnimator is used for inner animations.
         */
        public void onContentRender();
        public void setAnimating(boolean isAnimating);
    }
    /** Global variable, indicating that content is being restored. */
    private BaseAdViewListener mBaseListener;
    protected Context mContext = null;
    private String mLoadedContent;
    private WebViewClient mSimpleWebViewClient;
    private WebViewClient mMraidWebViewClient;
    private String mUserAgent;
    private boolean mIsRestoring = false;
    private int mTimesLoaded = 0;
    private JsLoadBridge mLoadBridge;
    private ImageView mViewCache;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private boolean mIsMraidReady = false;
    private HashMap<String, Boolean> mConfigurationPreset;
    private boolean mIsLoadedContentMraid = false;
    private MraidBridge mMraidBridge;

    public BaseInnerContainer(Context context) {
        this(context, null);
    }

    public BaseInnerContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseInnerContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;

        // Compability issues
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // Initializing fake ImageView to remove flicker when restoring content
        mViewCache = new ImageView(context);
        mViewCache.setLayoutParams(new RelativeLayout.LayoutParams(
                (int) (Utils.getWidthDeviceType(mContext) * CoreAdView.sDeviceDensity + 0.5f),
                (int) (Utils.getHeightDeviceType(mContext) * CoreAdView.sDeviceDensity + 0.5f)));
        addView(mViewCache);
        mViewCache.setVisibility(GONE);
        mViewCache.bringToFront();

        // Base parameters
        setBackgroundColor(Color.TRANSPARENT);
        mMraidBridge = new MraidBridge();
        initView();
    }

    /**
     * Initializes view that should be added to the main container
     */
    protected abstract void initView();

    /**
     * @return currently active WebView
     */
    protected abstract AdWebView getCurrentWebView();

    /**
     * @return WebView that should content be loaded to
     */
    protected abstract AdWebView getWebViewToLoadContentTo();
    /**
     * If any animation is needed, this methods is called.
     * Method is called when content is ready to be displayed, and all html is ready.
     */
    protected abstract void animateAdShowing();

    /**
     * Creates web view and returns its instance. Inside all needed clients and variables are binded.
     *
     * @param context provided context
     * @return initialized web view
     */
    protected AdWebView createWebView(final Context context) {
        final AdWebView webView = new AdWebView(context);

        if (mSimpleWebViewClient == null)
            mSimpleWebViewClient = new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(final WebView view,
                                                        final String url) {
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                }
            };

        webView.setWebViewClient(mSimpleWebViewClient);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
                Utils.p("JS(onJsBeforeUnload):"+message);
                return super.onJsBeforeUnload(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                Utils.p("JS(onJsConfirm):"+message);
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                Utils.p("JS(onJsPrompt):"+message);
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Utils.p("JS(onJsAlert):"+message);
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                Utils.p("JS(onConsoleMessage):"+message);
                super.onConsoleMessage(message, lineNumber, sourceID);
            }
        });
        if (mUserAgent == null)
            mUserAgent = webView.getSettings().getUserAgentString();
        return webView;
    }


    /**
     * Renders content in next in list webview
     * If content is null it resets loading content.
     * @see #showContent(String, boolean, boolean)
     */
    public void showContent(String content) {
        showContent(content, mIsLoadedContentMraid);
    }    /**
     * Renders content in next in list webview
     * If content is null it resets loading content.
     * @see #showContent(String, boolean, boolean)
     */
    public void showContent(String content, boolean isMraid) {
        showContent(content, isMraid, false);
    }

    /**
     * Renders content in next in list webview
     * If content is null it resets loading content.
     * This sets an additional setting for if content is being restored.
     *
     * @param content provided content to load
     * @param isMraid true if content is mraid
     * @param isRestoring This flag only can be set from inside.
     *                    By default, from outside this flag will always be false
     */
    protected void showContent(String content, boolean isMraid, boolean isRestoring) {
        if (!isRestoring) {
            mIsRestoring = false;
            post(mClearCacheRunnable);
        }
        if (content == null) {
            mLoadedContent = null;
            mIsLoadedContentMraid = false;
            return;
        } else {
            mLoadedContent = content;
            mIsLoadedContentMraid = isMraid;
        }
        resetConfigurationPreset();
        mIsMraidReady = false;
        // Wrapping js in js tags
        content = "<script type=\"text/javascript\">" + content + "</script>";

        // Injecting mraid script if needed
        String jsInjectionWrapper = ((isMraid) ? "<script>"+ MraidJavascript.JAVASCRIPT_SOURCE+"</script>" : "");
        content = "<html><head>"
                + JsLoadBridge.NATIVE_JS_CALLBACK_HEADER
                + jsInjectionWrapper
                + "</head>"
                + "<body"+JsLoadBridge.NATIVE_JS_CALLBACK_BODY_ONLOAD+">"
                + content
                + "</body></html>";

        AdWebView webView = getWebViewToLoadContentTo();
        if (webView != null) {
            webView.setWebViewClient((mIsLoadedContentMraid) ? getMraidWebViewClient() : getSimpleWebViewClient());
            webView.setMraidCallbackListener(mMraidBridge);
            webView.addJavascriptInterface(this, MRAID_JS_INTERFACE);
            if (mLoadBridge == null)
                mLoadBridge = new JsLoadBridge(this);
            mLoadBridge.setWebView(webView);
            mMraidBridge.setWebView(webView);
            mMraidBridge.onPlacementTypeChange(getPlacementType());
            mMraidBridge.onStateChange(AdformEnum.State.LOADING);
            webView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null);
        }
    }

    @Override
    public void onContentLoadedFromJsBridge() {
        mMraidBridge.onStateChange(AdformEnum.State.DEFAULT);
        if (mIsLoadedContentMraid) {
            mMraidBridge.forceSettingUpdate();
        }

        if (!mIsRestoring) {
            if (mBaseListener != null)
                mBaseListener.onContentRender();
            animateAdShowing();
        } else {
            postDelayed(mClearCacheRunnable, 200);
            mIsRestoring = false;
        }
        setTimesLoaded(mTimesLoaded + 1);
    }

    @Override
    public void onConfigurationPreset(String configuredParam) {
        if (!mIsMraidReady) {
            mConfigurationPreset.put(configuredParam, true);
            if (mIsLoadedContentMraid && isConfigurationPresetReady()) {
                mIsMraidReady = true;
                post(new Runnable() {
                    @Override
                    public void run() {
                        getCurrentWebView().fireReady();
                    }
                });
            }
        }
    }

    @Override
    public void onNativePrint(String nativeCall) {
        Utils.p("JS Console: " + nativeCall);
    }

    /**
     * Returns if all configurations are set for the loaded javascript.
     *
     * @see #onConfigurationPreset(String) when configurations are set from outer source, javascript callbacks function
     * @return true if all configurations are ready.
     */
    private boolean isConfigurationPresetReady() {
        for (Boolean isReady : mConfigurationPreset.values()) {
            if (!isReady)
                return false;
        }
        return true;
    }

    /**
     * Resets and reinitializes a dictionary that holds which properties has been loaded.
     * @see #onConfigurationPreset(String) sets each property for a change
     * @see #isConfigurationPresetReady() check if configurations are all set
     */
    private void resetConfigurationPreset(){
        if (mConfigurationPreset == null)
            mConfigurationPreset = new HashMap<String, Boolean>();
        mConfigurationPreset.put("screenSize", false);
        mConfigurationPreset.put("maxSize", false);
        mConfigurationPreset.put("defaultPosition", false);
        mConfigurationPreset.put("currentPosition", false);
        mConfigurationPreset.put("state", false);
        mConfigurationPreset.put("placementType", false);
    }

    // This defines orientation for the ad view

    /**
     * @return ad placeent type
     */
    public abstract AdformEnum.PlacementType getPlacementType();


    // -------------------------
    // Instance saving/restoring
    // -------------------------
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        removeCallbacks(mClearCacheRunnable);
        savedState.loadedContent = mLoadedContent;
        savedState.isLoadedContentMraid = mIsLoadedContentMraid;
        savedState.screenShot = mBitmap;
        savedState.timesLoaded = mTimesLoaded;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setTimesLoaded(savedState.timesLoaded);
        mBitmap = savedState.screenShot;
        mViewCache.setImageBitmap(mBitmap);
        mViewCache.setVisibility(VISIBLE);
        mIsRestoring = true;
        mLoadedContent = savedState.loadedContent;
        mIsLoadedContentMraid = savedState.isLoadedContentMraid;
        if (mLoadedContent != null && mLoadedContent.length() > 0) {
            if (mBaseListener != null)
                mBaseListener.onContentRestore(true);
            showContent(mLoadedContent, mIsLoadedContentMraid, true);
        } else {
            if (mBaseListener != null)
                mBaseListener.onContentRestore(false);
        }
    }

    private static class SavedState extends BaseSavedState {
        String loadedContent;
        boolean isLoadedContentMraid;
        Bitmap screenShot;
        int timesLoaded;

        public SavedState(Parcel source) {
            super(source);
            if (source.readInt() == 1)
                loadedContent = source.readString();
            if (source.readInt() == 1)
                screenShot = source.readParcelable(Bitmap.class.getClassLoader());
            isLoadedContentMraid = (source.readInt() == 1);
            timesLoaded = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt((loadedContent != null) ? 1 : 0);
            if (loadedContent != null)
                dest.writeString(loadedContent);
            dest.writeInt((screenShot != null) ? 1 : 0);
            if (screenShot != null)
                dest.writeParcelable(screenShot, 0);
            dest.writeInt((isLoadedContentMraid) ? 1 : 0);
            dest.writeInt(timesLoaded);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    // -----------------
    // Getters / Setters
    // -----------------
    public void setBaseListener(BaseAdViewListener listener) {
        this.mBaseListener = listener;
    }

    public BaseAdViewListener getBaseListener() {
        return mBaseListener;
    }

    public void setTimesLoaded(int timesLoaded) {
        this.mTimesLoaded = timesLoaded;
    }

    public int getTimesLoaded() {
        return mTimesLoaded;
    }

    public String getUserAgent() {
        return mUserAgent;
    }

    public WebViewClient getSimpleWebViewClient() {
        return mSimpleWebViewClient;
    }

    public WebViewClient getMraidWebViewClient() {
        // Lazy instantiation for mraid type of client
        if (mMraidWebViewClient == null)
            mMraidWebViewClient = new MraidWebViewClient();
        return mMraidWebViewClient;
    }

    protected String getLoadedContent() {
        return mLoadedContent;
    }

    protected void setLoadedContent(String loadedContent) {
        this.mLoadedContent = loadedContent;
    }

    protected boolean isRestoringContent() {
        return mIsRestoring;
    }

    public MraidBridge getMraidBridge() {
        return mMraidBridge;
    }

    // ---------
    // Runnables
    // ---------
    private Runnable mClearCacheRunnable = new Runnable() {
        @Override
        public void run() {
            mViewCache.setImageBitmap(null);
            mViewCache.setVisibility(GONE);
            //TODO mariusm 14/05/14 Bitmaps should be recycled, but it causes trouble at the moment when quickly switching view instances
//            if (mBitmap != null && !mBitmap.isRecycled())
//                mBitmap.recycle();
            mBitmap = null;
            mCanvas = null;
        }
    };

}
