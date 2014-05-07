package com.adform.sample.app;

import android.app.Application;
import com.joshdholtz.sentry.Sentry;

/**
 * Created by mariusm on 05/05/14.
 */
public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Sentry.init(getBaseContext(),
                "http://sentry.ito.lt",
                "http://aa87856ef768460aad2337eb93f0b2d0:7efe8759dd3a445193bca146b56fd0b8@sentry.ito.lt/24");
    }
}