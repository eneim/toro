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

package im.ene.lab.toro.player.internal;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.FloatRange;
import android.view.Surface;
import im.ene.lab.toro.player.PlaybackException;
import im.ene.lab.toro.player.PlaybackInfo;
import im.ene.lab.toro.player.TrMediaPlayer;
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
 *
 * Implementation for {@link TrMediaPlayer} backed by Android original {@link MediaPlayer}
 */
public class NativeMediaPlayer implements TrMediaPlayer {

  final MediaPlayer mediaPlayer;

  public NativeMediaPlayer() {
    this.mediaPlayer = new MediaPlayer();
  }

  @Override public void start() throws IllegalStateException {
    mediaPlayer.start();
  }

  @Override public void pause() {
    mediaPlayer.pause();
  }

  @Override public void stop() {
    mediaPlayer.stop();
  }

  @Override public void release() {
    mediaPlayer.release();
  }

  @Override public void reset() {
    mediaPlayer.reset();
  }

  @Override public long getDuration() {
    return mediaPlayer.getDuration();
  }

  @Override public long getCurrentPosition() {
    return mediaPlayer.getCurrentPosition();
  }

  @Override public void seekTo(long milliSec) {
    mediaPlayer.seekTo((int) milliSec);
  }

  @Override public boolean isPlaying() {
    return mediaPlayer.isPlaying();
  }

  @Override public void setAudioSessionId(int audioSessionId) {
    mediaPlayer.setAudioSessionId(audioSessionId);
  }

  @Override public int getAudioSessionId() {
    return mediaPlayer.getAudioSessionId();
  }

  @Override public int getVideoWidth() {
    return mediaPlayer.getVideoWidth();
  }

  @Override public int getVideoHeight() {
    return mediaPlayer.getVideoHeight();
  }

  @Override public void setOnPreparedListener(final OnPreparedListener listener) {
    if (listener == null) return;

    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override public void onPrepared(MediaPlayer mp) {
        listener.onPrepared(NativeMediaPlayer.this);
      }
    });
  }

  @Override public void setOnVideoSizeChangedListener(final OnVideoSizeChangedListener listener) {
    if (listener == null) return;

    mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
      @Override public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        listener.onVideoSizeChanged(NativeMediaPlayer.this, width, height);
      }
    });
  }

  @Override public void setOnCompletionListener(final OnCompletionListener listener) {
    if (listener == null) return;

    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override public void onCompletion(MediaPlayer mp) {
        listener.onCompletion(NativeMediaPlayer.this);
      }
    });
  }

  @Override public void setOnErrorListener(final OnErrorListener listener) {
    if (listener == null) return;

    mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
      @Override public boolean onError(MediaPlayer mp, int what, int extra) {
        return listener.onError(NativeMediaPlayer.this, new PlaybackException(what, extra));
      }
    });
  }

  @Override public void setOnInfoListener(final OnInfoListener listener) {
    if (listener == null) return;

    mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
      @Override public boolean onInfo(MediaPlayer mp, int what, int extra) {
        String info = "{what:" + what + ", extra:" + extra + "}";
        return listener.onInfo(NativeMediaPlayer.this, new PlaybackInfo(info));
      }
    });
  }

  @Override public void setOnBufferingUpdateListener(final OnBufferingUpdateListener listener) {
    if (listener == null) return;

    mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
      @Override public void onBufferingUpdate(MediaPlayer mp, int percent) {
        listener.onBufferingUpdate(NativeMediaPlayer.this, percent);
      }
    });
  }

  @Override public void setDataSource(Context context, Uri uri, Map<String, String> headers)
      throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
    mediaPlayer.setDataSource(context, uri, headers);
  }

  @Override public void setSurface(Surface surface) {
    mediaPlayer.setSurface(surface);
  }

  @Override public void setAudioStreamType(int audioStreamType) {
    mediaPlayer.setAudioStreamType(audioStreamType);
  }

  @Override public void setScreenOnWhilePlaying(boolean screenOnWhilePlaying) {
    mediaPlayer.setScreenOnWhilePlaying(screenOnWhilePlaying);
  }

  @Override public void prepareAsync() throws IllegalStateException {
    mediaPlayer.prepareAsync();
  }

  @Override public void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume) {
    mediaPlayer.setVolume(volume, volume);
  }
}
