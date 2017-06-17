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
import android.view.View;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import im.ene.toro.media.MediaPlayer;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;

/**
 * @author eneim | 5/31/17.
 */

public interface ToroPlayer extends MediaPlayer {

  @NonNull View getPlayerView();

  @NonNull PlaybackInfo getCurrentPlaybackInfo();

  /**
   * {@link ExoPlayer} should call {@link ExoPlayer#prepare(MediaSource)} here, and not start
   * playback when ready
   *
   * @param container the RecyclerView contains this Player.
   * @param playbackInfo initialize info for the preparation.
   */
  void initialize(@NonNull Container container, @NonNull PlaybackInfo playbackInfo);

  /**
   * Tear down all the setup. This should release all player instances.
   */
  void release();

  boolean wantsToPlay();

  /**
   * @return prefer playback order in list. Can be customized.
   */
  int getPlayerOrder();

  interface EventListener {

    void onBuffering(); // ExoPlayer state: 2

    void onPlaying(); // ExoPlayer state: 3, play flag: true

    void onPaused();  // ExoPlayer state: 3, play flag: false

    void onCompleted(Container container, ToroPlayer player); // ExoPlayer state: 4
  }
}
