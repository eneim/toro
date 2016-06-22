/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
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

package im.ene.lab.toro.player.internal;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.util.PlayerControl;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eneim on 6/12/16.
 */
public class ObservablePlayerControl extends PlayerControl {

  /**
   * Callbacks which will react to the player pausing or playing.
   */
  List<PlayerControlCallback> callbacks;

  /**
   * @param exoPlayer The {@link com.google.android.exoplayer.ExoPlayer} instance to control.
   */
  public ObservablePlayerControl(ExoPlayer exoPlayer) {
    super(exoPlayer);
  }

  /**
   * @param exoMediaPlayer Hold The {@link com.google.android.exoplayer.ExoPlayer} instance to
   * control.
   */
  public ObservablePlayerControl(ExoMediaPlayer exoMediaPlayer) {
    this(exoMediaPlayer.player);
  }

  /**
   * Add a callback to listen to play and pause events.
   *
   * @param callback Responds when the player is paused or played.
   */
  public void addCallback(PlayerControlCallback callback) {
    if (callbacks == null) {
      callbacks = new ArrayList<>();
    }
    callbacks.add(callback);
  }

  /**
   * Pause the video and notify the callbacks.
   */
  @Override public void pause() {
    super.pause();
    for (PlayerControlCallback callback : callbacks) {
      callback.onPause();
    }
  }

  /**
   * Remove a callback which is currently listening to play and pause events on the
   * {@link com.google.android.exoplayer.ExoPlayer} instance.
   *
   * @param callback Responds when the player is paused or played.
   */
  public void removeCallback(PlayerControlCallback callback) {
    callbacks.remove(callback);
  }

  /**
   * Play the video and notify the callbacks.
   */
  @Override public void start() {
    super.start();
    for (PlayerControlCallback callback : callbacks) {
      callback.onPlay();
    }
  }
}
