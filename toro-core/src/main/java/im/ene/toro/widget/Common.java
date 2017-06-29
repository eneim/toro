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

package im.ene.toro.widget;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.view.View;
import im.ene.toro.ToroPlayer;
import java.util.Comparator;

/**
 * @author eneim | 6/2/17.
 *
 *         A hub for internal convenient methods.
 */

@SuppressWarnings({ "unused", "WeakerAccess" }) //
final class Common {

  private static final String TAG = "ToroLib:Common";

  static int compare(int x, int y) {
    return (x < y) ? -1 : ((x == y) ? 0 : 1);
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY) //
  static Comparator<ToroPlayer> ORDER_COMPARATOR = new Comparator<ToroPlayer>() {
    @Override public int compare(ToroPlayer o1, ToroPlayer o2) {
      return Common.compare(o1.getPlayerOrder(), o2.getPlayerOrder());
    }
  };

  @RestrictTo(RestrictTo.Scope.LIBRARY) //
  static Comparator<ToroPlayer> ORDER_COMPARATOR_REVERSE = new Comparator<ToroPlayer>() {
    @Override public int compare(ToroPlayer o1, ToroPlayer o2) {
      return Common.compare(o2.getPlayerOrder(), o1.getPlayerOrder());
    }
  };

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  static boolean allowsToPlay(@NonNull View videoView, @NonNull Container parent) {
    Rect windowRect = new Rect();
    Rect parentRect = new Rect();
    // 1. Get Window's vision from parent
    parent.getWindowVisibleDisplayFrame(windowRect);
    int[] parentLoc = new int[2];
    parent.getLocationOnScreen(parentLoc);

    // 2. Get parent's global rect
    parent.getGlobalVisibleRect(parentRect);
    parentRect.offsetTo(parentLoc[0], parentLoc[1]);

    // 3. Get player global rect
    Rect videoRect = new Rect();
    int[] videoLoc = new int[2];
    videoView.getLocationOnScreen(videoLoc);
    videoView.getGlobalVisibleRect(videoRect);
    videoRect.offsetTo(videoLoc[0], videoLoc[1]);

    return (windowRect.contains(videoRect) || windowRect.intersect(videoRect))
        && (parentRect.contains(videoRect) || parentRect.intersect(videoRect));
  }
}
