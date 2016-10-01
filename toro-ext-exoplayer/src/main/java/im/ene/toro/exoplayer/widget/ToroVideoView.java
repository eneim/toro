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

package im.ene.toro.exoplayer.widget;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.util.Util;
import im.ene.toro.exoplayer.dev.ToroExoPlayer;
import im.ene.toro.exoplayer.dev.LastMomentCallback;
import im.ene.toro.exoplayer.dev.Media;
import im.ene.toro.exoplayer.dev.OnInfoListener;
import im.ene.toro.exoplayer.dev.OnPlayerStateChangeListener;
import im.ene.toro.exoplayer.dev.OnVideoSizeChangedListener;
import im.ene.toro.exoplayer.dev.PlaybackException;
import im.ene.toro.exoplayer.dev.PlaybackInfo;
import im.ene.toro.exoplayer.dev.State;
import im.ene.toro.exoplayer.BuildConfig;
import im.ene.toro.exoplayer.internal.ExoMediaPlayer;
import im.ene.toro.exoplayer.internal.RendererBuilderFactory;
import java.util.List;

/**
 * Created by eneim on 6/4/16.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)  //
public class ToroVideoView extends TextureView implements ToroExoPlayer.VideoPlayer {

  /**
   * The surface view will not resize itself if the fractional difference
   * between its default aspect ratio and the aspect ratio of the video falls
   * below this threshold.
   */
  private static final float MAX_ASPECT_RATIO_DEFORMATION_PERCENT = 0.01f;

  private AudioCapabilitiesReceiver.Listener audioCapabilitiesListener =
      new AudioCapabilitiesReceiver.Listener() {
        @Override public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
          maybeNotifyAudioCapabilitiesChanged(audioCapabilities);
        }
      };

  private SurfaceTextureListener surfaceTextureListener = new SurfaceTextureListener() {
    @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
      ToroVideoView.this.mSurface = new Surface(surface);
      if (mMediaPlayer != null) {
        mMediaPlayer.setSurface(ToroVideoView.this.mSurface);
        if (!mPlayerNeedsPrepare) {
          mMediaPlayer.seekTo(mPlayerPosition);
          mMediaPlayer.setPlayWhenReady(mPlayRequested);
        }
      } else {
        preparePlayer(mPlayRequested);
      }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
      if (!mBackgroundAudioEnabled) {
        releasePlayer();
      } else {
        if (mMediaPlayer != null) {
          mMediaPlayer.setBackgrounded(true);
        }
      }

      if (mMediaPlayer != null) {
        mMediaPlayer.blockingClearSurface();
      }
      mPlayerNeedsPrepare = true;
      ToroVideoView.this.mSurface = null;
      return true;
    }

    @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
  };

  private final ExoMediaPlayer.Listener playerListener = new ExoMediaPlayer.Listener() {
    @Override public void onStateChanged(ToroExoPlayer player, boolean playWhenReady, int playbackState) {
      mPlaybackState = playbackState;
      setKeepScreenOn(isInPlayableState());
    }

    @Override public void onError(Exception e) {
      // Do nothing here
    }

    @Override public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees,
        float pixelWidthHeightRatio) {
      videoWidth = width;
      videoHeight = height;
      if (getSurfaceTexture() != null) {
        setVideoWidthHeightRatio(height == 0 ? 1 : (width * pixelWidthHeightRatio) / height);
      }

      if (onVideoSizeChangedListener != null) {
        onVideoSizeChangedListener.onVideoSizeChanged(mMediaPlayer, width, height);
      }
    }
  };

  private OnPlayerStateChangeListener stateChangeListenerDelegate =
      new OnPlayerStateChangeListener() {
        @Override public void onPlayerStateChanged(ToroExoPlayer player, boolean playWhenReady,
            @State int playbackState) {
          if (playbackState == ToroExoPlayer.PLAYER_ENDED) {
            mPlayRequested = false;
            releasePlayer();
            mPlayerPosition = 0;
          }

          if (mPlayerStateChangeListener != null) {
            mPlayerStateChangeListener.onPlayerStateChanged(player, playWhenReady, playbackState);
          }
        }

        @Override public boolean onPlayerError(ToroExoPlayer player, PlaybackException error) {
          if (mPlayerStateChangeListener != null) {
            mPlayerStateChangeListener.onPlayerError(player, error);
          }

          return true;
        }
      };

  private boolean isInPlayableState() {
    return !mPlayerNeedsPrepare && (mPlaybackState != ToroExoPlayer.PLAYER_IDLE) && (mPlaybackState
        != ToroExoPlayer.PLAYER_PREPARING) && (mPlaybackState != ToroExoPlayer.PLAYER_ENDED);
  }

  private Media mMedia;
  private AudioCapabilitiesReceiver mAudioCapabilitiesReceiver;
  private AudioCapabilities mAudioCapabilities;
  private ExoMediaPlayer mMediaPlayer;
  private Surface mSurface;

  /**
   * The ratio of the width and height of the video.
   */
  private float mVideoWidthHeightAspectRatio;
  private long mPlayerPosition;

  private int videoWidth;
  private int videoHeight;

  private int mPlaybackState;
  private boolean mPlayerNeedsPrepare;
  private boolean mPlayRequested = false;
  private boolean mBackgroundAudioEnabled = false;

  private OnPlayerStateChangeListener mPlayerStateChangeListener;
  private OnVideoSizeChangedListener onVideoSizeChangedListener;
  private OnInfoListener mOnInfoListener;
  private LastMomentCallback lastMomentCallback;

  // DEBUG
  private EventLogger mEventLogger;

  private ExoMediaPlayer.CaptionListener mCaptionListener;
  private ExoMediaPlayer.Id3MetadataListener mId3MetadataListener;

  private ExoMediaPlayerHelper mExoMediaPlayerHelper = new ExoMediaPlayerHelper() {
    @Override public void onCues(List<Cue> cues) {
      if (mCaptionListener != null) {
        mCaptionListener.onCues(cues);
      }
    }

    @Override public void onId3Metadata(List<Id3Frame> id3Frames) {
      if (mId3MetadataListener != null) {
        mId3MetadataListener.onId3Metadata(id3Frames);
      }
    }
  };

  @Override public void setOnPlayerStateChangeListener(OnPlayerStateChangeListener listener) {
    this.mPlayerStateChangeListener = listener;
  }

  public void setOnInfoListener(OnInfoListener onInfoListener) {
    this.mOnInfoListener = onInfoListener;
  }

  public void setCaptionListener(ExoMediaPlayer.CaptionListener listener) {
    this.mCaptionListener = listener;
  }

  public void setId3MetadataListener(ExoMediaPlayer.Id3MetadataListener listener) {
    this.mId3MetadataListener = listener;
  }

  public void setLastMomentCallback(LastMomentCallback lastMomentCallback) {
    this.lastMomentCallback = lastMomentCallback;
  }

  private OnInfoListener onInfoListenerDelegate = new OnInfoListener() {
    @Override public boolean onInfo(ToroExoPlayer mp, PlaybackInfo info) {
      return mOnInfoListener != null && mOnInfoListener.onInfo(mp, info);
    }
  };

  /**
   * Resize the view based on the width and height specifications.
   *
   * @param widthMeasureSpec The specified width.
   * @param heightMeasureSpec The specified height.
   */
  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int width = getMeasuredWidth();
    int height = getMeasuredHeight();

    int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

    if (mVideoWidthHeightAspectRatio != 0) {
      if (widthSpecMode == MeasureSpec.EXACTLY) {
        height = (int) (width / mVideoWidthHeightAspectRatio);
      } else if (heightSpecMode == MeasureSpec.EXACTLY) {
        width = (int) (height * mVideoWidthHeightAspectRatio);
      } else {
        float viewAspectRatio = (float) width / height;
        float aspectDeformation = mVideoWidthHeightAspectRatio / viewAspectRatio - 1;
        if (aspectDeformation > MAX_ASPECT_RATIO_DEFORMATION_PERCENT) {
          width = (int) (height * mVideoWidthHeightAspectRatio);
        } else if (aspectDeformation < -MAX_ASPECT_RATIO_DEFORMATION_PERCENT) {
          height = (int) (width / mVideoWidthHeightAspectRatio);
        }
      }
    }

    setMeasuredDimension(width, height);
  }

  /**
   * Set the aspect ratio that this {@link ToroVideoView} should satisfy.
   *
   * {@code Deprecated}, we gonna support ScaleType
   *
   * @param widthHeightRatio The width to height ratio.
   */
  @Deprecated public final void setVideoWidthHeightRatio(float widthHeightRatio) {
    if (this.mVideoWidthHeightAspectRatio != widthHeightRatio) {
      this.mVideoWidthHeightAspectRatio = widthHeightRatio;
      requestLayout();
    }
  }

  public ToroVideoView(Context context) {
    super(context);
    initialize(context);
  }

  public ToroVideoView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context);
  }

  public ToroVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public ToroVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize(context);
  }

  private void initialize(Context context) {
    setFocusable(true);
    setFocusableInTouchMode(true);
    requestFocus();
    this.mAudioCapabilities = AudioCapabilities.getCapabilities(context);
    setSurfaceTextureListener(surfaceTextureListener);
  }

  @Override public void releasePlayer() {
    if (mMediaPlayer != null) {
      if (lastMomentCallback != null) {
        lastMomentCallback.onLastMoment(mMediaPlayer);
      }

      mPlayerPosition = mMediaPlayer.getCurrentPosition();
      mMediaPlayer.removeListener(mEventLogger);
      mMediaPlayer.removeListener(playerListener);
      mMediaPlayer.release();
      mMediaPlayer = null;
      mEventLogger.endSession();
      mEventLogger = null;
      mExoMediaPlayerHelper = null;
    }
  }

  @Override public void preparePlayer(boolean playWhenReady) {
    if (mMedia == null || mSurface == null) {
      return;
    }

    if (mMediaPlayer == null) {
      mMediaPlayer =
          new ExoMediaPlayer(RendererBuilderFactory.createRendererBuilder(getContext(), mMedia));
      mMediaPlayer.addListener(playerListener);

      mMediaPlayer.setPlayerStateChangeListener(stateChangeListenerDelegate);
      mMediaPlayer.setOnInfoListener(onInfoListenerDelegate);

      mMediaPlayer.setCaptionListener(mExoMediaPlayerHelper);
      mMediaPlayer.setMetadataListener(mExoMediaPlayerHelper);
      /* mMediaPlayer.setScreenOnWhilePlaying(true); */ // this is handle by this View
      mMediaPlayer.seekTo(mPlayerPosition);
      mPlayerNeedsPrepare = true;
      mEventLogger = new EventLogger();
      mEventLogger.startSession();
      if (BuildConfig.DEBUG) {
        mMediaPlayer.addListener(mEventLogger);
        mMediaPlayer.setInfoListener(mEventLogger);
        mMediaPlayer.setInternalErrorListener(mEventLogger);
      }
    }

    if (mPlayerNeedsPrepare) {
      mMediaPlayer.prepare();
      mPlayerNeedsPrepare = false;
    }

    mMediaPlayer.setSurface(mSurface);
    mMediaPlayer.setPlayWhenReady(playWhenReady);
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

  private void maybeNotifyAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
    if (this.mAudioCapabilities == audioCapabilities) {
      return;
    }

    if (mMediaPlayer == null) {
      return;
    }

    boolean backgrounded = mMediaPlayer.getBackgrounded();
    boolean playWhenReady = mMediaPlayer.getPlayWhenReady();
    releasePlayer();
    preparePlayer(playWhenReady);
    mMediaPlayer.setBackgrounded(backgrounded);
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

  @Override public void seekTo(long milliSec) {
    if (mMediaPlayer != null) {
      mMediaPlayer.seekTo(milliSec);
    }
  }

  @Override public void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume) {
    if (mMediaPlayer != null) {
      mMediaPlayer.setVolume(volume);
    }
  }

  @TargetApi(23) private boolean requiresPermission(Uri uri) {
    return Util.SDK_INT >= 23
        && Util.isLocalFileUri(uri)
        && ActivityCompat.checkSelfPermission(getContext(),
        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
  }

  @SuppressWarnings("ConstantConditions") @Override public void setMedia(@NonNull Media media) {
    if (media == null) {
      throw new IllegalArgumentException("MediaSource must not be null");
    }

    if (requiresPermission(media.getMediaUri())) {
      throw new RuntimeException("Permission to read this URI is not granted. "
          + "Consider to request READ_EXTERNAL_STORAGE permission.");
    }

    if (this.mMedia == media) {
      return;
    }

    this.mPlayerPosition = 0;
    this.mMedia = media;
    mPlayRequested = false;
    releasePlayer();
  }

  @Override public void setMedia(Uri uri) {
    setMedia(new Media(uri));
  }

  // IMediaPlayer

  @Override public void start() {
    mPlayRequested = true;
    if (mMediaPlayer == null) {
      preparePlayer(true);
    } else {
      mMediaPlayer.setBackgrounded(false);
      mMediaPlayer.setPlayWhenReady(mPlayRequested);
    }
  }

  @Override public long getDuration() {
    return mMediaPlayer != null ? mMediaPlayer.getDuration() : ExoPlayer.UNKNOWN_TIME;
  }

  @Override public long getCurrentPosition() {
    return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : ExoPlayer.UNKNOWN_TIME;
  }

  @Override public boolean isPlaying() {
    return mMediaPlayer != null && mMediaPlayer.isPlaying();
  }

  @Override public int getBufferPercentage() {
    return mMediaPlayer != null ? mMediaPlayer.getBufferedPercentage() : 0;
  }

  @Override public int getAudioSessionId() {
    return mMediaPlayer != null ? mMediaPlayer.getAudioSessionId() : 0;
  }

  @Override public void setBackgroundAudioEnabled(boolean enabled) {
    mBackgroundAudioEnabled = enabled;
  }

  @Override public int getVideoWidth() {
    return this.videoWidth;
  }

  @Override public int getVideoHeight() {
    return this.videoHeight;
  }

  @Override public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
    this.onVideoSizeChangedListener = listener;
  }

  private abstract class ExoMediaPlayerHelper
      implements ExoMediaPlayer.CaptionListener, ExoMediaPlayer.Id3MetadataListener {
  }
}
