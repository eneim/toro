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

package im.ene.lab.toro.ext.layeredvideo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.util.Util;
import im.ene.lab.toro.media.Cineer;
import im.ene.lab.toro.media.Media;
import im.ene.lab.toro.media.OnPlayerStateChangeListener;
import im.ene.lab.toro.media.OnVideoSizeChangedListener;
import im.ene.lab.toro.media.PlaybackException;
import im.ene.lab.toro.media.State;
import im.ene.lab.toro.player.Video;
import im.ene.lab.toro.player.internal.ExoMediaPlayer;

/**
 * Created by eneim on 6/28/16.
 *
 * A player ViewGroup which attempts to mimic {@link SimpleVideoPlayer} implementation.
 */
public class MultiLayerVideoPlayerView extends FrameLayout implements Cineer.VideoPlayer {

  private Video mMedia;
  private AudioCapabilitiesReceiver mAudioCapabilitiesReceiver;
  private AudioCapabilities mAudioCapabilities;
  private SimpleVideoPlayer mMediaPlayer;
  private OnPlayerStateChangeListener mPlayerStateChangeListener;
  private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
  private long mPlayerPosition;
  private boolean mPlayRequested = false;
  private int mPlaybackState;
  private int mVideoWidth;
  private int mVideoHeight;
  private final ExoMediaPlayer.Listener playerListenerDelegate = new ExoMediaPlayer.Listener() {
    @Override public void onStateChanged(Cineer player, boolean playWhenReady, int playbackState) {
      mPlaybackState = playbackState;
      setKeepScreenOn(isInPlayableState());
    }

    @Override public void onError(Exception e) {
      // Do nothing here
    }

    @Override public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees,
        float pixelWidthHeightRatio) {
      mVideoHeight = height;
      mVideoWidth = width;
    }
  };

  private OnVideoSizeChangedListener videoSizeChangedListenerDelegate =
      new OnVideoSizeChangedListener() {
        @Override public void onVideoSizeChanged(Cineer mp, int width, int height) {
          if (mOnVideoSizeChangedListener != null) {
            mOnVideoSizeChangedListener.onVideoSizeChanged(mp, width, height);
          }
        }
      };

  private OnPlayerStateChangeListener stateChangeListenerDelegate =
      new OnPlayerStateChangeListener() {
        @Override public void onPlayerStateChanged(Cineer player, boolean playWhenReady,
            @State int playbackState) {
          if (playbackState == Cineer.PLAYER_ENDED) {
            mPlayRequested = false;
            releasePlayer();
            mPlayerPosition = 0;
          }

          if (mPlayerStateChangeListener != null) {
            mPlayerStateChangeListener.onPlayerStateChanged(player, playWhenReady, playbackState);
          }
        }

        @Override public boolean onPlayerError(Cineer player, PlaybackException error) {
          if (mPlayerStateChangeListener != null) {
            mPlayerStateChangeListener.onPlayerError(player, error);
          }

          return true;
        }
      };

  private AudioCapabilitiesReceiver.Listener audioCapabilitiesListener =
      new AudioCapabilitiesReceiver.Listener() {
        @Override public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
          maybeNotifyAudioCapabilitiesChanged(audioCapabilities);
        }
      };

  public MultiLayerVideoPlayerView(Context context) {
    this(context, null);
  }

  public MultiLayerVideoPlayerView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MultiLayerVideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setBackgroundResource(android.R.color.black);
    setFocusable(true);
    setFocusableInTouchMode(true);
    requestFocus();
    this.mAudioCapabilities = AudioCapabilities.getCapabilities(context);
  }

  private void maybeNotifyAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
    if (this.mAudioCapabilities == audioCapabilities) {
      return;
    }

    if (mMediaPlayer == null) {
      return;
    }

    boolean backgrounded = mMediaPlayer.getPlayer().getBackgrounded();
    boolean playWhenReady = mMediaPlayer.getPlayer().getPlayWhenReady();
    releasePlayer();
    preparePlayer(playWhenReady);
    mMediaPlayer.getPlayer().setBackgrounded(backgrounded);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    mAudioCapabilitiesReceiver =
        new AudioCapabilitiesReceiver(getContext(), audioCapabilitiesListener);
    mAudioCapabilitiesReceiver.register();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    try {
      mAudioCapabilitiesReceiver.unregister();
      mAudioCapabilitiesReceiver = null;
    } catch (IllegalArgumentException er) {
      // Have no idea, it crash by this Exception sometime.
      er.printStackTrace();
    }
    mPlayerPosition = 0;
    mAudioCapabilities = null;
    releasePlayer();
  }

  private boolean isInPlayableState() {
    return (mPlaybackState != Cineer.PLAYER_IDLE)
        && (mPlaybackState != Cineer.PLAYER_PREPARING)
        && (mPlaybackState != Cineer.PLAYER_ENDED);
  }

  @TargetApi(23) private boolean requiresPermission(Uri uri) {
    return Util.SDK_INT >= 23
        && Util.isLocalFileUri(uri)
        && ActivityCompat.checkSelfPermission(getContext(),
        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
  }

  @SuppressWarnings("ConstantConditions") @Override public void setMedia(@NonNull Media media) {
    if (media == null || !(media instanceof Video)) {
      throw new IllegalArgumentException("Media source must be a valid Video");
    }

    if (requiresPermission(media.getMediaUri())) {
      throw new RuntimeException("Permission to read this URI is not granted. "
          + "Consider to request READ_EXTERNAL_STORAGE permission.");
    }

    if (this.mMedia == media) {
      return;
    }

    this.mPlayerPosition = 0;
    this.mMedia = (Video) media;
    mPlayRequested = false;
    releasePlayer();
    preparePlayer(mPlayRequested);
  }

  @Deprecated @Override public void setMedia(Uri uri) {
    throw new IllegalArgumentException(
        "This View doesn't support naive Media Uri. Use a Video instead.");
  }

  @Override public void releasePlayer() {
    if (mMediaPlayer != null) {
      mPlayerPosition = mMediaPlayer.getCurrentPosition();
      mMediaPlayer.getPlayer().removeListener(playerListenerDelegate);
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
  }

  @Override public void preparePlayer(boolean playWhenReady) {
    if (mMedia == null) {
      return;
    }

    if (mMediaPlayer == null) {
      mMediaPlayer =
          new SimpleVideoPlayer((Activity) getContext(), this, mMedia, mMedia.getVideoTitle(),
              mPlayRequested);
      mMediaPlayer.addPlaybackListener(playerListenerDelegate);
      mMediaPlayer.getPlayer().setPlayerStateChangeListener(stateChangeListenerDelegate);
      mMediaPlayer.getPlayer().setOnVideoSizeChangedListener(videoSizeChangedListenerDelegate);
      mMediaPlayer.getPlayer().seekTo(mPlayerPosition);
    }

    mMediaPlayer.getPlayer().setPlayWhenReady(playWhenReady);
  }

  @Override public void start() {
    mPlayRequested = true;
    if (mMediaPlayer == null) {
      preparePlayer(true);
    } else {
      mMediaPlayer.play();
    }
  }

  @Override public void pause() {
    mPlayRequested = false;
    if (mMediaPlayer != null) {
      mMediaPlayer.pause();
    }
  }

  @Override public void stop() {
    mPlayRequested = false;
    releasePlayer();
    mPlayerPosition = 0;
  }

  @Override public long getDuration() {
    return mMediaPlayer != null ? mMediaPlayer.getDuration() : ExoPlayer.UNKNOWN_TIME;
  }

  @Override public long getCurrentPosition() {
    return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : ExoPlayer.UNKNOWN_TIME;
  }

  @Override public void seekTo(long pos) {
    if (mMediaPlayer != null && mMediaPlayer.getPlayer() != null) {
      mMediaPlayer.getPlayer().seekTo(pos);
    }
  }

  @Override public boolean isPlaying() {
    return mMediaPlayer != null && mMediaPlayer.getPlayer() != null && mMediaPlayer.getPlayer()
        .isPlaying();
  }

  @Override public int getBufferPercentage() {
    return mMediaPlayer != null && mMediaPlayer.getPlayer() != null ? mMediaPlayer.getPlayer()
        .getBufferedPercentage() : 0;
  }

  @Override public int getAudioSessionId() {
    return mMediaPlayer != null && mMediaPlayer.getPlayer() != null ? mMediaPlayer.getPlayer()
        .getAudioSessionId() : 0;
  }

  @Override public void setBackgroundAudioEnabled(boolean enabled) {
    if (mMediaPlayer != null && mMediaPlayer.getPlayer() != null) {
      mMediaPlayer.getPlayer().setBackgrounded(enabled);
    }
  }

  @Override public void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume) {
    if (mMediaPlayer != null && mMediaPlayer.getPlayer() != null) {
      mMediaPlayer.getPlayer().setVolume(volume);
    }
  }

  @Override public int getVideoWidth() {
    return mVideoWidth;
  }

  @Override public int getVideoHeight() {
    return mVideoHeight;
  }

  @Override public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
    this.mOnVideoSizeChangedListener = listener;
  }

  @Override public void setOnPlayerStateChangeListener(OnPlayerStateChangeListener listener) {
    this.mPlayerStateChangeListener = listener;
  }
}
