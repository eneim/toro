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

import android.support.annotation.NonNull;
import android.support.v4.util.ArraySet;
import im.ene.toro.ToroPlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author eneim | 5/31/17.
 *
 *         Manage the collection of {@link ToroPlayer}s for a specific {@link Container}.
 *
 *         Task: collect all Players in which "{@link Common#allowsToPlay(ToroPlayer)}"
 *         returns true, then initialize them.
 */

@SuppressWarnings({ "unused", "UnusedReturnValue" }) //
final class PlayerManager {

  private final Container container;

  PlayerManager(Container container) {
    this.container = container;
  }

  // Make sure each ToroPlayer will present only once in this Manager.
  private final Set<ToroPlayer> players = new ArraySet<>();

  boolean attachPlayer(@NonNull ToroPlayer player) {
    return players.add(player);
  }

  boolean detachPlayer(@NonNull ToroPlayer player) {
    return players.remove(player);
  }

  boolean manages(@NonNull ToroPlayer player) {
    return players.contains(player);
  }

  /**
   * Return a "Copy" of the collection of players this manager is managing.
   *
   * @return a non null collection of Players those a managed.
   */
  @NonNull List<ToroPlayer> getPlayers() {
    return new ArrayList<>(this.players);
  }

  void initialize(@NonNull ToroPlayer player) {
    player.initialize(container, container.getPlaybackInfo(player.getPlayerOrder()));
  }

  void play(@NonNull ToroPlayer player) {
    player.play();
  }

  void pause(@NonNull ToroPlayer player) {
    player.pause();
  }

  void release(@NonNull ToroPlayer player) {
    player.release();
  }

  void clear() {
    this.players.clear();
  }
}
