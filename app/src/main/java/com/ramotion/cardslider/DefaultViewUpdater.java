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

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Default implementation of {@link ViewUpdater}
 */
@SuppressWarnings("WeakerAccess") public class DefaultViewUpdater extends ViewUpdater {

  private static final float SCALE_LEFT = 0.65f;
  private static final float SCALE_CENTER = 0.95f;
  private static final float SCALE_RIGHT = 0.8f;
  private static final float SCALE_CENTER_TO_LEFT = SCALE_CENTER - SCALE_LEFT;
  private static final float SCALE_CENTER_TO_RIGHT = SCALE_CENTER - SCALE_RIGHT;

  private static final int Z_CENTER_1 = 12;
  private static final int Z_CENTER_2 = 16;
  private static final int Z_RIGHT = 8;

  private int cardWidth;
  private int activeCardLeft;
  private int activeCardRight;
  private int activeCardCenter;
  private float cardsGap;

  private int transitionEnd;
  private int transitionDistance;
  private float transitionRight2Center;

  public DefaultViewUpdater(CardSliderLayoutManager layoutManager) {
    super(layoutManager);
  }

  @Override public void onLayoutManagerInitialized() {
    this.cardWidth = layoutManager.getCardWidth();
    this.activeCardLeft = layoutManager.getActiveCardLeft();
    this.activeCardRight = layoutManager.getActiveCardRight();
    this.activeCardCenter = layoutManager.getActiveCardCenter();
    this.cardsGap = layoutManager.getCardsGap();

    this.transitionEnd = activeCardCenter;
    this.transitionDistance = activeCardRight - transitionEnd;

    final float centerBorder = (cardWidth - cardWidth * SCALE_CENTER) / 2f;
    final float rightBorder = (cardWidth - cardWidth * SCALE_RIGHT) / 2f;
    final float right2centerDistance =
        (activeCardRight + centerBorder) - (activeCardRight - rightBorder);
    this.transitionRight2Center = right2centerDistance - cardsGap;
  }

  @Override public int getActiveCardPosition() {
    int result = RecyclerView.NO_POSITION;

    View biggestView = null;
    float lastScaleX = 0f;

    for (int i = 0, cnt = layoutManager.getChildCount(); i < cnt; i++) {
      final View child = layoutManager.getChildAt(i);
      final int viewLeft = layoutManager.getDecoratedLeft(child);
      if (viewLeft >= activeCardRight) {
        continue;
      }

      final float scaleX = child.getScaleX();
      if (lastScaleX < scaleX && viewLeft < activeCardCenter) {
        lastScaleX = scaleX;
        biggestView = child;
      }
    }

    if (biggestView != null) {
      result = layoutManager.getPosition(biggestView);
    }

    return result;
  }

  @Nullable @Override public View getTopView() {
    if (layoutManager.getChildCount() == 0) {
      return null;
    }

    View result = null;
    float lastValue = cardWidth;

    for (int i = 0, cnt = layoutManager.getChildCount(); i < cnt; i++) {
      final View child = layoutManager.getChildAt(i);
      if (layoutManager.getDecoratedLeft(child) > activeCardRight) {
        continue;
      }

      final int viewLeft = layoutManager.getDecoratedLeft(child);
      final int diff = activeCardRight - viewLeft;
      if (diff < lastValue) {
        lastValue = diff;
        result = child;
      }
    }

    return result;
  }

  @Override public void updateView() {
    View prevView = null;

    for (int i = 0, cnt = layoutManager.getChildCount(); i < cnt; i++) {
      final View view = layoutManager.getChildAt(i);
      final int viewLeft = layoutManager.getDecoratedLeft(view);

      final float scale;
      final float alpha;
      final float z;
      final float x;

      if (viewLeft < activeCardLeft) {
        final float ratio = (float) viewLeft / activeCardLeft;
        scale = SCALE_LEFT + SCALE_CENTER_TO_LEFT * ratio;
        alpha = 0.1f + ratio;
        z = Z_CENTER_1 * ratio;
        x = 0;
      } else if (viewLeft < activeCardCenter) {
        scale = SCALE_CENTER;
        alpha = 1;
        z = Z_CENTER_1;
        x = 0;
      } else if (viewLeft < activeCardRight) {
        final float ratio =
            (float) (viewLeft - activeCardCenter) / (activeCardRight - activeCardCenter);
        scale = SCALE_CENTER - SCALE_CENTER_TO_RIGHT * ratio;
        alpha = 1;
        z = Z_CENTER_2;
        x = -Math.min(transitionRight2Center,
            transitionRight2Center * (viewLeft - transitionEnd) / transitionDistance);
      } else {
        scale = SCALE_RIGHT;
        alpha = 1;
        z = Z_RIGHT;

        if (prevView != null) {
          final float prevViewScale;
          final float prevTransition;
          final int prevRight;

          final boolean isFirstRight = layoutManager.getDecoratedRight(prevView) <= activeCardRight;
          if (isFirstRight) {
            prevViewScale = SCALE_CENTER;
            prevRight = activeCardRight;
            prevTransition = 0;
          } else {
            prevViewScale = prevView.getScaleX();
            prevRight = layoutManager.getDecoratedRight(prevView);
            prevTransition = prevView.getTranslationX();
          }

          final float prevBorder = (cardWidth - cardWidth * prevViewScale) / 2;
          final float currentBorder = (cardWidth - cardWidth * SCALE_RIGHT) / 2;
          final float distance =
              (viewLeft + currentBorder) - (prevRight - prevBorder + prevTransition);

          final float transition = distance - cardsGap;
          x = -transition;
        } else {
          x = 0;
        }
      }

      onUpdateViewScale(view, scale);
      onUpdateViewTransitionX(view, x);
      onUpdateViewZ(view, z);
      onUpdateViewAlpha(view, alpha);

      prevView = view;
    }
  }

  protected void onUpdateViewAlpha(@NonNull View view, float alpha) {
    if (view.getAlpha() != alpha) {
      view.setAlpha(alpha);
    }
  }

  protected void onUpdateViewScale(@NonNull View view, float scale) {
    if (view.getScaleX() != scale) {
      view.setScaleX(scale);
      view.setScaleY(scale);
    }
  }

  protected void onUpdateViewZ(@NonNull View view, float z) {
    if (ViewCompat.getZ(view) != z) {
      ViewCompat.setZ(view, z);
    }
  }

  protected void onUpdateViewTransitionX(@NonNull View view, float x) {
    if (view.getTranslationX() != x) {
      view.setTranslationX(x);
    }
  }
}
