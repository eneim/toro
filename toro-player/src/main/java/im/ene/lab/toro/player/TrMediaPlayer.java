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

package im.ene.lab.toro.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.view.Surface;
import android.view.View;
import im.ene.lab.toro.player.internal.ExoMediaPlayer;
import im.ene.lab.toro.player.internal.NativeMediaPlayer;
import im.ene.lab.toro.player.listener.OnBufferingUpdateListener;
import im.ene.lab.toro.player.listener.OnCompletionListener;
import im.ene.lab.toro.player.listener.OnErrorListener;
import im.ene.lab.toro.player.listener.OnInfoListener;
import im.ene.lab.toro.player.listener.OnPreparedListener;
import im.ene.lab.toro.player.listener.OnVideoSizeChangedListener;
import java.io.IOException;
import java.util.Map;

/**
 * Created by eneim on 6/2/16.
 */
public interface TrMediaPlayer {

  interface IMediaPlayer /* extends MediaController.MediaPlayerControl */ {

    void start();

    void start(long position);

    void pause();

    void stop();

    /* int */ long getDuration();

    /* int */ long getCurrentPosition();

    void seekTo(/* int */ long pos);

    boolean isPlaying();

    @IntRange(from = 0, to = 100) int getBufferPercentage();

    /**
     * Get the audio session id for the player used by this VideoView. This can be used to
     * apply audio effects to the audio track of a video.
     *
     * @return The audio session, or 0 if there was an error.
     */
    int getAudioSessionId();

    void setBackgroundAudioEnabled(boolean enabled);

    void setMediaSource(@NonNull MediaSource source);

    void setMediaUri(Uri uri);

    void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume);
  }

  interface Controller {

    /** see {@link android.widget.MediaController#hide()} */
    void hide();

    /** see {@link android.widget.MediaController#show()} */
    void show();

    /** see {@link android.widget.MediaController#show(int)} */
    void show(int timeout);

    /** see {@link android.widget.MediaController#setMediaPlayer(android.widget.MediaController.MediaPlayerControl)} */
    void setMediaPlayer(IMediaPlayer player);

    /** see {@link android.widget.MediaController#setAnchorView(View)} */
    void setAnchorView(View anchorView);

    /** see {@link android.widget.MediaController#setEnabled(boolean)} */
    void setEnabled(boolean enabled);

    /** see {@link android.widget.MediaController#isShowing()} */
    boolean isShowing();
  }

  class Factory {

    public static TrMediaPlayer createNativePlayer() {
      return new NativeMediaPlayer();
    }

    public static TrMediaPlayer createExoPlayer(ExoMediaPlayer.RendererBuilder builder) {
      return new ExoMediaPlayer(builder);
    }
  }

  /** see {@link MediaPlayer#start()} */
  void start() throws IllegalStateException;

  /** see {@link MediaPlayer#pause()} */
  void pause();

  /** see {@link MediaPlayer#stop()} */
  void stop();

  /** see {@link MediaPlayer#release()} */
  void release();

  /** see {@link MediaPlayer#reset()} */
  void reset();

  /** see {@link MediaPlayer#getDuration()} */
  long getDuration();

  /** see {@link MediaPlayer#getCurrentPosition()} */
  long getCurrentPosition();

  /** see {@link MediaPlayer#seekTo(int)} */
  void seekTo(long milliSec);

  /** see {@link MediaPlayer#isPlaying()} */
  boolean isPlaying();

  /** see {@link MediaPlayer#setAudioSessionId(int)} */
  void setAudioSessionId(int audioSessionId);

  /** see {@link MediaPlayer#getAudioSessionId()} */
  int getAudioSessionId();

  /** see {@link MediaPlayer#getVideoWidth()} */
  int getVideoWidth();

  /** see {@link MediaPlayer#getVideoHeight()} ()} */
  int getVideoHeight();

  /** see {@link MediaPlayer#setOnPreparedListener(MediaPlayer.OnPreparedListener)} */
  void setOnPreparedListener(OnPreparedListener listener);

  /** see {@link MediaPlayer#setOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener)} */
  void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener);

  /** see {@link MediaPlayer#setOnCompletionListener(MediaPlayer.OnCompletionListener)} */
  void setOnCompletionListener(OnCompletionListener listener);

  /** see {@link MediaPlayer#setOnErrorListener(MediaPlayer.OnErrorListener)} */
  void setOnErrorListener(OnErrorListener listener);

  /** see {@link MediaPlayer#setOnInfoListener(MediaPlayer.OnInfoListener)} */
  void setOnInfoListener(OnInfoListener listener);

  /** see {@link MediaPlayer#setOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener)} */
  void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);

  /** see {@link MediaPlayer#setDataSource(Context, Uri, Map)} */
  void setDataSource(Context context, Uri uri, Map<String, String> headers)
      throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

  /** see {@link MediaPlayer#setSurface(Surface)} */
  void setSurface(Surface surface);

  /** see {@link MediaPlayer#setAudioStreamType(int)} */
  void setAudioStreamType(int audioStreamType);

  /** see {@link MediaPlayer#setScreenOnWhilePlaying(boolean)} */
  void setScreenOnWhilePlaying(boolean screenOnWhilePlaying);

  /** see {@link MediaPlayer#prepareAsync()} */
  void prepareAsync() throws IllegalStateException;

  /** see {@link MediaPlayer#setVolume(float, float)} */
  void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume);
}
