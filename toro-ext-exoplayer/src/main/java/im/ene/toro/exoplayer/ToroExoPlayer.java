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

package im.ene.toro.exoplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.view.Surface;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;
import java.io.IOException;
import java.util.Map;

/**
 * ToroExoPlayer is the base interface for any MediaPlayer. It is named by the combination of 'Cine' and
 * '-er' suffix.
 *
 * @author eneim (nam@ene.im)
 * @version 2.0
 */
public interface ToroExoPlayer {

  /**
   * See {@link State}
   */
  int PLAYER_IDLE = 0;
  int PLAYER_PREPARING = 1;
  int PLAYER_PREPARED = 2;
  int PLAYER_BUFFERING = 3;
  int PLAYER_READY = 4;
  int PLAYER_ENDED = 5;

  /** See {@link MediaPlayer#start()} */
  void start() throws IllegalStateException;

  /** See {@link MediaPlayer#pause()} */
  void pause();

  /** See {@link MediaPlayer#stop()} */
  void stop();

  /** See {@link MediaPlayer#release()} */
  void release();

  /** See {@link MediaPlayer#reset()} */
  void reset();

  /** See {@link MediaPlayer#getDuration()} */
  long getDuration();

  /** See {@link MediaPlayer#getCurrentPosition()} */
  long getCurrentPosition();

  /** See {@link MediaPlayer#seekTo(int)} */
  void seekTo(long milliSec);

  /** See {@link MediaPlayer#isPlaying()} */
  boolean isPlaying();

  /** See {@link MediaPlayer#getAudioSessionId()} */
  int getAudioSessionId();

  /** See {@link MediaPlayer#setAudioSessionId(int)} */
  void setAudioSessionId(int audioSessionId);

  /** See {@link MediaPlayer#getVideoWidth()} */
  int getVideoWidth();

  /** See {@link MediaPlayer#getVideoHeight()} ()} */
  int getVideoHeight();

  /** See ExoPlayer#getBufferedPercentage() */
  int getBufferedPercentage();

  /** See {@link MediaPlayer#setOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener)} */
  void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener);

  /** See {@link MediaPlayer#setOnInfoListener(MediaPlayer.OnInfoListener)} */
  void setOnInfoListener(OnInfoListener listener);

  /** Combined with {@link State}. Used to setup MediaPlayer for custom Player widget. */
  void setPlayerStateChangeListener(OnPlayerStateChangeListener listener);

  /**
   * See {@link MediaPlayer#setDataSource(Context, Uri, Map)}
   */
  void setDataSource(Context context, Uri uri, Map<String, String> headers)
      throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

  /** See {@link MediaPlayer#setSurface(Surface)} */
  void setSurface(Surface surface);

  /** See {@link MediaPlayer#setAudioStreamType(int)} */
  void setAudioStreamType(int audioStreamType);

  /** See {@link MediaPlayer#setScreenOnWhilePlaying(boolean)} */
  void setScreenOnWhilePlaying(boolean screenOnWhilePlaying);

  /** See {@link MediaPlayer#prepareAsync()} */
  void prepareAsync() throws IllegalStateException;

  /** See {@link MediaPlayer#setVolume(float, float)} */
  void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume);

  /**
   * Common API for Media player widget. A custom VideoView should implement this interface to get
   * support from this library.
   */
  interface Player {

    /**
     * See {@link MediaPlayer#prepareAsync()}
     *
     * @param playWhenReady Immediately start playback when Ready.
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
     * See {@link VideoView#stopPlayback()} VideoView#stopPlayback()
     */
    void stop();

    /**
     * Release player's resource.
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

    /**
     * See {@link VideoView#getBufferPercentage()}
     *
     * @return current buffered percentage.
     */
    @IntRange(from = 0, to = 100) int getBufferPercentage();

    /**
     * Get the audio session id for the player used by this VideoView. This can be used to
     * apply audio effects to the audio track of a video.
     *
     * See {@link VideoView#getAudioSessionId()}
     *
     * @return The audio session, or 0 if there was an error.
     */
    int getAudioSessionId();

    void setBackgroundAudioEnabled(boolean enabled);

    /**
     * Set a media source for current Player.
     *
     * @param source the Media source to be played.
     */
    void setMedia(@NonNull Media source);

    /**
     * Simple version of {@link #setMedia(Media)}.
     *
     * @param uri Media source's Uri.
     */
    void setMedia(@NonNull Uri uri);

    /**
     * See {@link MediaPlayer#setVolume(float, float)}
     *
     * @param volume volume level.
     */
    void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume);

    void setOnPlayerStateChangeListener(OnPlayerStateChangeListener listener);
  }

  /**
   * Specified interface for Video Player widgets.
   */
  interface VideoPlayer extends Player {

    /**
     * @return current Video width, or 0 if there isn't any.
     */
    int getVideoWidth();

    /**
     * @return current Video height, or 0 if there isn't any.
     */
    int getVideoHeight();

    /**
     * Setup Video size change listener.
     */
    void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener);
  }

  /**
   * Common API for a Media Player Controller.
   */
  interface Controller {

    /** see {@link MediaController#hide()} */
    void hide();

    /** see {@link MediaController#show()} */
    void show();

    /** see {@link MediaController#show(int)} */
    void show(int timeout);

    /** see {@link MediaController#setMediaPlayer(MediaController.MediaPlayerControl)} */
    void setMediaPlayer(Player player);

    /** see {@link MediaController#setAnchorView(View)} */
    void setAnchorView(View anchorView);

    /** see {@link MediaController#setEnabled(boolean)} */
    void setEnabled(boolean enabled);

    /** see {@link MediaController#isShowing()} */
    boolean isShowing();
  }
}
