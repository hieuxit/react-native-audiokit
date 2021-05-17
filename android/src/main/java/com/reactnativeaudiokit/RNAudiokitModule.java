
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RNAudiokitModule extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;
    private TrimmingTask asyncTask;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

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
        asyncTask.setExecutor(executorService);
        asyncTask.execute(file, (float) start, (float) end);
    }

    static class TrimmingTask extends AsyncTask<Object, Void, String> {

        private WeakReference<Promise> promiseRef;
        private WeakReference<Context> contextRef;
        private WeakReference<ExecutorService> executorRef;

        public TrimmingTask(Context context) {
            this.contextRef = new WeakReference<>(context);
        }

        private File input = null;

        @Override
        protected String doInBackground(Object... args) {

            Context context = contextRef.get();
            if (context == null) return "";

            try {
                input = (File) args[0];
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

            if (executorRef.get() != null && input != null && input.exists()) {
                executorRef.get().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            input.delete();
                        } catch (Exception ex) {
                            Log.e("RNAudioKit", "Delete file", ex);
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }

        public void setCallback(Promise promise) {
            this.promiseRef = new WeakReference<>(promise);
        }

        public void setExecutor(ExecutorService executor) {
            this.executorRef = new WeakReference<>(executor);
        }
    }

}
