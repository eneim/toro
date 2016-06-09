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

package im.ene.lab.toro.player.listener;

import im.ene.lab.toro.player.PlaybackException;
import im.ene.lab.toro.player.State;
import im.ene.lab.toro.player.TrMediaPlayer;
import im.ene.lab.toro.player.internal.ExoMediaPlayer;

/**
 * Created by eneim on 6/9/16.
 */
public interface OnPlayerStateChangeListener {

  /**
   * Invoked when the value returned from either {@link ExoMediaPlayer#getPlayWhenReady()} or
   * {@link ExoMediaPlayer#getPlaybackState()} changes.
   *
   * @param playWhenReady Whether playback will proceed when ready.
   * @param playbackState One of the {@link State} constants defined in the {@link TrMediaPlayer}
   * interface.
   */
  void onPlayerStateChanged(TrMediaPlayer player, boolean playWhenReady, @State int playbackState);

  /**
   * Invoked when an error occurs. The playback state will transition to
   * {@link TrMediaPlayer#STATE_IDLE} immediately after this method is invoked. The player instance
   * can still be used, and {@link TrMediaPlayer#release()} must still be called on the player
   * should
   * it no longer be required.
   *
   * @param error The error.
   */
  boolean onPlayerError(TrMediaPlayer player, PlaybackException error);
}
