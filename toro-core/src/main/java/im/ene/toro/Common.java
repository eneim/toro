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

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.view.View;
import im.ene.toro.widget.Container;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author eneim | 6/2/17.
 */

public final class Common {

  @SuppressWarnings("WeakerAccess") static int compare(int x, int y) {
    return (x < y) ? -1 : ((x == y) ? 0 : 1);
  }

  static long max(Long... numbers) {
    List<Long> list = Arrays.asList(numbers);
    return Collections.<Long>max(list);
  }

  static long min(Long... numbers) {
    List<Long> list = Arrays.asList(numbers);
    return Collections.<Long>min(list);
  }

  static Comparator<ToroPlayer> ORDER_COMPARATOR = new Comparator<ToroPlayer>() {
    @Override public int compare(ToroPlayer o1, ToroPlayer o2) {
      return Common.compare(o1.getPlayerOrder(), o2.getPlayerOrder());
    }
  };

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public static boolean allowsToPlay(@NonNull View videoView, @NonNull Container parent) {
    Rect windowRect = new Rect();
    Rect parentRect = new Rect();
    // 1. Get Window's vision from parent
    parent.getWindowVisibleDisplayFrame(windowRect);
    // 2. Get parent's global rect
    parent.getGlobalVisibleRect(parentRect, null);
    // 3. Get player global rect
    Rect videoRect = new Rect();
    // Headache !!!
    int[] screenLoc = new int[2];
    videoView.getLocationOnScreen(screenLoc);
    videoRect.left += screenLoc[0];
    videoRect.right += screenLoc[0] + videoView.getWidth();
    videoRect.top += screenLoc[1];
    videoRect.bottom += screenLoc[1] + videoView.getHeight();

    // Condition: window manages parent, and parent manages Video or parent intersects Video
    return windowRect.contains(parentRect) && (parentRect.contains(videoRect)
        || parentRect.intersect(videoRect));
  }
}
