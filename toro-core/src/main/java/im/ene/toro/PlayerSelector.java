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
import im.ene.toro.widget.Container;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static im.ene.toro.ToroUtil.visibleAreaOffset;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * @author eneim | 6/2/17.
 *
 *         PlayerSelector is a convenient class to help selecting the players to start Media
 *         playback.
 *
 *         On specific event of RecyclerView, such as Child view attached/detached, scroll, the
 *         collection of players those are available for a playback will change. PlayerSelecter is
 *         used to select a specific number of players from that updated Collection to start a new
 *         playback or pause an old playback if the corresponding Player is not selected anymore.
 *
 *         Client should implement a custom PlayerSelecter and set it to the Container for expected
 *         behaviour. By default, Toro comes with linear selection implementation (the Selector
 *         that
 *         will iterate over the Collection and select the players from top to bottom until a
 *         certain condition is fullfilled, for example the maximum of player count is reached).
 *
 *         Custom Selector can have more complicated selecting logics, for example: among 2n + 1
 *         playable widgets, select n players in the middles ...
 */

@SuppressWarnings("unused") //
public interface PlayerSelector {

  String TAG = "ToroLib:Selector";

  /**
   * Select a collection of {@link ToroPlayer}s to start a playback (if there is non-playing) item.
   * Playing item are also selected.
   *
   * @param container current {@link Container} that holds the players.
   * @param items a mutable collection of candidate {@link ToroPlayer}s, which are the players
   * those can start a playback. Items are sorted in order obtained from {@link
   * ToroPlayer#getPlayerOrder()}.
   * @return the collection of {@link ToroPlayer}s to start a playback. An on-going playback can be
   * selected, but it will keep playing.
   */
  @NonNull Collection<ToroPlayer> select(@NonNull Container container,
      @NonNull List<ToroPlayer> items);

  /**
   * The 'reverse' selector of this selector, which can help to select the reversed collection of
   * that expected by this selector.
   * For example: this selector will select the first playable {@link ToroPlayer} from top, so the
   * 'reverse' selector will select the last playable {@link ToroPlayer} from top.
   *
   * @return The PlayerSelector that has opposite selecting logic. If there is no special one,
   * return "this".
   */
  @NonNull PlayerSelector reverse();

  PlayerSelector DEFAULT = new PlayerSelector() {
    @NonNull @Override public Collection<ToroPlayer> select(@NonNull Container container, //
        @NonNull List<ToroPlayer> items) {
      int count = items.size();
      return count > 0 ? singletonList(items.get(0)) : Collections.<ToroPlayer>emptyList();
    }

    @NonNull @Override public PlayerSelector reverse() {
      return DEFAULT_REVERSE;
    }
  };

  PlayerSelector DEFAULT_REVERSE = new PlayerSelector() {
    @NonNull @Override public Collection<ToroPlayer> select(@NonNull Container container, //
        @NonNull List<ToroPlayer> items) {
      int count = items.size();
      return count > 0 ? singletonList(items.get(count - 1)) : Collections.<ToroPlayer>emptyList();
    }

    @NonNull @Override public PlayerSelector reverse() {
      return DEFAULT;
    }
  };

  @SuppressWarnings("unused") PlayerSelector BY_AREA = new PlayerSelector() {
    @NonNull @Override public Collection<ToroPlayer> select(@NonNull final Container container,
        @NonNull List<ToroPlayer> items) {
      int count = items.size();
      Collections.sort(items, new Comparator<ToroPlayer>() {
        @Override public int compare(ToroPlayer o1, ToroPlayer o2) {
          return Float.compare(visibleAreaOffset(o1, container), visibleAreaOffset(o2, container));
        }
      });

      return count > 0 ? singletonList(items.get(0)) : Collections.<ToroPlayer>emptyList();
    }

    @NonNull @Override public PlayerSelector reverse() {
      return this;  // FIXME return proper reverse selector.
    }
  };

  @SuppressWarnings("unused") PlayerSelector NONE = new PlayerSelector() {
    @NonNull @Override public Collection<ToroPlayer> select(@NonNull Container container, //
        @NonNull List<ToroPlayer> items) {
      return emptyList();
    }

    @NonNull @Override public PlayerSelector reverse() {
      return this;
    }
  };
}
