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

package im.ene.lab.toro.ext;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.FloatRange;
import android.view.Surface;
import im.ene.lab.toro.media.Cineer;
import im.ene.lab.toro.media.OnInfoListener;
import im.ene.lab.toro.media.OnPlayerStateChangeListener;
import im.ene.lab.toro.media.OnVideoSizeChangedListener;
import im.ene.lab.toro.media.PlaybackException;
import im.ene.lab.toro.media.PlaybackInfo;
import java.io.IOException;
import java.util.Map;

/**
 * Created by eneim on 6/2/16.
 *
 * Implementation for {@link Cineer} backed by Android original {@link MediaPlayer}
 */
public final class NativeMediaPlayer implements Cineer, MediaPlayer.OnBufferingUpdateListener {

  final MediaPlayer mediaPlayer;
  int bufferedPercent = 0;

  public NativeMediaPlayer() {
    this.mediaPlayer = new MediaPlayer();
    this.mediaPlayer.setOnBufferingUpdateListener(this);
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

  @Override public int getBufferedPercentage() {
    return bufferedPercent;
  }

  @Override public void setOnVideoSizeChangedListener(final OnVideoSizeChangedListener listener) {
    if (listener == null) return;

    mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
      @Override public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        listener.onVideoSizeChanged(NativeMediaPlayer.this, width, height);
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

  @Override public void setPlayerStateChangeListener(final OnPlayerStateChangeListener listener) {
    if (listener == null) {
      return;
    }

    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override public void onPrepared(MediaPlayer mp) {
        listener.onPlayerStateChanged(NativeMediaPlayer.this, false, PLAYER_PREPARED);
      }
    });

    mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
      @Override public boolean onError(MediaPlayer mp, int what, int extra) {
        return listener.onPlayerError(NativeMediaPlayer.this, new PlaybackException(what, extra));
      }
    });

    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override public void onCompletion(MediaPlayer mp) {
        listener.onPlayerStateChanged(NativeMediaPlayer.this, false, PLAYER_ENDED);
      }
    });

    mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
      @Override public void onBufferingUpdate(MediaPlayer mp, int percent) {
        bufferedPercent = percent;
        listener.onPlayerStateChanged(NativeMediaPlayer.this, false, PLAYER_BUFFERING);
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

  @Override public void onBufferingUpdate(MediaPlayer mp, int percent) {
    this.bufferedPercent = percent;
  }
}
