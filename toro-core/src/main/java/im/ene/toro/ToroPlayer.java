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

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author eneim | 5/31/17.
 */

public interface ToroPlayer {

  @NonNull View getPlayerView();

  @NonNull PlaybackInfo getCurrentPlaybackInfo();

  /**
   * Initialize resource for the incoming playback. After this point, {@link ToroPlayer} should be
   * able to start the playback at anytime in the future (This doesn't mean that any call to {@link
   * ToroPlayer#play()} will start the playback immediately. It can start buffering enough resource
   * before any rendering). {@link ExoPlayer} should call {@link ExoPlayer#prepare(MediaSource)}
   * here, and not start playback when ready.
   *
   * @param container the RecyclerView contains this Player.
   * @param playbackInfo initialize info for the preparation.
   */
  void initialize(@NonNull Container container, @Nullable PlaybackInfo playbackInfo);

  /**
   * Start playback or resume from a pausing state.
   */
  void play();

  /**
   * Pause current playback.
   */
  void pause();

  boolean isPlaying();

  /**
   * Tear down all the setup. This should release all player instances.
   */
  void release();

  boolean wantsToPlay();

  /**
   * @return prefer playback order in list. Can be customized.
   */
  int getPlayerOrder();

  /**
   * Notify a Player about its {@link Container}'s scroll state change.
   *
   * @param container the {@link Container} that contains this player.
   * @param newState new Scroll state of the Container.
   */
  void onContainerScrollStateChange(Container container, int newState);

  /**
   * A convenient callback to help {@link ToroPlayer} to listen to different playback states.
   */
  interface EventListener {

    void onBuffering(); // ExoPlayer state: 2

    void onPlaying(); // ExoPlayer state: 3, play flag: true

    void onPaused();  // ExoPlayer state: 3, play flag: false

    void onCompleted(Container container, ToroPlayer player); // ExoPlayer state: 4
  }

  // Adapt from ExoPlayer.
  @SuppressWarnings("UnnecessaryInterfaceModifier") @Retention(RetentionPolicy.SOURCE)  //
  @IntDef({ State.STATE_IDLE, State.STATE_BUFFERING, State.STATE_READY, State.STATE_END })  //
  public @interface State {
    int STATE_IDLE = 1;
    int STATE_BUFFERING = 2;
    int STATE_READY = 3;
    int STATE_END = 4;
  }
}
