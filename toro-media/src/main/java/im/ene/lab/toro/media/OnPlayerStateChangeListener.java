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

package im.ene.lab.toro.media;

/**
 * Created by eneim on 6/9/16.
 */
public interface OnPlayerStateChangeListener {

  /**
   * @param playWhenReady Whether playback will proceed when ready.
   * @param playbackState One of the {@link State} constants defined in the {@link Cineer}
   * interface.
   */
  void onPlayerStateChanged(Cineer player, boolean playWhenReady, @State int playbackState);

  /**
   * Invoked when an error occurs. The playback state will transition to {@link Cineer#PLAYER_IDLE}
   * immediately after this method is invoked. The player instance can still be used, and {@link
   * Cineer#release()} must still be called on the player should it no longer be required.
   *
   * @param error The error.
   */
  boolean onPlayerError(Cineer player, PlaybackException error);
}
