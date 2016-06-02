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
import android.media.MediaCodec;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.TimeRange;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.chunk.Format;
import im.ene.lab.toro.player.PlaybackException;
import im.ene.lab.toro.player.PlaybackInfo;
import im.ene.lab.toro.player.TrMediaPlayer;
import im.ene.lab.toro.player.listener.OnBufferingUpdateListener;
import im.ene.lab.toro.player.listener.OnCompletionListener;
import im.ene.lab.toro.player.listener.OnErrorListener;
import im.ene.lab.toro.player.listener.OnInfoListener;
import im.ene.lab.toro.player.listener.OnPreparedListener;
import im.ene.lab.toro.player.listener.OnSeekCompleteListener;
import im.ene.lab.toro.player.listener.OnVideoSizeChangedListener;
import java.io.IOException;
import java.util.Map;

/**
 * Created by eneim on 6/2/16.
 */
public class ExoMediaPlayer implements TrMediaPlayer, DemoPlayer.Listener {

  private static final String TAG = "ExoMediaPlayer";

  private int videoWidth;
  private int videoHeight;

  private int audioSessionId = 0;

  private final DemoPlayer delegate;

  public ExoMediaPlayer(DemoPlayer.RendererBuilder rendererBuilder) {
    this.delegate = new DemoPlayer(rendererBuilder);
    this.delegate.addListener(this);
  }

  @Override public void start() throws IllegalStateException {
    // playerControl.start();
    delegate.setPlayWhenReady(true);
  }

  @Override public void pause() {
    // playerControl.pause();
    delegate.setPlayWhenReady(false);
  }

  @Override public void stop() {
    delegate.stop();
  }

  @Override public void release() {
    delegate.release();
  }

  @Override public void reset() {
    delegate.release();
  }

  @Override public long getDuration() {
    return delegate.getDuration();
  }

  @Override public long getCurrentPosition() {
    return delegate.getCurrentPosition();
  }

  @Override public void seekTo(long milliSec) {
    delegate.seekTo(milliSec);
  }

  @Override public boolean isPlaying() {
    return /* playerControl.isPlaying() */ delegate.getPlayWhenReady();
  }

  @Override public void setAudioSessionId(int audioSessionId) {
    this.audioSessionId = audioSessionId;
  }

  @Override public int getAudioSessionId() {
    return this.audioSessionId;
  }

  @Override public int getVideoWidth() {
    return videoWidth;
  }

  @Override public int getVideoHeight() {
    return videoHeight;
  }

  @Override public void setOnPreparedListener(final OnPreparedListener listener) {
    if (listener == null) return;

    delegate.addListener(new ListenerAdapter() {
      @Override public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == DemoPlayer.STATE_READY) {
          listener.onPrepared(ExoMediaPlayer.this);
        }
      }
    });
  }

  @Override public void setOnVideoSizeChangedListener(final OnVideoSizeChangedListener listener) {
    if (listener == null) return;

    delegate.addListener(new ListenerAdapter() {
      @Override public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees,
          float pixelWidthHeightRatio) {
        listener.onVideoSizeChanged(ExoMediaPlayer.this, width, height);
      }
    });
  }

  @Override public void setOnCompletionListener(final OnCompletionListener listener) {
    if (listener == null) return;

    delegate.addListener(new ListenerAdapter() {
      @Override public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == DemoPlayer.STATE_ENDED) {
          listener.onCompletion(ExoMediaPlayer.this);
        }
      }
    });
  }

  @Override public void setOnErrorListener(final OnErrorListener listener) {
    if (listener == null) return;

    delegate.setInternalErrorListener(new DemoPlayer.InternalErrorListener() {
      @Override public void onRendererInitializationError(Exception e) {
        PlaybackException exception = new PlaybackException(e.getLocalizedMessage(), 0, 0);
        listener.onError(ExoMediaPlayer.this, exception);
      }

      @Override public void onAudioTrackInitializationError(AudioTrack.InitializationException e) {
        PlaybackException exception = new PlaybackException(e.getLocalizedMessage(), 0, 0);
        listener.onError(ExoMediaPlayer.this, exception);
      }

      @Override public void onAudioTrackWriteError(AudioTrack.WriteException e) {
        PlaybackException exception = new PlaybackException(e.getLocalizedMessage(), 0, 0);
        listener.onError(ExoMediaPlayer.this, exception);
      }

      @Override public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs,
          long elapsedSinceLastFeedMs) {
        PlaybackException exception = new PlaybackException("AudioTrackUnderrun", 0, 0);
        listener.onError(ExoMediaPlayer.this, exception);
      }

      @Override public void onDecoderInitializationError(
          MediaCodecTrackRenderer.DecoderInitializationException e) {
        PlaybackException exception = new PlaybackException(e.getLocalizedMessage(), 0, 0);
        listener.onError(ExoMediaPlayer.this, exception);
      }

      @Override public void onCryptoError(MediaCodec.CryptoException e) {
        PlaybackException exception = new PlaybackException(e.getLocalizedMessage(), 0, 0);
        listener.onError(ExoMediaPlayer.this, exception);
      }

      @Override public void onLoadError(int sourceId, IOException e) {
        PlaybackException exception = new PlaybackException(e.getLocalizedMessage(), 0, 0);
        listener.onError(ExoMediaPlayer.this, exception);
      }

      @Override public void onDrmSessionManagerError(Exception e) {
        PlaybackException exception = new PlaybackException(e.getLocalizedMessage(), 0, 0);
        listener.onError(ExoMediaPlayer.this, exception);
      }
    });
  }

  @Override public void setOnInfoListener(final OnInfoListener listener) {
    if (listener == null) return;

    delegate.setInfoListener(new DemoPlayer.InfoListener() {
      @Override public void onVideoFormatEnabled(Format format, int trigger, long mediaTimeMs) {
        String info = "{" +
            "name:" + "VideoFormatEnabled" + "," +
            "format:" + format.codecs + "," +
            "trigger:" + trigger + "," +
            "mediaTimeMs:" + mediaTimeMs + "}";
        listener.onInfo(ExoMediaPlayer.this, new PlaybackInfo(info));
      }

      @Override public void onAudioFormatEnabled(Format format, int trigger, long mediaTimeMs) {
        String info = "{" +
            "name:" + "AudioFormatEnabled" + "," +
            "format:" + format.codecs + "," +
            "trigger:" + trigger + "," +
            "mediaTimeMs:" + mediaTimeMs + "}";
        listener.onInfo(ExoMediaPlayer.this, new PlaybackInfo(info));
      }

      @Override public void onDroppedFrames(int count, long elapsed) {
        String info = "{" +
            "name:" + "DroppedFrames" + "," +
            "count:" + count + "," +
            "elapsed:" + elapsed + "}";
        listener.onInfo(ExoMediaPlayer.this, new PlaybackInfo(info));
      }

      @Override public void onBandwidthSample(int elapsedMs, long bytes, long bitrateEstimate) {
        String info = "{" +
            "name:" + "BandwidthSample" + "," +
            "elapsedMs:" + elapsedMs + "," +
            "bytes:" + bytes + "," +
            "bitrateEstimate:" + bitrateEstimate + "}";
        listener.onInfo(ExoMediaPlayer.this, new PlaybackInfo(info));
      }

      @Override
      public void onLoadStarted(int sourceId, long length, int type, int trigger, Format format,
          long mediaStartTimeMs, long mediaEndTimeMs) {
        String info = "{" +
            "name:" + "LoadStarted" + "," +
            "sourceId:" + sourceId + "," +
            "length:" + length + "," +
            "type:" + type + "}";
        listener.onInfo(ExoMediaPlayer.this, new PlaybackInfo(info));
      }

      @Override public void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger,
          Format format, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs,
          long loadDurationMs) {
        String info = "{" +
            "name:" + "LoadCompleted" + "," +
            "sourceId:" + sourceId + "}";
        listener.onInfo(ExoMediaPlayer.this, new PlaybackInfo(info));
      }

      @Override public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs,
          long initializationDurationMs) {
        String info = "{" +
            "name:" + "DecoderInitialized" + "}";
        listener.onInfo(ExoMediaPlayer.this, new PlaybackInfo(info));
      }

      @Override public void onAvailableRangeChanged(int sourceId, TimeRange availableRange) {
        String info = "{" +
            "name:" + "AvailableRangeChanged" + "}";
        listener.onInfo(ExoMediaPlayer.this, new PlaybackInfo(info));
      }
    });
  }

  @Override public void setOnBufferingUpdateListener(final OnBufferingUpdateListener listener) {
    if (listener == null) return;

    delegate.addListener(new ListenerAdapter() {
      @Override public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == DemoPlayer.STATE_BUFFERING) {
          listener.onBufferingUpdate(ExoMediaPlayer.this, delegate.getBufferedPercentage());
        }
      }
    });
  }

  @Override public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
    if (listener == null) return;

    delegate.addListener(new ListenerAdapter() {
      @Override public void onStateChanged(boolean playWhenReady, int playbackState) {
        super.onStateChanged(playWhenReady, playbackState);
        Log.d(TAG, "onStateChanged() called with: "
            + "playWhenReady = ["
            + playWhenReady
            + "], playbackState = ["
            + playbackState
            + "]");
      }
    });
  }

  @Override public void setDataSource(Context context, Uri uri, Map<String, String> headers)
      throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {

  }

  @Override public void setSurface(Surface surface) {
    delegate.setSurface(surface);
  }

  @Override public void setAudioStreamType(int audioStreamType) {
    // left blank
  }

  @Override public void setScreenOnWhilePlaying(boolean screenOnWhilePlaying) {
    // left blank
  }

  @Override public void prepareAsync() throws IllegalStateException {
    delegate.prepare();
  }

  @Override public void onStateChanged(boolean playWhenReady, int playbackState) {

  }

  @Override public void onError(Exception e) {

  }

  @Override public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees,
      float pixelWidthHeightRatio) {
    videoHeight = height;
    videoWidth = width;
  }

  private static class ListenerAdapter implements DemoPlayer.Listener {

    @Override public void onStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override public void onError(Exception e) {

    }

    @Override public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees,
        float pixelWidthHeightRatio) {

    }
  }
}
