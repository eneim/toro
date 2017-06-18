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
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
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

public class PlayerManager {

  @SuppressWarnings("unused") String TAG = "ToroLib:PlayerManager";

  private final HashSet<ToroPlayer> players = new HashSet<>();

  public boolean attachPlayer(@NonNull ToroPlayer player) {
    return players.add(player);
  }

  public boolean detachPlayer(@NonNull ToroPlayer player) {
    return players.remove(player);
  }

  public boolean manages(@NonNull ToroPlayer player) {
    return players.contains(player);
  }

  /**
   * Return a collection of Players those are allowed to play by Container.
   *
   * @return a non null collection of Players those are allowed to play by Container.
   */
  @NonNull public Collection<ToroPlayer> getPlayers() {
    return new ArrayList<>(this.players);
  }

  public void initialize(@NonNull ToroPlayer player, @NonNull Container container,
      @NonNull PlaybackInfo playbackInfo) {
    player.initialize(container, playbackInfo);
    Log.d(TAG, "initialize() called with: player = ["
        + player
        + "], container = ["
        + container
        + "], playbackInfo = ["
        + playbackInfo
        + "]");
  }

  public void play(@NonNull ToroPlayer player) {
    player.play();
    Log.d(TAG, "play() called with: player = [" + player + "]");
  }

  public void pause(@NonNull ToroPlayer player) {
    player.pause();
    Log.d(TAG, "pause() called with: player = [" + player + "]");
  }

  public void release(@NonNull ToroPlayer player) {
    player.release();
    Log.d(TAG, "release() called with: player = [" + player + "]");
  }

  public void clear() {
    this.players.clear();
  }
}
