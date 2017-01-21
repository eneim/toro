/*
 * Copyright 2017 eneim@Eneim Labs, nam@ene.im
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

import android.support.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by eneim on 1/31/16.
 *
 * Extension/Implementation of {@link PlayerManager}. Can be used as a delegation.
 */
final class PlayerManagerImpl implements PlayerManager {

  private final Map<String, PlaybackState> playbackStates = new LinkedHashMap<>();

  private ToroPlayer player;

  @Nullable @Override public ToroPlayer getPlayer() {
    return player;
  }

  @Override public void setPlayer(ToroPlayer player) {
    this.player = player;
  }

  @Override public void onRegistered() {

  }

  @Override public void onUnregistered() {

  }

  @Override public void startPlayback() {
    if (player != null) {
      player.start();
    }
  }

  @Override public void pausePlayback() {
    if (player != null) {
      player.pause();
    }
  }

  @Override public void stopPlayback() {
    if (player != null) {
      player.stop();
    }
  }

  @Override public void saveVideoState(String videoId, @Nullable Long position, long duration) {
    PlaybackState playbackState = playbackStates.get(videoId);
    if (playbackState == null) {
      playbackState = new PlaybackState(videoId);
    }

    playbackState.setDuration(duration);
    playbackState.setPosition(position);
    playbackStates.put(videoId, playbackState);
  }

  @Override public void restoreVideoState(String videoId) {
    if (player == null) {
      return;
    }

    player.seekTo(getSavedPosition(videoId));
  }

  private long getSavedPosition(String videoId) {
    PlaybackState savedState = playbackStates.get(videoId);
    long position = 0;
    if (savedState != null) {
      position = savedState.getPosition();
    }
    return position;
  }

  @Nullable @Override public PlaybackState getSavedState(String videoId) {
    return playbackStates.get(videoId);
  }

  @Override public void remove() throws Exception {
    playbackStates.clear();
  }
}
