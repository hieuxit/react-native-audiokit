//  The MIT License (MIT)

//  Copyright (c) 2018 Intuz Pvt Ltd.

//  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
//  (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
//  merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:

//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
//  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
//  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
//  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.reactnativeaudiokit.utils;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;

import java.io.File;
import java.io.FileNotFoundException;

public class Utility {

    //audio format in which file after trim will be saved.
    public static final String AUDIO_FORMAT = ".wav";

    //audio mime type in which file after trim will be saved.
    public static final String AUDIO_MIME_TYPE = "audio/wav";

    public static long getCurrentTime() {
        return System.nanoTime() / 1000000;
    }

    public static int getDuration(File file) {
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
            String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            int millSecond = Integer.parseInt(durationStr);
            return millSecond / 1000;
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        try {
            MediaExtractor extractor = new MediaExtractor();
            MediaFormat format = null;
            int i;

            extractor.setDataSource(file.getPath());
            int numTracks = extractor.getTrackCount();
            // find and select the first audio track present in the file.
            for (i = 0; i < numTracks; i++) {
                format = extractor.getTrackFormat(i);
                if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                    extractor.selectTrack(i);
                    break;
                }
            }
            if (i == numTracks) {
                throw new FileNotFoundException("No audio track found in " + file.getPath());
            }
            // Expected total number of samples per channel.
            return (int) (format.getLong(MediaFormat.KEY_DURATION) / 1000000.f);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return 0;
    }

}
