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

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author eneim (7/5/17).
 */

public interface CacheManager {

  @Nullable Object getKeyForOrder(@IntRange(from = 0) int order);

  // return null will ignore cache for this key.
  @Nullable Integer getOrderForKey(@NonNull Object key);

  CacheManager DEFAULT = new CacheManager() {
    @Override public Object getKeyForOrder(int order) {
      return order;
    }

    @Override public Integer getOrderForKey(@NonNull Object key) {
      return key instanceof Integer ? (Integer) key : null;
    }
  };
}
