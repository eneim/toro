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

package im.ene.lab.toro.player.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;
import android.view.View;

/**
 * Created by eneim on 6/16/16.
 */
public final class DisplayHelper {

  private static final String TAG = "DisplayHelper";

  private static IDisplayHelper IMPL;

  static {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
        && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
      IMPL = new HelperV16();
    } else if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      IMPL = new HelperV19();
    } else {
      IMPL = new HelperV23();
    }
  }

  // Call before setContentView
  interface IDisplayHelper {

    /**
     * Setup full screen for current Activity. Helpful for Video Player, when User changes from
     * Portrait to Landscape and vice versa. Should be called in {@link
     * Activity#onConfigurationChanged(Configuration)}
     *
     * NOTE: Activity must use {@code android:configChanges="orientation|screenSize|keyboardHidden"}
     * in Manifest.
     */
    void setFullScreen(Activity activity, boolean isFullScreen);
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN) private static class HelperV16
      implements IDisplayHelper {

    @Override public void setFullScreen(Activity activity, boolean isFullScreen) {
      // The UI options currently enabled are represented by a bitfield.
      // getSystemUiVisibility() gives us that bitfield.
      int newUiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();

      // Navigation bar hiding:  Backwards compatible to ICS.
      newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

      // Status bar hiding: Backwards compatible to Jellybean
      newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;

      activity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }
  }

  @TargetApi(Build.VERSION_CODES.KITKAT) private static class HelperV19 extends HelperV16 {

    @Override public void setFullScreen(Activity activity, boolean isFullScreen) {
      // The UI options currently enabled are represented by a bitfield.
      // getSystemUiVisibility() gives us that bitfield.
      int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
      int newUiOptions = uiOptions;
      boolean isImmersiveModeEnabled =
          ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
      if (isImmersiveModeEnabled) {
        Log.i(TAG, "Turning immersive mode mode off. ");
      } else {
        Log.i(TAG, "Turning immersive mode mode on.");
      }

      // Navigation bar hiding:  Backwards compatible to ICS.
      if (Build.VERSION.SDK_INT >= 14) {
        newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
      }

      // Status bar hiding: Backwards compatible to Jellybean
      if (Build.VERSION.SDK_INT >= 16) {
        newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
      }

      // Immersive mode: Backward compatible to KitKat.
      // Note that this flag doesn't do anything by itself, it only augments the behavior
      // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
      // all three flags are being toggled together.
      // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
      // Sticky immersive mode differs in that it makes the navigation and status bars
      // semi-transparent, and the UI flag does not get cleared when the user interacts with
      // the screen.
      if (Build.VERSION.SDK_INT >= 18) {
        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
      }

      activity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }
  }

  @TargetApi(Build.VERSION_CODES.KITKAT) private static class HelperV23 extends HelperV19 {

  }

  public static void setFullScreen(Activity activity, boolean isFullScreen) {
    IMPL.setFullScreen(activity, isFullScreen);
  }
}
