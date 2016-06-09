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
import im.ene.lab.toro.player.PlaybackException;
import im.ene.lab.toro.player.PlaybackInfo;
import im.ene.lab.toro.player.TrMediaPlayer;
import im.ene.lab.toro.player.listener.OnInfoListener;
import im.ene.lab.toro.player.listener.OnPlayerStateChangeListener;

/**
 * Created by eneim on 1/29/16.
 */
public interface ToroPlayer extends TrMediaPlayer.IMediaPlayer,
    // OnPreparedListener, OnCompletionListener, OnErrorListener,
    OnInfoListener, OnPlayerStateChangeListener {

  /**
   * This player wants to play or not. Client must provide reasonable motivation for this Player to
   * be played. For example, it could be properly visible by User, therefore It wants to behave.
   */
  boolean wantsToPlay();

  /**
   * {@since 2.0.0}
   *
   * !Deprecated From 2.0.0. User will just need to tell Toro by {@link ToroPlayer#wantsToPlay()}.
   * Toro 2.0 will not listen to this Method anymore.
   *
   * Called after {@link ToroPlayer#wantsToPlay()} returning <b>true</b>, indicate that even if
   * this player wants to play that much, is It able to play (Video is correctly set or there is no
   * Error)
   */
  // @Deprecated boolean isAbleToPlay();

  /**
   * Indicate that this Player is able to replay right after it stops (loop-able) or not.
   *
   * @return true if this Player is loop-able, false otherwise
   */
  boolean isLoopAble();

  /**
   * @return value from 0.0 ~ 1.0 the visible offset of current Video
   */
  @FloatRange(from = 0.0, to = 1.0) float visibleAreaOffset();

  /**
   * Support save/restore Video state (last played/paused position)
   *
   * !IMPORTANT this ID must be unique, and avoid using Video's filename, url String or any object
   * that depends on the Video object itself. There will be the case user uses same Video in
   * different places.
   * <p/>
   * Furthermore, ToroPlayer would be recycled, so it requires a separated, resource-independent Id
   *
   * @return current Video's id.
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
  void onActivityActive();

  /**
   * Host Activity resumed
   */
  void onActivityInactive();

  /* Playback lifecycle callback */

  /**
   * Replace {@link MediaPlayer.OnPreparedListener#onPrepared(MediaPlayer)}
   *
   * @param mp media player which is prepared
   */
  void onVideoPrepared(TrMediaPlayer mp);

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
   * Called from {@link Toro#onError(ToroPlayer, TrMediaPlayer, PlaybackException)}
   *
   * This method has the same signature with {@link OnPlayerStateChangeListener#onPlayerError(Exception)},
   * but {@link OnPlayerStateChangeListener#onPlayerError(Exception)} will be called explicitly by
   * Toro, so this method will prevent infinite loop
   */
  boolean onPlaybackError(TrMediaPlayer mp, PlaybackException error);

  /**
   * Called from {@link Toro#onInfo(ToroPlayer, TrMediaPlayer, PlaybackInfo)}
   *
   * This method has the same signature with {@link ToroPlayer#onInfo(TrMediaPlayer, PlaybackInfo)}
   * , but {@link ToroPlayer##onInfo(TrMediaPlayer, PlaybackInfo)} will be called explicitly by
   * Toro, so this method will prevent infinite loop
   */
  void onPlaybackInfo(TrMediaPlayer mp, PlaybackInfo info);

  /**
   * Callback from playback progress update. This method is called from main thread (UIThread)
   *
   * @param position current playing position
   * @param duration total duration of current video
   */
  @UiThread void onPlaybackProgress(long position, long duration);
}
