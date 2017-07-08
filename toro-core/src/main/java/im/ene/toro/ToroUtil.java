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
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewParent;
import im.ene.toro.widget.Container;

/**
 * @author eneim | 5/31/17.
 */

@SuppressWarnings("unused") public final class ToroUtil {

  private static final String TAG = "ToroLib:Util";

  private ToroUtil() {
    throw new RuntimeException("Meh!");
  }

  public static final String LIB_NAME = "ToroLib, v3.0.0";

  /**
   * Get the ratio in range of 0.0 ~ 1.0 of the visible area of a {@link ToroPlayer}'s playerView.
   *
   * @param player the {@link ToroPlayer} need to investigate.
   * @param parent the {@link ViewParent} that holds the {@link ToroPlayer}. If {@code null} or
   * not a {@link Container} then this method must returns 0.0f;
   * @return the ratio value in range of 0.0 ~ 1.0 of the visible area.
   */
  @FloatRange(from = 0.0, to = 1.0) //
  public static float visibleAreaOffset(@NonNull ToroPlayer player, @Nullable ViewParent parent) {
    if (parent == null || !(parent instanceof Container)) return 0.0f;

    View playerView = player.getPlayerView();
    Rect drawRect = new Rect();
    playerView.getDrawingRect(drawRect);
    int drawArea = drawRect.width() * drawRect.height();

    Rect videoRect = new Rect();
    boolean visible = playerView.getGlobalVisibleRect(videoRect, new Point());

    float offset = 0.f;
    if (visible && drawArea > 0) {
      int visibleArea = videoRect.height() * videoRect.width();
      offset = visibleArea / (float) drawArea;
    }
    return offset;
  }
}
