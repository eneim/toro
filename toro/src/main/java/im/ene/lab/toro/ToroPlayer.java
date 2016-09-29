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

package im.ene.lab.toro;

import android.media.MediaPlayer;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.VideoView;

/**
 * Created by eneim on 1/29/16.
 */
public interface ToroPlayer {

  // Playback method

  /**
   * Prepare video
   */
  void preparePlayer(boolean playWhenReady);

  /**
   * See {@link VideoView#start()}
   */
  void start();

  /**
   * See {@link VideoView#pause()}
   */
  void pause();

  /**
   * See {@link VideoView#stopPlayback()}
   */
  void stop();

  /**
   *
   */
  void releasePlayer();

  /**
   * See {@link VideoView#getDuration()}
   *
   * @return media's duration.
   */
  long getDuration();

  /**
   * See {@link VideoView#getCurrentPosition()}
   *
   * @return current playback position.
   */
  long getCurrentPosition();

  /**
   * See {@link VideoView#seekTo(int)}
   *
   * @param pos seek to specific position.
   */
  void seekTo(long pos);

  /**
   * See {@link VideoView#isPlaying()}
   *
   * @return {@code true} if the media is being played, {@code false} otherwise.
   */
  boolean isPlaying();

  // Custom method to add Toro abilities

  /**
   * This player wants to play or not. Client must provide reasonable motivation for this Player to
   * be played. For example, it could be properly visible by User, therefore It wants to behave.
   */
  boolean wantsToPlay();

  /**
   * Indicate that this Player is able to replay right after it stops (loop-able) or not.
   *
   * @return true if this Player is loop-able, false otherwise
   */
  boolean isLoopAble();

  /**
   * @return value from 0.0 ~ 1.0 the visible Area offset of current Video
   */
  @FloatRange(from = 0.0, to = 1.0) float visibleAreaOffset();

  /**
   * Support save/restore Media state (last played/paused position)
   *
   * !IMPORTANT this ID must be unique, and avoid using Media's filename, url String or any object
   * that depends <b>only</b> on the Media object itself. There will be the case user uses same
   * Media in different places.
   * <p/>
   * Furthermore, ToroPlayer would be recycled, so it requires a separated, resource-independent Id
   *
   * @return current Media source's id.
   */
  @Nullable String getMediaId();

  /**
   * In case there is a list of Players who want to play, Toro wants to know their orders, then
   * Toro will decide who could start, based on Toro' playback {@link ToroStrategy}
   *
   * In RecyclerView (which is the only widget Toro currently support), this method expect
   * ViewHolder's Adapter position (see {@link RecyclerView.ViewHolder#getAdapterPosition()})
   */
  @IntRange(from = RecyclerView.NO_POSITION) int getPlayOrder();

  /**
   * Retrieve current player's View
   *
   * @return attached video view
   */
  @NonNull View getPlayerView();

  /* Host activity lifecycle callback */

  /**
   * Host Activity becomes active.
   * Note that Activity behaviour is different from API 24+ to API 23-.
   */
  void onActivityActive();

  /**
   * Host Activity becomes inactive.
   * Note that Activity behaviour is different from API 24+ to API 23-.
   */
  void onActivityInactive();

  /* Playback lifecycle callback */

  /**
   * Preparing resources;
   */
  void onVideoPreparing();

  /**
   * Replace {@link MediaPlayer.OnPreparedListener#onPrepared(MediaPlayer)}
   *
   * @param mp media player which is prepared
   */
  void onVideoPrepared();

  /**
   * Callback after this player starts playing
   */
  void onPlaybackStarted();

  /**
   * Callback after this player pauses playing
   */
  void onPlaybackPaused();

  /**
   * Callback after this player stops playing
   */
  void onPlaybackCompleted();

  boolean onPlaybackError(Exception error);
}
