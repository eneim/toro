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
import android.support.annotation.Nullable;
import android.view.View;
import im.ene.toro.widget.Container;

/**
 * @author eneim | 5/31/17.
 */

public final class ToroUtil {

  private ToroUtil() {
    throw new RuntimeException("Meh!");
  }

  private static Rect getVideoRect(View playerView) {
    Rect rect = new Rect();
    Point offset = new Point();
    playerView.getGlobalVisibleRect(rect, offset);
    return rect;
  }

  @Nullable private static Rect getRecyclerViewRect(@NonNull Container parent) {
    Rect rect = new Rect();
    Point offset = new Point();
    parent.getGlobalVisibleRect(rect, offset);
    return rect;
  }

  public static float visibleAreaOffset(View playerView, Container parent) {
    Rect videoRect = getVideoRect(playerView);
    Rect parentRect = getRecyclerViewRect(parent);

    if (parentRect != null && (parentRect.contains(videoRect) || parentRect.intersect(videoRect))) {
      int visibleArea = videoRect.height() * videoRect.width();
      int viewArea = playerView.getWidth() * playerView.getHeight();
      return viewArea <= 0.f ? 1.f : visibleArea / (float) viewArea;
    } else {
      return 0.f;
    }
  }
}
