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

package im.ene.toro.sample.feature.advance1;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import im.ene.toro.ToroLayoutManager;

/**
 * Created by eneim on 6/30/16.
 */
public class Advance1LayoutManager extends CarouselLayoutManager implements ToroLayoutManager {

  public Advance1LayoutManager(int orientation) {
    super(orientation);
  }

  public Advance1LayoutManager(int orientation, boolean circleLayout) {
    super(orientation, circleLayout);
  }

  @Override public int getFirstVisibleItemPosition() {
    return getCenterItemPosition();
  }

  @Override public int getLastVisibleItemPosition() {
    return getCenterItemPosition();
  }
}
