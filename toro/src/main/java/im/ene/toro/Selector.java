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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import ix.Ix;
import java.util.Collection;

/**
 * @author eneim | 6/2/17.
 */

public interface Selector {

  String TAG = "ToroLib:Selector";

  @NonNull Collection<Player> select(@NonNull Collection<Player> items, int limit);

  /**
   * @return The Selector that has opposite selecting logic.
   */
  @Nullable Selector mirror();

  Selector DEFAULT = new Selector() {
    @NonNull @Override
    public Collection<Player> select(@NonNull Collection<Player> items, int limit) {
      Log.w(TAG, "select() called with: items = [" + items + "], limit = [" + limit + "]");
      return Ix.from(items).take(limit).toList();
    }

    @Override public Selector mirror() {
      return DEFAULT_REVERSE;
    }
  };

  Selector DEFAULT_REVERSE = new Selector() {
    @NonNull @Override
    public Collection<Player> select(@NonNull Collection<Player> items, int limit) {
      Log.w(TAG, "select() called with: items = [" + items + "], limit = [" + limit + "]");
      return Ix.from(items).takeLast(limit).toList();
    }

    @Nullable @Override public Selector mirror() {
      return DEFAULT;
    }
  };
}
