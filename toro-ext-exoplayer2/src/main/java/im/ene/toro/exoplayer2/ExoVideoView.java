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
import android.content.res.TypedArray;
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
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
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
 *
 * @deprecated from 2.2.0 Use {@link ExoPlayerView} instead.
 */
@Deprecated
public class ExoVideoView extends FrameLayout {

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

  private final class ComponentListener
      implements SimpleExoPlayer.VideoListener, ExoPlayer.EventListener, TextRenderer.Output {

    @Override public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees,
        float pixelWidthHeightRatio) {
      setAspectRatio(height == 0 ? 1 : (width * pixelWidthHeightRatio) / height);
    }

    @Override public void onRenderedFirstFrame() {
      shutterView.setVisibility(GONE);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
      if (player == null) {
        return;
      }

      // Video disabled so the shutter must be closed.
      //noinspection ConstantConditions
      if (shutterView != null) {
        shutterView.setVisibility(VISIBLE);
      }
    }

    @Override public void onLoadingChanged(boolean isLoading) {

    }

    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      if (playerCallback != null) {
        playerCallback.onPlayerStateChanged(playWhenReady, playbackState);
      }
    }

    @Override public void onTimelineChanged(Timeline timeline, Object manifest) {
      isTimelineStatic = timeline != null && timeline.getWindowCount() > 0 && !timeline.getWindow(
          timeline.getWindowCount() - 1, window).isDynamic;
    }

    @Override public void onPlayerError(ExoPlaybackException error) {
      if (playerCallback != null) {
        playerCallback.onPlayerError(error);
      }
    }

    @Override public void onPositionDiscontinuity() {

    }

    @Override public void onCues(List<Cue> cues) {
      if (subtitleListener != null) {
        subtitleListener.onCues(cues);
      }
    }

    @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }
  }

  private static final int SURFACE_TYPE_DEFAULT = 0;
  private static final int SURFACE_TYPE_SURFACE_VIEW = 1;
  private static final int SURFACE_TYPE_TEXTURE_VIEW = 2;

  TextRenderer.Output subtitleListener;

  public void setSubtitleListener(TextRenderer.Output subtitleListener) {
    this.subtitleListener = subtitleListener;
  }

  private final View surfaceView;
  final View shutterView;
  private float videoAspectRatio;
  private int resizeMode = RESIZE_MODE_FIXED_WIDTH;
  private final ComponentListener componentListener;
  PlayerCallback playerCallback;

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
        int surfaceType = a.getInt(R.styleable.ExoVideoView_tx2_surfaceType, SURFACE_TYPE_DEFAULT);
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

        resizeMode = a.getInt(R.styleable.ExoVideoView_tx2_resizeMode, RESIZE_MODE_FIXED_WIDTH);
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

    shutterView = new View(context);
    ViewGroup.LayoutParams shutterViewParams =
        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
    shutterView.setLayoutParams(shutterViewParams);
    shutterView.setBackgroundColor(Color.BLACK);
    addView(shutterView);

    window = new Timeline.Window();

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
  void setAspectRatio(float widthHeightRatio) {
    if (this.videoAspectRatio != widthHeightRatio) {
      this.videoAspectRatio = widthHeightRatio;
      requestLayout();
    }
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    releasePlayer();
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

  SimpleExoPlayer player;

  public void setMedia(Uri uri) {
    if (uri == null) {
      return;
    }

    setMedia(new Media(uri));
  }

  public void setMedia(Media media) {
    if (media.equals(this.media)) {
      return;
    }

    this.media = media;

    releasePlayer();
    this.isTimelineStatic = false;
    preparePlayer(false);
  }

  /**
   * Set the {@link SimpleExoPlayer} to use. The {@link SimpleExoPlayer#setTextOutput} and
   * {@link SimpleExoPlayer#setVideoListener} method of the player will be called and
   * previous
   * assignments are overridden.
   *
   * @param player The {@link SimpleExoPlayer} to use.
   */
  protected void setPlayer(SimpleExoPlayer player) {
    if (this.player != null) {
      this.player.setTextOutput(null);
      this.player.setVideoListener(null);
      this.player.removeListener(componentListener);
      this.player.setVideoSurface(null);
    }

    this.player = player;
    if (this.player != null) {
      if (surfaceView instanceof TextureView) {
        this.player.setVideoTextureView((TextureView) surfaceView);
      } else if (surfaceView instanceof SurfaceView) {
        this.player.setVideoSurfaceView((SurfaceView) surfaceView);
      }
      this.player.setVideoListener(componentListener);
      this.player.addListener(componentListener);
      this.player.setTextOutput(componentListener);
    } else {
      shutterView.setVisibility(VISIBLE);
    }
  }

  public SimpleExoPlayer getPlayer() {
    return this.player;
  }

  public final void preparePlayer(boolean shouldAutoPlay) {
    this.shouldAutoPlay = shouldAutoPlay;
    this.playerNeedsSource = player == null || player.getPlaybackState() == ExoPlayer.STATE_IDLE;
    if (player == null) {
      // Setup DRM Resources
      DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
      try {
        UUID drmSchemeUuid =
            this.media instanceof DrmMedia ? getDrmUuid(((DrmMedia) this.media).getType()) : null;
        if (drmSchemeUuid != null) {
          String drmLicenseUrl = ((DrmMedia) this.media).getLicenseUrl();
          String[] keyRequestPropertiesArray =
              ((DrmMedia) this.media).getKeyRequestPropertiesArray();
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
          new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
      trackSelector = new DefaultTrackSelector(/* mainHandler, */ videoTrackSelectionFactory);
      player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector,  //
          new DefaultLoadControl(), drmSessionManager /*, false */);
      setPlayer(player);
      if (isTimelineStatic) {
        // playerWindow is not null here
        if (playerPosition == C.TIME_UNSET) {
          player.seekToDefaultPosition(playerWindow);
        } else {
          player.seekTo(playerWindow, playerPosition);
        }
      }
    }

    if (playerNeedsSource) {
      if (requiresPermission(media.getMediaUri())) {
        // The player will be reinitialized if the permission is granted.
        // TODO reinitialize the Player from higher layer.
        return;
      }

      MediaSource mediaSource = buildMediaSource(media.getMediaUri(), null);
      // player.prepare(mediaSource, !isTimelineStatic);
      player.prepare(mediaSource, !isTimelineStatic, !isTimelineStatic);
      playerNeedsSource = false;
    }

    player.setPlayWhenReady(this.shouldAutoPlay);
  }

  public final void releasePlayer() {
    if (player != null) {
      shouldAutoPlay = player.getPlayWhenReady();
      playerWindow = player.getCurrentWindowIndex();
      playerPosition = C.TIME_UNSET;
      Timeline timeline = player.getCurrentTimeline();
      if (timeline != null && timeline.getWindowCount() > playerWindow && //
          timeline.getWindow(playerWindow, window).isSeekable) {
        playerPosition = player.getCurrentPosition();
      }
      player.setTextOutput(null);
      player.setVideoListener(null);
      player.removeListener(componentListener);
      player.setVideoSurface(null);
      player.release();
      player = null;
      trackSelector = null;
    }

    shutterView.setVisibility(VISIBLE);
  }

  private DataSource.Factory mediaDataSourceFactory;
  private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
  private static final CookieManager DEFAULT_COOKIE_MANAGER;

  static {
    DEFAULT_COOKIE_MANAGER = new CookieManager();
    DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
  }

  Timeline.Window window;
  private Handler mainHandler;
  private MappingTrackSelector trackSelector;
  private boolean playerNeedsSource = true;
  boolean isTimelineStatic;
  private boolean shouldAutoPlay;
  private int playerWindow;
  private long playerPosition;

  private Media media;

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
    return new DefaultDrmSessionManager<>(uuid, FrameworkMediaDrm.newInstance(uuid), drmCallback,
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
    return player != null ? player.getDuration() : C.TIME_UNSET;
  }

  public long getCurrentPosition() {
    return player != null ? player.getCurrentPosition() : C.TIME_UNSET;
  }

  public boolean isPlaying() {
    return player != null && player.getPlayWhenReady();
  }

  public int getBufferPercentage() {
    return player != null ? player.getBufferedPercentage() : 0;
  }

  public int getAudioSessionId() {
    return player != null ? player.getAudioSessionId() : 0;
  }

  public void start() {
    if (this.player != null) {
      this.player.setPlayWhenReady(true);
    }
  }

  public void pause() {
    if (this.player != null) {
      this.player.setPlayWhenReady(false);
    }
  }

  public void stop() {
    if (this.player != null) {
      this.player.stop();
      this.player.setPlayWhenReady(false);
      this.shouldAutoPlay = false;
      this.isTimelineStatic = false;
    }
  }

  public void seekTo(long milliSec) {
    if (player != null) {
      player.seekTo(milliSec);
    }
  }

  public void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume) {
    if (player != null) {
      player.setVolume(volume);
    }
  }

  public int getResizeMode() {
    return resizeMode;
  }

  public void setResizeMode(int resizeMode) {
    if (this.resizeMode != resizeMode) {
      this.resizeMode = resizeMode;
      requestLayout();
    }
  }

  public void setPlayerCallback(PlayerCallback stateChangeListener) {
    this.playerCallback = stateChangeListener;
  }
}