package xyz.janficko.movienator;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

public class Movienator extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
