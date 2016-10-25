/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.ene.toro.sample.util;

import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.util.TimeUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

/**
 * Created by eneim on 2/3/16.
 */
public class Util {

  public static String timeStamp(long position, long duration) {
    StringBuilder posTime = new StringBuilder();
    TimeUtils.formatDuration(position, posTime);
    StringBuilder durationTime = new StringBuilder();
    TimeUtils.formatDuration(duration, durationTime);

    return posTime + " / " + durationTime.toString();
  }

  public static File[] loadMovieFolder() throws FileNotFoundException {
    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        .listFiles(new FilenameFilter() {
          @Override public boolean accept(File dir, String filename) {
            return filename.endsWith(".mp4");
          }
        });
  }

  public static String genVideoId(@NonNull Uri videoUri, int playbackOrder, Object... manifest) {
    return genVideoId(videoUri.toString(), playbackOrder, manifest);
  }

  public static String genVideoId(@NonNull String videoUri, int playbackOrder, Object... manifest) {
    StringBuilder builder = new StringBuilder();
    builder.append(videoUri).append(":").append(playbackOrder);
    if (manifest != null && manifest.length > 0) {
      for (Object o : manifest) {
        builder.append(":").append(o.toString());
      }
    }

    return builder.toString();
  }
}
