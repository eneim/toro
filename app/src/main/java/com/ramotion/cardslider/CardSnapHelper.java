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

/*

The MIT License (MIT)

Copyright (c) 2017 Ramotion

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial
portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

package com.ramotion.cardslider;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import java.security.InvalidParameterException;

/**
 * Extended {@link LinearSnapHelper} that works <b>only</b> with {@link CardSliderLayoutManager}.
 */
public class CardSnapHelper extends LinearSnapHelper {

  private RecyclerView recyclerView;

  /**
   * Attaches the {@link CardSnapHelper} to the provided RecyclerView, by calling
   * {@link RecyclerView#setOnFlingListener(RecyclerView.OnFlingListener)}.
   * You can call this method with {@code null} to detach it from the current RecyclerView.
   *
   * @param recyclerView The RecyclerView instance to which you want to add this helper or
   * {@code null} if you want to remove SnapHelper from the current
   * RecyclerView.
   * @throws IllegalArgumentException if there is already a {@link RecyclerView.OnFlingListener}
   * attached to the provided {@link RecyclerView}.
   * @throws InvalidParameterException if provided RecyclerView has LayoutManager which is not
   * instance of CardSliderLayoutManager
   */
  @Override public void attachToRecyclerView(@Nullable RecyclerView recyclerView)
      throws IllegalStateException {
    super.attachToRecyclerView(recyclerView);

    if (recyclerView != null
        && !(recyclerView.getLayoutManager() instanceof CardSliderLayoutManager)) {
      throw new InvalidParameterException(
          "LayoutManager must be instance of CardSliderLayoutManager");
    }

    this.recyclerView = recyclerView;
  }

  @Override
  public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX,
      int velocityY) {
    final CardSliderLayoutManager lm = (CardSliderLayoutManager) layoutManager;

    final int itemCount = lm.getItemCount();
    if (itemCount == 0) {
      return RecyclerView.NO_POSITION;
    }

    final RecyclerView.SmoothScroller.ScrollVectorProvider vectorProvider =
        (RecyclerView.SmoothScroller.ScrollVectorProvider) layoutManager;

    final PointF vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1);
    if (vectorForEnd == null) {
      return RecyclerView.NO_POSITION;
    }

    final int distance = calculateScrollDistance(velocityX, velocityY)[0];
    int deltaJump;

    if (distance > 0) {
      deltaJump = (int) Math.floor(distance / lm.getCardWidth());
    } else {
      deltaJump = (int) Math.ceil(distance / lm.getCardWidth());
    }

    final int deltaSign = Integer.signum(deltaJump);
    deltaJump = deltaSign * Math.min(3, Math.abs(deltaJump));

    if (vectorForEnd.x < 0) {
      deltaJump = -deltaJump;
    }

    if (deltaJump == 0) {
      return RecyclerView.NO_POSITION;
    }

    final int currentPosition = lm.getActiveCardPosition();
    if (currentPosition == RecyclerView.NO_POSITION) {
      return RecyclerView.NO_POSITION;
    }

    int targetPos = currentPosition + deltaJump;
    if (targetPos < 0 || targetPos >= itemCount) {
      targetPos = RecyclerView.NO_POSITION;
    }

    return targetPos;
  }

  @Override public View findSnapView(RecyclerView.LayoutManager layoutManager) {
    return ((CardSliderLayoutManager) layoutManager).getTopView();
  }

  @Override
  public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager,
      @NonNull View targetView) {
    final CardSliderLayoutManager lm = (CardSliderLayoutManager) layoutManager;
    final int viewLeft = lm.getDecoratedLeft(targetView);
    final int activeCardLeft = lm.getActiveCardLeft();
    final int activeCardCenter = lm.getActiveCardLeft() + lm.getCardWidth() / 2;
    final int activeCardRight = lm.getActiveCardLeft() + lm.getCardWidth();

    int[] out = new int[] { 0, 0 };
    if (viewLeft < activeCardCenter) {
      final int targetPos = lm.getPosition(targetView);
      final int activeCardPos = lm.getActiveCardPosition();
      if (targetPos != activeCardPos) {
        out[0] = -(activeCardPos - targetPos) * lm.getCardWidth();
      } else {
        out[0] = viewLeft - activeCardLeft;
      }
    } else {
      out[0] = viewLeft - activeCardRight + 1;
    }

    if (out[0] != 0) {
      recyclerView.smoothScrollBy(out[0], 0, new AccelerateInterpolator());
    }

    return new int[] { 0, 0 };
  }

  @Nullable @Override
  protected LinearSmoothScroller createSnapScroller(RecyclerView.LayoutManager layoutManager) {
    return ((CardSliderLayoutManager) layoutManager).getSmoothScroller(recyclerView);
  }
}
