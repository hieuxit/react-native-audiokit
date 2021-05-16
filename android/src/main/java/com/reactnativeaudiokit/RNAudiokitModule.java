
package com.reactnativeaudiokit;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.reactnativeaudiokit.utils.Utility;
import com.reactnativeaudiokit.wav.SoundFile;

import java.io.File;
import java.lang.ref.WeakReference;


public class RNAudiokitModule extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;
    private TrimmingTask asyncTask;

    public RNAudiokitModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNAudioKit";
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
    }

    @ReactMethod
    public void getDuration(String fileName, Promise promise) {
        File file = new File(reactContext.getFilesDir(), fileName);
        int duration = 0;
        if (file.exists()) {
            duration = Utility.getDuration(file);
        }
        promise.resolve(String.valueOf(duration));
    }

    @ReactMethod
    public void trim(String fileName, int start, int end, final Promise promise) {
        File file = new File(reactContext.getFilesDir(), fileName);
        if (!file.exists()) {
            Log.e("hieuth", "File to trim does not exists!!! " + fileName);
            promise.resolve("");
            return;
        }

        if (asyncTask != null) {
            asyncTask.cancel(true);
        }

        asyncTask = new TrimmingTask(reactContext);
        asyncTask.setCallback(promise);

        asyncTask.execute(file, (float) start, (float) end);
    }

    static class TrimmingTask extends AsyncTask<Object, Void, String> {

        private WeakReference<Promise> promiseRef;
        private WeakReference<Context> contextRef;

        public TrimmingTask(Context context) {
            this.contextRef = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(Object... args) {

            Context context = contextRef.get();
            if (context == null) return "";

            try {
                File input = (File) args[0];
                float start = (float) args[1];
                float end = (float) args[2];

                String newFileName = System.currentTimeMillis() + ".wav";
                final SoundFile audio = SoundFile.create(input.getAbsolutePath(), null);
                File output = new File(context.getFilesDir(), newFileName);
                if (!output.exists()) output.createNewFile();
                audio.WriteWAVFile(output, start, end);
                return newFileName;
            } catch (Exception ex) {
                Log.e("hieuth", "Can not trim audio", ex);
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            Promise promise = promiseRef.get();
            if (promise != null) promise.resolve(s);
        }

        public void setCallback(Promise promise) {
            this.promiseRef = new WeakReference<>(promise);
        }
    }

}
