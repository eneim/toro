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

package im.ene.toro.sample.custom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import com.ramotion.cardslider.CardSliderLayoutManager;
import im.ene.toro.ToroLayoutManager;

/**
 * @author eneim (7/6/17).
 */

@SuppressWarnings("unused") public class CustomCardSliderLayoutManager
    extends CardSliderLayoutManager implements ToroLayoutManager {

  public CustomCardSliderLayoutManager(@NonNull Context context) {
    super(context);
  }

  public CustomCardSliderLayoutManager(@NonNull Context context, AttributeSet attrs, int defStyleAttr,
      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  public CustomCardSliderLayoutManager(int activeCardLeft, int cardWidth, float cardsGap) {
    super(activeCardLeft, cardWidth, cardsGap);
  }

  @Override public int getFirstVisibleItemPosition() {
    int active = super.getActiveCardPosition();
    return Math.max(0, active - 2);
  }

  @Override public int getLastVisibleItemPosition() {
    int active = super.getActiveCardPosition();
    return Math.min(getItemCount() - 1, active + 2);
  }
}
