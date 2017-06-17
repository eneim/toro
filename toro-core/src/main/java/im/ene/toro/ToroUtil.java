/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package im.ene.toro;

import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

/**
 * @author eneim | 5/31/17.
 */

public final class ToroUtil {

  private static final String TAG = "ToroLib:Util";

  private ToroUtil() {
    throw new RuntimeException("Meh!");
  }

  private static Rect getViewRect(View view) {
    Rect rect = new Rect();
    Point offset = new Point();
    view.getGlobalVisibleRect(rect, offset);
    return rect;
  }

  public static float visibleAreaOffset(@NonNull View playerView, @NonNull View container) {
    Rect videoRect = getViewRect(playerView);
    Rect parentRect = getViewRect(container);

    float percent = 0.f;
    if (parentRect != null && (parentRect.contains(videoRect) || parentRect.intersect(videoRect))) {
      int visibleArea = videoRect.height() * videoRect.width();
      int viewArea = playerView.getWidth() * playerView.getHeight();
      percent = viewArea <= 0.f ? 1.f : visibleArea / (float) viewArea;
    }
    if (BuildConfig.DEBUG) Log.i(TAG, "visibleAreaOffset: " + percent);
    return percent;
  }
}
