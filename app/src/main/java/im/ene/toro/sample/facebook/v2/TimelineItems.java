/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
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

package im.ene.toro.sample.facebook.v2;

import android.support.annotation.NonNull;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.function.Predicate;

final class TimelineItems extends ArrayList<BaseItem> {

  private static final BaseItem NULL = new BaseItem();
  private static final int DEMO_COUNT = VideoItem.VIDEOS.length;

  private final SparseArray<BaseItem> mCache = new SparseArray<>();
  private final Random random = new Random();
  private final double rate;

  @SuppressWarnings("unused") //
  TimelineItems() {
    this(0.65);
  }

  TimelineItems(double rate) {
    if (rate >= 1.0) throw new IllegalArgumentException("Rate must be less than 1.0");
    this.rate = rate;
  }

  @Override public BaseItem get(int index) {
    BaseItem cache = mCache.get(index);
    if (cache == null) {
      double next = random.nextDouble();
      if (next < this.rate || index <= 4) {
        // Add 'NULL' to cache
        cache = NULL;
      } else {
        double mark = DEMO_COUNT * (next - this.rate) / (1.0 - this.rate);
        cache = VideoItem.VIDEOS[((int) mark % DEMO_COUNT)].getCopy();
      }
      mCache.put(index, cache);
    }
    return cache == NULL ? null : cache;
  }

  @Override public int size() {
    return Integer.MAX_VALUE;
  }

  @Override public boolean add(BaseItem t) {
    return false;
  }

  @Override public void add(int index, BaseItem element) {
    // Do nothing
  }

  @Override public boolean addAll(@NonNull Collection<? extends BaseItem> c) {
    return false;
  }

  @Override public boolean addAll(int index, @NonNull Collection<? extends BaseItem> c) {
    return false;
  }

  @Override public boolean remove(Object o) {
    return false;
  }

  @Override public BaseItem remove(int index) {
    return null;
  }

  @Override protected void removeRange(int fromIndex, int toIndex) {
    // Do nothing
  }

  @Override public boolean removeAll(@NonNull Collection<?> c) {
    return false;
  }

  @Override public boolean removeIf(@NonNull Predicate<? super BaseItem> filter) {
    return false;
  }
}
