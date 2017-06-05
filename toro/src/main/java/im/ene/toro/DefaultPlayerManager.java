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
import android.util.Log;
import im.ene.toro.widget.Container;
import ix.Ix;
import ix.IxConsumer;
import ix.IxPredicate;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author eneim | 5/31/17.
 */

public class DefaultPlayerManager implements PlayerManager {

  private final HashSet<Player> players = new HashSet<>();

  private final int playerCount;

  public DefaultPlayerManager(int playerCount) {
    this.playerCount = playerCount;
  }

  @SuppressWarnings("unused") public DefaultPlayerManager() {
    this(1);
  }

  @Override
  public void updatePlayback(@NonNull final Container container, @NonNull Selector selector) {
    Log.e(TAG, "updatePlayback: " + this.players);
    if (BuildConfig.DEBUG) {
      //noinspection ConstantConditions
      if (selector == null) {
        throw new IllegalArgumentException("Selector must not be null");
      }
    }

    if (this.players.isEmpty()) return;
    // from current player list:
    // 1. find those are allowed to play
    // 2. among them, use Selector to select a subset then for each of them start the playback
    // if it is not playing, and pause the playback for others.
    final Ix<Player> source = Ix.from(players).filter(new IxPredicate<Player>() {
      @Override public boolean test(Player player) {
        return ToroUtil.doAllowsToPlay(player.getPlayerView(), container);
      }
    }).orderBy(Common.ORDER_COMPARATOR);

    source.except(Ix.from(selector.select(source.toList(), this.playerCount))
        .doOnNext(new IxConsumer<Player>() {
          @Override public void accept(Player player) {
            if (!player.isPlaying()) player.play();
          }
        })) //
        .doOnNext(new IxConsumer<Player>() {
          @Override public void accept(Player player) {
            if (player.isPlaying()) player.pause();
          }
        }).subscribe();
  }

  @Override public boolean attachPlayer(@NonNull Player player) {
    Log.d(TAG, "attachPlayer() called with: player = [" + player + "]");
    return players.add(player);
  }

  @Override public boolean detachPlayer(@NonNull Player player) {
    Log.d(TAG, "detachPlayer() called with: player = [" + player + "]");
    return players.remove(player);
  }

  @Override public boolean manages(@NonNull Player player) {
    return players.contains(player);
  }

  @NonNull @Override public Collection<Player> getPlayers() {
    return this.players;
  }
}
