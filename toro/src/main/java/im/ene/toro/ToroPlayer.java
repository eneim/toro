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

package im.ene.toro;

import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by eneim on 1/29/16.
 */
public interface ToroPlayer extends BaseMediaPlayer {

  boolean isPrepared();

  /**
   * This player wants to play or not. Client must provide reasonable motivation for this Player to
   * be played. For example, it could be properly visible by User, therefore It wants to behave.
   *
   * @return {@code true} if this Player wants to start playback, {@code false} otherwise.
   */
  boolean wantsToPlay();

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
   *
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
   *
   * @return current Adapter position of this Player
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
   * Preparing resources.
   *
   * @deprecated only used in ExoPlayer v1 Extension.
   */
  @Deprecated
  void onVideoPreparing();

  /**
   * Replace {@link android.media.MediaPlayer.OnPreparedListener#onPrepared(android.media.MediaPlayer)}
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
