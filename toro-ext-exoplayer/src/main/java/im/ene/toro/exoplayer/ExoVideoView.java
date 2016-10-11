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

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.Uri;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.util.Util;
import im.ene.toro.exoplayer.internal.ExoMediaPlayer;
import im.ene.toro.exoplayer.internal.RendererBuilderFactory;

/**
 * Created by eneim on 10/1/16.
 *
 * A FrameLayout which holds a Surface for Video Stream. Due to the update of Android N, it is
 * recommended to use SurfaceView again, so this Layout will be able to switch that base on the
 * host
 * OS Version.
 */
public class ExoVideoView extends FrameLayout /* implements BaseMediaPlayer */ {

  private static final float MAX_ASPECT_RATIO_DEFORMATION_FRACTION = 0.01f;

  /**
   * Either the width or height is decreased to obtain the desired aspect ratio.
   */
  public static final int RESIZE_MODE_DEFAULT = 0;
  /**
   * The width is fixed and the height is increased or decreased to obtain the desired aspect
   * ratio.
   */
  public static final int RESIZE_MODE_FIXED_WIDTH = 1;
  /**
   * The height is fixed and the width is increased or decreased to obtain the desired aspect
   * ratio.
   */
  public static final int RESIZE_MODE_FIXED_HEIGHT = 2;

  private static final int SURFACE_TYPE_DEFAULT = 0;
  private static final int SURFACE_TYPE_SURFACE_VIEW = 1;
  private static final int SURFACE_TYPE_TEXTURE_VIEW = 2;

  // Default implementation of Player's Listener
  private final class VideoPlayerListener implements ExoMediaPlayer.Listener {

    @Override public void onStateChanged(boolean playWhenReady, int playbackState) {
      mPlaybackState = playbackState;
      setKeepScreenOn(isInPlayableState());

      if (playbackState == ExoMediaPlayer.STATE_ENDED) {
        mPlayRequested = false;
        releasePlayer();
        mPlayerPosition = 0;
      }

      if (playerCallback != null) {
        playerCallback.onPlayerStateChanged(playWhenReady, playbackState);
      }
    }

    @Override public void onError(Exception e) {
      if (playerCallback != null) {
        playerCallback.onPlayerError(e);
      }
    }

    @Override public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees,
        float pixelWidthHeightRatio) {
      setAspectRatio(height == 0 ? 1 : (width * pixelWidthHeightRatio) / height);
    }
  }

  public ExoVideoView(Context context) {
    this(context, null);
  }

  public ExoVideoView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ExoVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    // By default, TextureView is used for Android 23 and below, and SurfaceView is for the rest
    boolean useTextureView = context.getResources().getBoolean(R.bool.use_texture_view);
    if (attrs != null) {
      TypedArray a =
          context.getTheme().obtainStyledAttributes(attrs, R.styleable.ExoVideoView, 0, 0);
      try {
        int surfaceType = a.getInt(R.styleable.ExoVideoView_tx1_surfaceType, SURFACE_TYPE_DEFAULT);
        switch (surfaceType) {
          case SURFACE_TYPE_SURFACE_VIEW:
            useTextureView = false;
            break;
          case SURFACE_TYPE_TEXTURE_VIEW:
            useTextureView = true;
            break;
          case SURFACE_TYPE_DEFAULT:
          default:
            // Unchanged, so don't need to execute the line below
            // useTextureView = context.getResources().getBoolean(R.bool.use_texture_view);
            break;
        }

        resizeMode = a.getInt(R.styleable.ExoVideoView_tx1_resizeMode, RESIZE_MODE_FIXED_WIDTH);
      } finally {
        a.recycle();
      }
    }

    View view = useTextureView ? new TextureView(context) : new SurfaceView(context);
    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    view.setLayoutParams(params);
    surfaceView = view;
    addView(surfaceView, 0);

    playerListener = new VideoPlayerListener();

    SurfaceHelper surfaceHelper = SurfaceHelper.Factory.getInstance(this, surfaceView);
    surfaceHelper.setupForView(surfaceView);

    setFocusable(true);
    setFocusableInTouchMode(true);
    requestFocus();
  }

  /**
   * Set the aspect ratio that this view should satisfy.
   *
   * @param widthHeightRatio The width to height ratio.
   */
  private void setAspectRatio(float widthHeightRatio) {
    if (this.videoAspectRatio != widthHeightRatio) {
      this.videoAspectRatio = widthHeightRatio;
      requestLayout();
    }
  }

  private boolean isInPlayableState() {
    return !mPlayerNeedsPrepare && (mPlaybackState != ExoMediaPlayer.STATE_IDLE) && (mPlaybackState
        != ExoMediaPlayer.STATE_PREPARING) && (mPlaybackState != ExoMediaPlayer.STATE_ENDED);
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    if (videoAspectRatio == 0) {
      // Aspect ratio not set.
      return;
    }

    int width = getMeasuredWidth();
    int height = getMeasuredHeight();
    float viewAspectRatio = (float) width / height;
    float aspectDeformation = videoAspectRatio / viewAspectRatio - 1;
    if (Math.abs(aspectDeformation) <= MAX_ASPECT_RATIO_DEFORMATION_FRACTION) {
      // We're within the allowed tolerance.
      return;
    }

    switch (this.resizeMode) {
      case RESIZE_MODE_FIXED_WIDTH:
        height = (int) (width / videoAspectRatio);
        break;
      case RESIZE_MODE_FIXED_HEIGHT:
        width = (int) (height * videoAspectRatio);
        break;
      case RESIZE_MODE_DEFAULT:
      default:
        if (aspectDeformation > 0) {
          height = (int) (width / videoAspectRatio);
        } else {
          width = (int) (height * videoAspectRatio);
        }
        break;
    }

    super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
  }

  // VideoView Implementation
  Surface mSurface;
  ExoMediaPlayer mMediaPlayer;
  private float videoAspectRatio;
  private final View surfaceView;
  private int resizeMode = RESIZE_MODE_FIXED_WIDTH;
  private final VideoPlayerListener playerListener;
  private int mPlaybackState;
  private Media mMedia;
  long mPlayerPosition;
  boolean mPlayerNeedsPrepare;
  boolean mPlayRequested = false;
  boolean mBackgroundAudioEnabled = false;
  private OnReleaseCallback onReleaseCallback;

  private PlayerCallback playerCallback;
  private AudioCapabilitiesReceiver mAudioCapabilitiesReceiver;
  private AudioCapabilities mAudioCapabilities;

  private AudioCapabilitiesReceiver.Listener audioCapabilitiesListener =
      new AudioCapabilitiesReceiver.Listener() {
        @Override public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
          maybeNotifyAudioCapabilitiesChanged(audioCapabilities);
        }
      };

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

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    this.mAudioCapabilities = AudioCapabilities.getCapabilities(getContext());
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

  public final void releasePlayer() {
    if (mMediaPlayer != null) {
      if (onReleaseCallback != null) {
        onReleaseCallback.onRelease(new SimpleMediaPlayer() {
          @Override public long getDuration() {
            return mMediaPlayer.getDuration();
          }

          @Override public long getCurrentPosition() {
            return mMediaPlayer.getCurrentPosition();
          }
        });
      }

      mPlayerPosition = mMediaPlayer.getCurrentPosition();
      mMediaPlayer.removeListener(playerListener);
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
  }

  public final void preparePlayer(boolean playWhenReady) {
    if (mMedia == null || mSurface == null) {
      return;
    }

    if (mMediaPlayer == null) {
      mMediaPlayer =
          new ExoMediaPlayer(RendererBuilderFactory.createRendererBuilder(getContext(), mMedia));
      mMediaPlayer.addListener(playerListener);
      // TODO Define the need of Caption/Subtitle and MetaData Listener
      // mMediaPlayer.setCaptionListener(mExoMediaPlayerHelper);
      // mMediaPlayer.setMetadataListener(mExoMediaPlayerHelper);

      /* mMediaPlayer.setScreenOnWhilePlaying(true); */ // this is handle by this View
      mMediaPlayer.seekTo(mPlayerPosition);
      mPlayerNeedsPrepare = true;
    }

    if (mPlayerNeedsPrepare) {
      mMediaPlayer.prepare();
      mPlayerNeedsPrepare = false;
    }

    mMediaPlayer.setSurface(mSurface);
    mMediaPlayer.setPlayWhenReady(playWhenReady);
  }

  public void pause() {
    mPlayRequested = false;
    if (mMediaPlayer != null) {
      mMediaPlayer.setPlayWhenReady(false);
    }
  }

  public void stop() {
    mPlayRequested = false;
    releasePlayer();
    mPlayerPosition = 0;
  }

  public void seekTo(long milliSec) {
    if (mMediaPlayer != null) {
      mMediaPlayer.seekTo(milliSec);
    }
  }

  public void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume) {
    if (mMediaPlayer != null) {
      mMediaPlayer.setVolume(volume);
    }
  }

  @SuppressWarnings("ConstantConditions") public void start() {
    mPlayRequested = true;
    if (mMediaPlayer == null) {
      preparePlayer(mPlayRequested);
    } else {
      mMediaPlayer.setBackgrounded(false);
      mMediaPlayer.setPlayWhenReady(mPlayRequested);
    }
  }

  public long getDuration() {
    return mMediaPlayer != null ? mMediaPlayer.getDuration() : ExoPlayer.UNKNOWN_TIME;
  }

  public long getCurrentPosition() {
    return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : ExoPlayer.UNKNOWN_TIME;
  }

  public boolean isPlaying() {
    return mMediaPlayer != null && mMediaPlayer.getPlayWhenReady();
  }

  public int getBufferPercentage() {
    return mMediaPlayer != null ? mMediaPlayer.getBufferedPercentage() : 0;
  }

  public int getAudioSessionId() {
    return mMediaPlayer != null ? mMediaPlayer.getAudioSessionId() : 0;
  }

  public void setBackgroundAudioEnabled(boolean enabled) {
    mBackgroundAudioEnabled = enabled;
  }

  public void setOnReleaseCallback(OnReleaseCallback onReleaseCallback) {
    this.onReleaseCallback = onReleaseCallback;
  }

  public void setPlayerCallback(PlayerCallback stateChangeListener) {
    this.playerCallback = stateChangeListener;
  }

  @TargetApi(23) private boolean requiresPermission(Uri uri) {
    return Util.SDK_INT >= 23
        && Util.isLocalFileUri(uri)
        && ActivityCompat.checkSelfPermission(getContext(),
        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
  }

  @SuppressWarnings("ConstantConditions") public void setMedia(@NonNull Media media) {
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
    this.mPlayRequested = false;
    preparePlayer(false);
  }

  public void setMedia(Uri uri) {
    setMedia(new Media(uri));
  }
}
