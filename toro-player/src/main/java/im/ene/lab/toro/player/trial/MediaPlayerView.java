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

package im.ene.lab.toro.player.trial;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.FloatRange;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import im.ene.lab.toro.player.MediaSource;
import im.ene.lab.toro.player.TrMediaPlayer;
import im.ene.lab.toro.player.internal.ExoMediaPlayer;
import im.ene.lab.toro.player.internal.RendererBuilderFactory;

/**
 * Created by eneim on 6/4/16.
 */
public class MediaPlayerView extends TextureView
    implements MediaPlayerWidget, TrMediaPlayer.IMediaPlayer {

  private AudioCapabilitiesReceiver.Listener audioCapabilitiesListener =
      new AudioCapabilitiesReceiver.Listener() {
        @Override public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
          maybeNotifyAudioCapabilitiesChanged(audioCapabilities);
        }
      };

  private SurfaceTextureListener textureListener = new SurfaceTextureListener() {
    @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
      MediaPlayerView.this.surface = new Surface(surface);
      if (player != null) {
        player.setSurface(MediaPlayerView.this.surface);
        if (!playerNeedsPrepare) {
          player.setPlayWhenReady(playRequested);
        }
      }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
      if (player != null) {
        player.blockingClearSurface();
      }
      MediaPlayerView.this.surface = null;
      return true;
    }

    @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
  };

  private ExoMediaPlayer.Listener exoPlayerListener = new ExoMediaPlayer.Listener() {
    @Override public void onStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override public void onError(Exception e) {

    }

    @Override public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees,
        float pixelWidthHeightRatio) {
      if (getSurfaceTexture() != null) {
        getSurfaceTexture().setDefaultBufferSize(width, height);
        requestLayout();
      }
    }
  };

  private Uri mUri;
  private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
  private AudioCapabilities audioCapabilities;
  private ExoMediaPlayer player;
  private Surface surface;

  private boolean playerNeedsPrepare;
  private boolean playRequested = false;

  public MediaPlayerView(Context context) {
    this(context, null);
  }

  public MediaPlayerView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MediaPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public MediaPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize(context);
    setSurfaceTextureListener(textureListener);
    getSurfaceTexture();
  }

  private void initialize(Context context) {

  }

  private void releasePlayer() {
    playRequested = false;
    if (player != null) {
      player.release();
      player = null;
    }
  }

  private void preparePlayer(boolean playWhenReady) {
    if (player == null) {
      player = (ExoMediaPlayer) TrMediaPlayer.Factory.createExoPlayer(
          RendererBuilderFactory.createRendererBuilder(getContext(), mUri));
      player.addListener(exoPlayerListener);
      // player.setCaptionListener(this);
      // player.setMetadataListener(this);
      // player.seekTo(playerPosition);
      playerNeedsPrepare = true;
      // player.addListener(eventLogger);
      // player.setInfoListener(eventLogger);
      // player.setInternalErrorListener(eventLogger);
    }

    if (playerNeedsPrepare) {
      player.prepare();
      playerNeedsPrepare = false;
    }
    player.setSurface(surface);
    player.setPlayWhenReady(playWhenReady);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (audioCapabilitiesReceiver == null) {
      audioCapabilitiesReceiver =
          new AudioCapabilitiesReceiver(getContext(), audioCapabilitiesListener);
    }
    audioCapabilitiesReceiver.register();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    releasePlayer();
    audioCapabilitiesReceiver.unregister();
    audioCapabilitiesReceiver = null;
  }

  private void maybeNotifyAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
    if (this.audioCapabilities == audioCapabilities) {
      return;
    }

    if (player == null) {
      return;
    }

    boolean backgrounded = player.getBackgrounded();
    boolean playWhenReady = player.getPlayWhenReady();
    releasePlayer();
    preparePlayer(playWhenReady);
    player.setBackgrounded(backgrounded);
  }

  @Override public void play() {
    playRequested = true;
    if (player != null) {
      player.start();
    }
  }

  @Override public void pause() {
    playRequested = false;
    if (player != null) {
      player.start();
    }
  }

  @Override public void seekTo(long milliSec) {
    if (player != null) {
      player.seekTo(milliSec);
    }
  }

  @Override public void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume) {
    if (player != null) {
      player.setVolume(volume);
    }
  }

  // TODO Handle permission state
  @Override public void setMediaSource(MediaSource source) {
    if (this.mUri == source.mediaUri) {
      return;
    }

    this.mUri = source.mediaUri;
    releasePlayer();
    if (player == null) {
      preparePlayer(playRequested);
    } else {
      player.setBackgrounded(false);
    }
  }

  @Override public void setMediaUri(Uri uri) {
    setMediaSource(new MediaSource(uri));
  }

  // IMediaPlayer

  @Override public void start() {
    play();
  }

  @Override public void start(long position) {
    seekTo(position);
    start();
  }

  @Override public long getDuration() {
    return player != null ? player.getDuration() : ExoPlayer.UNKNOWN_TIME;
  }

  @Override public long getCurrentPosition() {
    return player != null ? player.getCurrentPosition() : ExoPlayer.UNKNOWN_TIME;
  }

  @Override public boolean isPlaying() {
    return player != null && player.isPlaying();
  }

  @Override public int getBufferPercentage() {
    return player != null ? player.getBufferedPercentage() : 0;
  }

  @Override public boolean canPause() {
    return true;
  }

  @Override public boolean canSeekBackward() {
    return true;
  }

  @Override public boolean canSeekForward() {
    return true;
  }

  @Override public int getAudioSessionId() {
    return 0;
  }
}
