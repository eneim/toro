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
import im.ene.toro.ToroPlayer;
import im.ene.toro.media.PlaybackInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author eneim | 5/31/17.
 *
 *         Logic: collect all Players those "wantsToPlay()", then internally decide if we allow
 *         each of them to play or not.
 *
 *         All managed players must return {@code true} from {@link ToroPlayer#wantsToPlay()}
 */

final class PlayerManager {

  @SuppressWarnings("unused") private static String TAG = "ToroLib:PlayerManager";

  private final HashSet<ToroPlayer> players = new HashSet<>();

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
   * Return a collection of Players those are allowed to play by Container.
   *
   * @return a non null collection of Players those are allowed to play by Container.
   */
  @NonNull Collection<ToroPlayer> getPlayers() {
    return new ArrayList<>(this.players);
  }

  public void initialize(@NonNull ToroPlayer player, @NonNull Container container,
      @NonNull PlaybackInfo playbackInfo) {
    player.initialize(container, playbackInfo);
  }

  public void play(@NonNull ToroPlayer player) {
    player.play();
  }

  public void pause(@NonNull ToroPlayer player) {
    player.pause();
  }

  public void release(@NonNull ToroPlayer player) {
    player.release();
  }

  void clear() {
    this.players.clear();
  }
}
