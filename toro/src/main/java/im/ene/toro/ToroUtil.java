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

package im.ene.toro;

import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewParent;
import java.util.Arrays;
import java.util.List;

/**
 * Created by eneim on 2/1/16.
 */
public final class ToroUtil {

  private ToroUtil() {
    throw new AssertionError("Not supported");
  }

  @NonNull static Integer[] asArray(@NonNull int[] array) {
    Integer[] result = new Integer[array.length];
    if (array.length > 0) {
      for (int i = 0; i < array.length; i++) {
        result[i] = array[i];
      }
    }

    return result;
  }

  @NonNull static <T> List<T> asList(T[] array) {
    return Arrays.asList(array);
  }

  @NonNull static List<Integer> asList(int[] array) {
    return asList(asArray(array));
  }

  private static Rect getVideoRect(ToroPlayer player) {
    Rect rect = new Rect();
    Point offset = new Point();
    player.getPlayerView().getGlobalVisibleRect(rect, offset);
    return rect;
  }

  @Nullable private static Rect getRecyclerViewRect(ViewParent parent) {
    if (parent == null) { // view is not attached to RecyclerView parent
      return null;
    }

    if (!(parent instanceof View)) {
      return null;
    }

    Rect rect = new Rect();
    Point offset = new Point();
    ((View) parent).getGlobalVisibleRect(rect, offset);
    return rect;
  }

  @SuppressWarnings("ConstantConditions")
  public static float visibleAreaOffset(ToroPlayer player, ViewParent parent) {
    if (player.getPlayerView() == null) {
      throw new IllegalArgumentException("Player must have a valid VideoView.");
    }

    Rect videoRect = getVideoRect(player);
    Rect parentRect = getRecyclerViewRect(parent);

    if (parentRect != null && (parentRect.contains(videoRect) || parentRect.intersect(videoRect))) {
      float visibleArea = videoRect.height() * videoRect.width();
      float viewArea = player.getPlayerView().getWidth() * player.getPlayerView().getHeight();
      return viewArea <= 0.f ? 1.f : visibleArea / viewArea;
    } else {
      return 0.f;
    }
  }
}
