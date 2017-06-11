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
import ix.Ix;
import ix.IxConsumer;
import ix.IxPredicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author eneim | 5/31/17.
 */

public class DefaultPlayerManager implements PlayerManager {

  private final HashSet<ToroPlayer> players = new HashSet<>();
  private final int playerCount;

  public DefaultPlayerManager(int playerCount) {
    this.playerCount = playerCount;
  }

  @SuppressWarnings("unused") public DefaultPlayerManager() {
    this(1);
  }

  @Override
  public void updatePlayback(@NonNull final Container container, @NonNull PlayerSelector selector) {
    if (BuildConfig.DEBUG) {
      //noinspection ConstantConditions
      if (selector == null) {
        throw new IllegalArgumentException("PlayerSelector is Null.");
      }
    }

    if (this.players.isEmpty()) return;
    // from current player list:
    // 1. find those are allowed to play
    // 2. among them, use PlayerSelector to select a subset then for each of them start the playback
    // if it is not playing, and pause the playback for others.
    final Ix<ToroPlayer> source = Ix.from(players).filter(new IxPredicate<ToroPlayer>() {
      @Override public boolean test(ToroPlayer player) {
        return Common.doAllowsToPlay(player.getPlayerView(), container);
      }
    });

    source.except(Ix.from(selector.select(container, source.toList(), this.playerCount))
        .doOnNext(new IxConsumer<ToroPlayer>() {
          @Override public void accept(ToroPlayer player) {
            if (!player.isPlaying()) player.play();
          }
        })) //
        .doOnNext(new IxConsumer<ToroPlayer>() {
          @Override public void accept(ToroPlayer player) {
            if (player.isPlaying()) player.pause();
          }
        }).subscribe();
  }

  @Override public boolean attachPlayer(@NonNull ToroPlayer player) {
    return players.add(player);
  }

  @Override public boolean detachPlayer(@NonNull ToroPlayer player) {
    return players.remove(player);
  }

  @Override public boolean manages(@NonNull ToroPlayer player) {
    return players.contains(player);
  }

  @NonNull @Override public Collection<ToroPlayer> getPlayers() {
    return new ArrayList<>(this.players);
  }

  @Override public void clear() {
    this.players.clear();
  }
}
