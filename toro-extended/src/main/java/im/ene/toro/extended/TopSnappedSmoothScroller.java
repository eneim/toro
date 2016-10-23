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

package im.ene.toro.extended;

import android.content.Context;
import android.support.v7.widget.LinearSmoothScroller;
import android.util.DisplayMetrics;

/**
 * Created by eneim on 10/23/16.
 */class TopSnappedSmoothScroller extends LinearSmoothScroller {
  TopSnappedSmoothScroller(Context context) {
    super(context);
  }

  @Override protected int getVerticalSnapPreference() {
    return SNAP_TO_START;
  }

  @Override protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
    return super.calculateSpeedPerPixel(displayMetrics) * 5.f;
  }
}
