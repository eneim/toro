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

package im.ene.toro.exoplayer2;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.FloatRange;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.StreamingDrmSessionManager;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by eneim on 10/2/16.
 */

public class ExoVideoView extends FrameLayout {

  private static final float MAX_ASPECT_RATIO_DEFORMATION_FRACTION = 0.01f;

  private final class ComponentListener
      implements SimpleExoPlayer.VideoListener, ExoPlayer.EventListener, TextRenderer.Output {

    @Override public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees,
        float pixelWidthHeightRatio) {
      setAspectRatio(height == 0 ? 1 : (width * pixelWidthHeightRatio) / height);
    }

    @Override public void onRenderedFirstFrame() {
      shutterView.setVisibility(GONE);
    }

    @Override public void onVideoTracksDisabled() {
      shutterView.setVisibility(VISIBLE);
    }

    @Override public void onLoadingChanged(boolean isLoading) {

    }

    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      if (onStateChangeListener != null) {
        onStateChangeListener.onPlayerStateChanged(playWhenReady, playbackState);
      }
    }

    @Override public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override public void onPlayerError(ExoPlaybackException error) {

    }

    @Override public void onPositionDiscontinuity() {

    }

    @Override public void onCues(List<Cue> cues) {
      if (subtitleListener != null) {
        subtitleListener.onCues(cues);
      }
    }
  }

  private TextRenderer.Output subtitleListener;

  public void setSubtitleListener(TextRenderer.Output subtitleListener) {
    this.subtitleListener = subtitleListener;
  }

  private float videoAspectRatio;
  private final View surfaceView;
  private final View shutterView;
  private final ComponentListener componentListener;
  private OnStateChangeListener onStateChangeListener;

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
    View view = useTextureView ? new TextureView(context) : new SurfaceView(context);
    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    view.setLayoutParams(params);
    surfaceView = view;
    addView(surfaceView, 0);

    shutterView = new View(context);
    ViewGroup.LayoutParams shutterViewParams =
        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
    shutterView.setLayoutParams(shutterViewParams);
    shutterView.setBackgroundColor(Color.BLACK);
    addView(shutterView);

    componentListener = new ComponentListener();

    mediaDataSourceFactory = buildDataSourceFactory(true);
    mainHandler = new Handler();

    if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
      CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
    }

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

    if (aspectDeformation > 0) {
      height = (int) (width / videoAspectRatio);
    } else {
      width = (int) (height * videoAspectRatio);
    }
    super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
  }

  SimpleExoPlayer mMediaPlayer;

  public void setMedia(Uri uri) {
    if (uri == null || (this.mMedia != null && uri.equals(this.mMedia.getMediaUri()))) {
      return;
    }

    setMedia(new Media(uri));
  }

  public void setMedia(Media media) {
    if (this.mMedia == media) {
      return;
    }

    this.mMedia = media;

    releasePlayer();
    initializePlayer();
  }

  /**
   * Set the {@link SimpleExoPlayer} to use. The {@link SimpleExoPlayer#setTextOutput} and
   * {@link SimpleExoPlayer#setVideoListener} method of the mMediaPlayer will be called and
   * previous
   * assignments are overridden.
   *
   * @param player The {@link SimpleExoPlayer} to use.
   */
  /* package */ void setPlayer(SimpleExoPlayer player) {
    if (this.mMediaPlayer != null) {
      this.mMediaPlayer.setTextOutput(null);
      this.mMediaPlayer.setVideoListener(null);
      this.mMediaPlayer.removeListener(componentListener);
      this.mMediaPlayer.setVideoSurface(null);
    }

    this.mMediaPlayer = player;
    if (player != null) {
      if (surfaceView instanceof TextureView) {
        player.setVideoTextureView((TextureView) surfaceView);
      } else if (surfaceView instanceof SurfaceView) {
        player.setVideoSurfaceView((SurfaceView) surfaceView);
      }
      player.setVideoListener(componentListener);
      player.addListener(componentListener);
      player.setTextOutput(componentListener);
    } else {
      shutterView.setVisibility(VISIBLE);
    }
  }

  public final void initializePlayer() {
    if (mMediaPlayer == null) {
      DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
      try {
        UUID drmSchemeUuid =
            this.mMedia instanceof DrmVideo ? getDrmUuid(((DrmVideo) this.mMedia).getType()) : null;
        if (drmSchemeUuid != null) {
          String drmLicenseUrl = ((DrmVideo) this.mMedia).getLicenseUrl();
          String[] keyRequestPropertiesArray =
              ((DrmVideo) this.mMedia).getKeyRequestPropertiesArray();
          Map<String, String> keyRequestProperties;
          if (keyRequestPropertiesArray == null || keyRequestPropertiesArray.length < 2) {
            keyRequestProperties = null;
          } else {
            keyRequestProperties = new HashMap<>();
            for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
              keyRequestProperties.put(keyRequestPropertiesArray[i],
                  keyRequestPropertiesArray[i + 1]);
            }
          }

          try {
            drmSessionManager =
                buildDrmSessionManager(drmSchemeUuid, drmLicenseUrl, keyRequestProperties);
          } catch (UnsupportedDrmException e) {
            int errorStringId = Util.SDK_INT < 18 ? R.string.error_drm_not_supported
                : (e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
            new AlertDialog.Builder(getContext()).setMessage(errorStringId)
                .setCancelable(true)
                .create()
                .show();
            return;
          }
        }
      } catch (ParserException e) {
        e.printStackTrace();
      }

      TrackSelection.Factory videoTrackSelectionFactory =
          new AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER);
      trackSelector = new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);
      mMediaPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector,  //
          new DefaultLoadControl(), drmSessionManager, false);
      setPlayer(mMediaPlayer);
      if (shouldRestorePosition) {
        // playerWindow is not null here
        if (playerPosition == C.TIME_UNSET) {
          mMediaPlayer.seekToDefaultPosition(playerWindow);
        } else {
          mMediaPlayer.seekTo(playerWindow, playerPosition);
        }
      }

      mMediaPlayer.setPlayWhenReady(shouldAutoPlay);
      playerNeedsSource = true;
    }

    if (playerNeedsSource) {
      if (requiresPermission(mMedia.getMediaUri())) {
        // The mMediaPlayer will be reinitialized if the permission is granted.
        return;
      }

      MediaSource mediaSource = buildMediaSource(mMedia.getMediaUri(), null);
      mMediaPlayer.prepare(mediaSource, !shouldRestorePosition);
      playerNeedsSource = false;
    }
  }

  public final void releasePlayer() {
    if (mMediaPlayer != null) {
      shouldAutoPlay = mMediaPlayer.getPlayWhenReady();
      shouldRestorePosition = false;
      Timeline timeline = mMediaPlayer.getCurrentTimeline();
      if (timeline != null) {
        playerWindow = mMediaPlayer.getCurrentWindowIndex();
        Timeline.Window window = timeline.getWindow(playerWindow, new Timeline.Window());
        if (!window.isDynamic) {
          shouldRestorePosition = true;
          playerPosition = window.isSeekable ? mMediaPlayer.getCurrentPosition() : C.TIME_UNSET;
        }
      }
      mMediaPlayer.release();
      mMediaPlayer = null;
      trackSelector = null;
    }
  }

  private DataSource.Factory mediaDataSourceFactory;
  private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
  private static final CookieManager DEFAULT_COOKIE_MANAGER;

  static {
    DEFAULT_COOKIE_MANAGER = new CookieManager();
    DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
  }

  private Handler mainHandler;
  private MappingTrackSelector trackSelector;
  private boolean playerNeedsSource = true;
  private boolean shouldRestorePosition;
  private boolean shouldAutoPlay;
  private int playerWindow;
  private long playerPosition;

  private Media mMedia;

  private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
    int type = Util.inferContentType(
        !TextUtils.isEmpty(overrideExtension) ? "." + overrideExtension : uri.getLastPathSegment());
    switch (type) {
      case C.TYPE_SS:
        return new SsMediaSource(uri, buildDataSourceFactory(false),
            new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, null /* eventLogger */);
      case C.TYPE_DASH:
        return new DashMediaSource(uri, buildDataSourceFactory(false),
            new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, null /* eventLogger */);
      case C.TYPE_HLS:
        return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, null /* eventLogger */);
      case C.TYPE_OTHER:
        return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
            mainHandler, null /* eventLogger */);
      default: {
        throw new IllegalStateException("Unsupported type: " + type);
      }
    }
  }

  private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(UUID uuid,
      String licenseUrl, Map<String, String> keyRequestProperties) throws UnsupportedDrmException {
    if (Util.SDK_INT < 18) {
      return null;
    }

    HttpMediaDrmCallback drmCallback =
        new HttpMediaDrmCallback(licenseUrl, buildHttpDataSourceFactory(false),
            keyRequestProperties);
    return new StreamingDrmSessionManager<>(uuid, FrameworkMediaDrm.newInstance(uuid), drmCallback,
        null, mainHandler, null /* eventLogger */);
  }

  /**
   * Returns a new DataSource factory.
   *
   * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
   * DataSource factory.
   * @return A new DataSource factory.
   */
  private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
    return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
  }

  /**
   * Returns a new HttpDataSource factory.
   *
   * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
   * DataSource factory.
   * @return A new HttpDataSource factory.
   */
  private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
    return buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
  }

  private DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
    return new DefaultDataSourceFactory(getContext(), bandwidthMeter,
        buildHttpDataSourceFactory(bandwidthMeter));
  }

  private HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
    return new DefaultHttpDataSourceFactory(Util.getUserAgent(getContext(), "Toro"),
        bandwidthMeter);
  }

  private UUID getDrmUuid(String typeString) throws ParserException {
    switch (typeString.toLowerCase()) {
      case "widevine":
        return C.WIDEVINE_UUID;
      case "playready":
        return C.PLAYREADY_UUID;
      default:
        try {
          return UUID.fromString(typeString);
        } catch (RuntimeException e) {
          throw new ParserException("Unsupported drm type: " + typeString);
        }
    }
  }

  @TargetApi(23) private boolean requiresPermission(Uri uri) {
    return Util.SDK_INT >= 23
        && Util.isLocalFileUri(uri)
        && ActivityCompat.checkSelfPermission(getContext(),
        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
  }

  // Player Interface Implementation
  public long getDuration() {
    return mMediaPlayer != null ? mMediaPlayer.getDuration() : C.TIME_UNSET;
  }

  public long getCurrentPosition() {
    return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : C.TIME_UNSET;
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

  public void start() {
    if (this.mMediaPlayer != null) {
      this.mMediaPlayer.setPlayWhenReady(true);
    }
  }

  public void pause() {
    if (this.mMediaPlayer != null) {
      this.mMediaPlayer.setPlayWhenReady(false);
    }
  }

  public void stop() {
    if (this.mMediaPlayer != null) {
      this.mMediaPlayer.setPlayWhenReady(false);
    }
    releasePlayer();
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

  public void setOnStateChangeListener(OnStateChangeListener stateChangeListener) {
    this.onStateChangeListener = stateChangeListener;
  }
}
