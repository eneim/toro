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

package com.google.android.libraries.mediaframework.exoplayerextensions;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.util.PlayerControl;

import java.util.ArrayList;
import java.util.List;

/**
 * Extends the {@link PlayerControl} class, which is responsible
 * for controlling playback (ex play, pause, seek, get duration, get elapsed time) by adding the
 * ability to make a list of callbacks which respond when the player is paused or played.
 */
public class ObservablePlayerControl extends PlayerControl {

  /**
   * Callbacks which will react to the player pausing or playing.
   */
  List<PlayerControlCallback> callbacks;

  /**
   * @param exoPlayer The {@link ExoPlayer} instance to control.
   */
  public ObservablePlayerControl(ExoPlayer exoPlayer) {
    super(exoPlayer);
    callbacks = new ArrayList<PlayerControlCallback>();
  }

  /**
   * Add a callback to listen to play and pause events.
   * @param callback Responds when the player is paused or played.
   */
  public void addCallback(PlayerControlCallback callback) {
    callbacks.add(callback);
  }

  /**
   * Pause the video and notify the callbacks.
   */
  @Override
  public void pause() {
    super.pause();
    for (PlayerControlCallback callback : callbacks) {
      callback.onPause();
    }
  }

  /**
   * Remove a callback which is currently listening to play and pause events on the
   * {@link ExoPlayer} instance.
   * @param callback Responds when the player is paused or played.
   */
  public void removeCallback(PlayerControlCallback callback) {
    callbacks.remove(callback);
  }

  /**
   * Play the video and notify the callbacks.
   */
  @Override
  public void start() {
    super.start();
    for (PlayerControlCallback callback : callbacks) {
      callback.onPlay();
    }
  }

}
