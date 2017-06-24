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

package im.ene.toro.widget;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.view.View;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import ix.Ix;
import java.util.Collection;
import java.util.Comparator;

import static im.ene.toro.widget.Common.ORDER_COMPARATOR;
import static im.ene.toro.widget.Common.ORDER_COMPARATOR_REVERSE;
import static java.util.Collections.emptyList;

/**
 * @author eneim | 6/2/17.
 */

public interface PlayerSelector {

  String TAG = "ToroLib:Selector";

  @NonNull Collection<ToroPlayer> select(@NonNull View container, //
      @NonNull Iterable<ToroPlayer> items, @IntRange(from = 0) int limit);

  /**
   * @return The PlayerSelector that has opposite selecting logic. If there is no available one,
   * return "this".
   */
  @NonNull PlayerSelector reverse();

  PlayerSelector DEFAULT = new PlayerSelector() {
    @NonNull @Override public Collection<ToroPlayer> select(@NonNull View container, //
        @NonNull Iterable<ToroPlayer> items, int limit) {
      return Ix.from(items).orderBy(ORDER_COMPARATOR).take(limit).toList();
    }

    @NonNull @Override public PlayerSelector reverse() {
      return DEFAULT_REVERSE;
    }
  };

  PlayerSelector DEFAULT_REVERSE = new PlayerSelector() {
    @NonNull @Override public Collection<ToroPlayer> select(@NonNull View container, //
        @NonNull Iterable<ToroPlayer> items, int limit) {
      return Ix.from(items).orderBy(ORDER_COMPARATOR_REVERSE).take(limit).toList();
    }

    @NonNull @Override public PlayerSelector reverse() {
      return DEFAULT;
    }
  };

  @SuppressWarnings("unused") PlayerSelector BY_AREA = new PlayerSelector() {
    @NonNull @Override public Collection<ToroPlayer> select(@NonNull final View container,
        @NonNull Iterable<ToroPlayer> items, int limit) {
      return Ix.from(items).orderBy(new Comparator<ToroPlayer>() {
        @Override public int compare(ToroPlayer o1, ToroPlayer o2) {
          return Float.compare( //
              ToroUtil.visibleAreaOffset(o1.getPlayerView(), container),
              ToroUtil.visibleAreaOffset(o2.getPlayerView(), container));
        }
      }).take(limit).toList();
    }

    @NonNull @Override public PlayerSelector reverse() {
      return this;  // FIXME return proper reverse selector.
    }
  };

  @SuppressWarnings("unused") PlayerSelector NONE = new PlayerSelector() {
    @NonNull @Override public Collection<ToroPlayer> select(@NonNull View container, //
        @NonNull Iterable<ToroPlayer> items, int limit) {
      return emptyList();
    }

    @NonNull @Override public PlayerSelector reverse() {
      return this;
    }
  };
}
