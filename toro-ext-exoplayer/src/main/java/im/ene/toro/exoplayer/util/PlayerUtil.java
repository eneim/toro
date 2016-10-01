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

package im.ene.toro.exoplayer.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import com.google.android.exoplayer.util.Util;
import im.ene.toro.exoplayer.ExoVideo;

/**
 * Contains utility functions which are used by a number of other classes.
 */
public class PlayerUtil {

  public static ExoVideo.Type inferVideoType(Uri uri) {
    int type = Util.inferContentType(uri.getLastPathSegment());
    switch (type) {
      case Util.TYPE_DASH:
        return ExoVideo.Type.DASH;
      case Util.TYPE_HLS:
        return ExoVideo.Type.HLS;
      case Util.TYPE_SS:
        return ExoVideo.Type.SS;
      case Util.TYPE_OTHER:
      default:
        return ExoVideo.Type.OTHER;
    }
  }

  private static final ScreenHelper IMPL;

  static {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Will be true obviously.
        && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
      IMPL = new ScreenHelperV16();
    } else if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      IMPL = new ScreenHelperV19();
    } else {
      IMPL = new ScreenHelperV23();
    }
  }

  private interface ScreenHelper {

    void setFullScreen(Activity activity, boolean isFullScreen);
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN) private static class ScreenHelperV16
      implements ScreenHelper {

    @Override public void setFullScreen(Activity activity, boolean isFullScreen) {
      // TODO Implement this
    }
  }

  @TargetApi(Build.VERSION_CODES.KITKAT) private static class ScreenHelperV19
      extends ScreenHelperV16 {

    @Override public void setFullScreen(Activity activity, boolean isFullScreen) {
      super.setFullScreen(activity, isFullScreen);
      // TODO Implement this
    }
  }

  @TargetApi(Build.VERSION_CODES.KITKAT) private static class ScreenHelperV23
      extends ScreenHelperV19 {

    @Override public void setFullScreen(Activity activity, boolean isFullScreen) {
      super.setFullScreen(activity, isFullScreen);
      // TODO Implement this
    }
  }

  /**
   * Setup full screen for current Activity. Helpful for Video Player, when User changes from
   * Portrait to Landscape and vice versa. Should be called in {@link
   * Activity#onConfigurationChanged(Configuration)}
   *
   * NOTE: Activity must use {@code android:configChanges="orientation|screenSize|keyboardHidden"}
   * in Manifest.
   *
   * @param activity Activity which is being set up.
   * @param isFullScreen {@code true} to Set current Activity to Fullscreen, {@code false} to set
   * current Activity to normal state.
   */
  public static void setFullScreen(Activity activity, boolean isFullScreen) {
    IMPL.setFullScreen(activity, isFullScreen);
  }
}
