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
import android.view.View;
import ix.Ix;
import java.util.Collection;
import java.util.Comparator;

import static java.util.Collections.emptyList;

/**
 * @author eneim | 6/2/17.
 */

public interface Selector {

  String TAG = "ToroLib:Selector";

  @NonNull Collection<Player> select(@NonNull View container, //
      @NonNull Collection<Player> items, int limit);

  /**
   * @return The Selector that has opposite selecting logic.
   */
  @SuppressWarnings("unused") @Nullable Selector reverse();

  Selector DEFAULT = new Selector() {
    @NonNull @Override public Collection<Player> select(@NonNull View container, //
        @NonNull Collection<Player> items, int limit) {
      Log.w(TAG, "select() called with: items = [" + items + "], limit = [" + limit + "]");
      return Ix.from(items).orderBy(Common.ORDER_COMPARATOR).take(limit).toList();
    }

    @Override public Selector reverse() {
      return DEFAULT_REVERSE;
    }
  };

  Selector DEFAULT_REVERSE = new Selector() {
    @NonNull @Override public Collection<Player> select(@NonNull View container, //
        @NonNull Collection<Player> items, int limit) {
      Log.w(TAG, "select() called with: items = [" + items + "], limit = [" + limit + "]");
      return Ix.from(items).takeLast(limit).toList();
    }

    @Nullable @Override public Selector reverse() {
      return DEFAULT;
    }
  };

  @SuppressWarnings("unused") Selector BY_AREA = new Selector() {
    @NonNull @Override public Collection<Player> select(@NonNull final View container,
        @NonNull Collection<Player> items, int limit) {
      return Ix.from(items).orderBy(new Comparator<Player>() {
        @Override public int compare(Player o1, Player o2) {
          return Float.compare( //
              ToroUtil.visibleAreaOffset(o1.getPlayerView(), container),
              ToroUtil.visibleAreaOffset(o2.getPlayerView(), container));
        }
      }).take(limit).toList();
    }

    @Nullable @Override public Selector reverse() {
      return this;  // FIXME return proper reverse selector.
    }
  };

  @SuppressWarnings("unused") Selector NONE = new Selector() {
    @NonNull @Override public Collection<Player> select(@NonNull View container, //
        @NonNull Collection<Player> items, int limit) {
      return emptyList();
    }

    @Nullable @Override public Selector reverse() {
      return this;
    }
  };
}
