package com.stanfy.enroscar.goro;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * Created by uniqa on 22.07.15.
 */
class MainThreadExecutor implements Executor {
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    public void execute(Runnable command) {
        mainThreadHandler.post(command);
    }
}
