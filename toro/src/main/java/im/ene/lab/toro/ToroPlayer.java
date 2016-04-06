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
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.MediaController;

/**
 * Created by eneim on 1/29/16.
 */
public interface ToroPlayer
    extends MediaController.MediaPlayerControl, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
    MediaPlayer.OnSeekCompleteListener {

  /**
   * This player wants to play or not. Client must provide reasonable motivation for this Player to
   * be played. For example, it could be properly visible by User, therefore It wants to behave.
   */
  boolean wantsToPlay();

  /**
   * Called after {@link ToroPlayer#wantsToPlay()} returning <b>true</b>, indicate that even if
   * this player wants to play that much, is It able to play (Video is correctly set or there is no
   * Error)
   */
  boolean isAbleToPlay();

  /**
   * Indicate that this Player is able to replay right after it stops (loop-able) or not.
   *
   * @return true if this Player is loopable, false otherwise
   */
  boolean isLoopAble();

  /**
   * @return value from 0.0 ~ 1.0 the visible offset of current Video
   */
  @FloatRange(from = 0.0, to = 1.0) float visibleAreaOffset();

  /**
   * Support save/restore Video state (last played/paused position)
   *
   * @return current Video's id. !IMPORTANT this ID must be unique, and avoid using Video's
   * filename, url String or any object which depends on the Video object itself. There will be
   * the case user uses same Video in multiple place. User of this library should better use the
   * main object (which holds the Video as member) as key to generate this Id.
   * <p/>
   * Furthermore, ToroPlayer would be recycled, so it requires a separated, resource-depended Id
   */
  @Nullable String getVideoId();

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
  @NonNull View getVideoView();

  /* Host activity lifecycle callback */

  /**
   * Host Activity paused
   */
  void onActivityPaused();

  /**
   * Host Activity resumed
   */
  void onActivityResumed();

  /* Playback lifecycle callback */

  /**
   * Replace {@link MediaPlayer.OnPreparedListener#onPrepared(MediaPlayer)}
   *
   * @param mp media player which is prepared
   */
  void onVideoPrepared(MediaPlayer mp);

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
  void onPlaybackStopped();

  /**
   * Called from {@link Toro#onError(ToroPlayer, MediaPlayer, int, int)}
   *
   * This method has the same signature with {@link ToroPlayer#onError(MediaPlayer, int, int)}, but
   * {@link ToroPlayer#onError(MediaPlayer, int, int)} will be called explicitly by Toro, so this
   * method will prevent infinite loop
   */
  boolean onPlaybackError(MediaPlayer mp, int what, int extra);

  /**
   * Called from {@link Toro#onError(ToroPlayer, MediaPlayer, int, int)}
   *
   * This method has the same signature with {@link ToroPlayer#onInfo(MediaPlayer, int, int)} , but
   * {@link ToroPlayer#onInfo(MediaPlayer, int, int)} will be called explicitly by Toro, so this
   * method will prevent infinite loop
   */
  void onPlaybackInfo(MediaPlayer mp, int what, int extra);

  /**
   * Callback from playback progress update. This method is called from main thread (UIThread)
   *
   * @param position current playing position
   * @param duration total duration of current video
   */
  @UiThread void onPlaybackProgress(int position, int duration);
}
