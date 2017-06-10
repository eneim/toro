/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package im.ene.toro;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
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
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import im.ene.toro.media.DrmMedia;
import im.ene.toro.media.PlayerState;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.UUID;

import static com.google.android.exoplayer2.drm.UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME;

/**
 * @author eneim | 6/5/17.
 *
 *         A helper class, dedicated to {@link SimpleExoPlayerView}.
 */

public final class ExoPlayerHelper {

  private static final String TAG = "ToroLib:ExoPlayer";

  @SuppressWarnings("WeakerAccess") //
  @NonNull final SimpleExoPlayerView playerView;
  @SuppressWarnings("WeakerAccess") //
  final PlayerState playerState;  // instance is unchanged, but inner fields are changeable.
  @SuppressWarnings("WeakerAccess") //
  @DefaultRenderersFactory.ExtensionRendererMode  //
  final int extensionMode;

  private SimpleExoPlayer player;
  private Handler mainHandler;
  private ComponentListener componentListener;
  private boolean shouldAutoPlay;

  @SuppressWarnings("WeakerAccess") DefaultTrackSelector trackSelector;
  @SuppressWarnings("WeakerAccess") MediaSource mediaSource;
  @SuppressWarnings("WeakerAccess") boolean needRetrySource;
  @SuppressWarnings("WeakerAccess") EventListener eventListener;

  @SuppressWarnings("WeakerAccess")
  public ExoPlayerHelper(@NonNull SimpleExoPlayerView playerView, int extensionMode,
      PlayerState playerState, boolean shouldAutoPlay) {
    this.playerView = playerView;
    this.extensionMode = extensionMode;
    this.playerState = playerState;
    this.shouldAutoPlay = shouldAutoPlay;
  }

  public ExoPlayerHelper(@NonNull SimpleExoPlayerView playerView, PlayerState playerState) {
    this(playerView, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF, playerState, false);
  }

  @SuppressWarnings("WeakerAccess")
  public ExoPlayerHelper(@NonNull SimpleExoPlayerView playerView, int extensionMode) {
    this(playerView, extensionMode, new PlayerState(), false);
  }

  @SuppressWarnings("unused") //
  public ExoPlayerHelper(@NonNull SimpleExoPlayerView playerView) {
    this(playerView, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
  }

  @SuppressWarnings("unused") //
  public void prepare(Uri media) throws ParserException {
    mainHandler = new Handler();
    Context context = playerView.getContext();
    MediaSource mediaSource =
        buildMediaSource(context, media, buildDataSourceFactory(context, true), mainHandler, null);
    prepare(mediaSource);
  }

  @SuppressWarnings("WeakerAccess") //
  public void prepare(MediaSource mediaSource) throws ParserException {
    if (mediaSource == null) {
      throw new IllegalStateException("Media Source must not be null.");
    }

    this.mediaSource = mediaSource;
    if (this.mainHandler == null) {
      this.mainHandler = new Handler();
    }

    if (componentListener == null) {
      componentListener = new ComponentListener();
    }

    Context context = playerView.getContext();
    this.player = playerView.getPlayer();
    boolean needNewPlayer = player == null;
    if (needNewPlayer) {
      DrmMedia drmMedia = null;
      if (mediaSource instanceof DrmMedia) {
        drmMedia = (DrmMedia) mediaSource;
      }

      UUID drmSchemeUuid = drmMedia != null ? getDrmUuid(drmMedia.getType()) : null;
      DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
      if (drmSchemeUuid != null) {
        String drmLicenseUrl = drmMedia.getLicenseUrl();
        String[] keyRequestPropertiesArray = drmMedia.getKeyRequestPropertiesArray();
        try {
          drmSessionManager = buildDrmSessionManager(context,  //
              drmSchemeUuid, drmLicenseUrl, keyRequestPropertiesArray, mainHandler);
        } catch (UnsupportedDrmException e) {
          int errorStringId = Util.SDK_INT < 18 ? R.string.error_drm_not_supported
              : (e.reason == REASON_UNSUPPORTED_SCHEME ?  //
                  R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
          Toast.makeText(context, errorStringId, Toast.LENGTH_SHORT).show();
          return;
        }
      }

      TrackSelection.Factory adaptiveTrackSelectionFactory =
          new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
      trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);

      DefaultRenderersFactory renderersFactory =
          new DefaultRenderersFactory(context, drmSessionManager, extensionMode);

      player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
      player.addListener(componentListener);

      playerView.setPlayer(player);
      player.setPlayWhenReady(shouldAutoPlay);
      needRetrySource = true;
    }

    if (needNewPlayer || needRetrySource) {
      boolean haveResumePosition = playerState.getResumeWindow() != C.INDEX_UNSET;
      if (haveResumePosition) {
        player.seekTo(playerState.getResumeWindow(), playerState.getResumePosition());
      }
      player.prepare(mediaSource, !haveResumePosition, false);
      needRetrySource = false;
    }
  }

  public void release() {
    if (player != null) {
      shouldAutoPlay = player.getPlayWhenReady();
      updateResumePosition();
      player.removeListener(componentListener);
      player.release();
      playerView.setPlayer(null);
      player = null;
      trackSelector = null;
    }

    mediaSource = null;
    componentListener = null;
    if (mainHandler != null) {
      mainHandler.removeCallbacksAndMessages(null);
      mainHandler = null;
    }
  }

  @NonNull public PlayerState getPlayerState() {
    updateResumePosition();
    return new PlayerState(playerState.getResumeWindow(), playerState.getResumePosition());
  }

  public void setEventListener(EventListener eventListener) {
    this.eventListener = eventListener;
  }

  public void play() {
    if (player != null) player.setPlayWhenReady(true);
  }

  public void pause() {
    if (player != null) player.setPlayWhenReady(false);
  }

  public boolean isPlaying() {
    return this.player != null && this.player.getPlayWhenReady();
  }

  public void setVolume(@FloatRange(from = 0.0, to = 1.0) float volume) {
    if (player != null) player.setVolume(volume);
  }

  public SimpleExoPlayer getPlayer() {
    return player;
  }

  void updateResumePosition() {
    playerState.setResumeWindow(player.getCurrentWindowIndex());
    playerState.setResumePosition(
        player.isCurrentWindowSeekable() ? Math.max(0, player.getCurrentPosition()) : C.TIME_UNSET);
  }

  void clearResumePosition() {
    playerState.reset();
  }

  private class ComponentListener implements ExoPlayer.EventListener {

    ComponentListener() {
    }

    @Override public void onTimelineChanged(Timeline timeline, Object manifest) {
      Log.d(TAG, "onTimelineChanged() called with: timeline = ["
          + timeline
          + "], manifest = ["
          + manifest
          + "]");
      if (eventListener != null) eventListener.onTimelineChanged(timeline, manifest);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
      Log.d(TAG, "onTracksChanged() called with: trackGroups = ["
          + trackGroups
          + "], trackSelections = ["
          + trackSelections
          + "]");
      MappingTrackSelector.MappedTrackInfo mappedTrackInfo =
          trackSelector.getCurrentMappedTrackInfo();
      if (mappedTrackInfo != null) {
        if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
          Toast.makeText(playerView.getContext(), R.string.error_unsupported_video,
              Toast.LENGTH_SHORT).show();
        }
        if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
          Toast.makeText(playerView.getContext(), R.string.error_unsupported_audio,
              Toast.LENGTH_SHORT).show();
        }
      }
      if (eventListener != null) eventListener.onTracksChanged(trackGroups, trackSelections);
    }

    @Override public void onLoadingChanged(boolean isLoading) {
      if (eventListener != null) eventListener.onLoadingChanged(isLoading);
    }

    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      Log.d(TAG, "onPlayerStateChanged() called with: playWhenReady = ["
          + playWhenReady
          + "], playbackState = ["
          + playbackState
          + "]");
      if (eventListener != null) eventListener.onPlayerStateChanged(playWhenReady, playbackState);
    }

    @Override public void onPlayerError(ExoPlaybackException e) {
      String errorString = null;
      Context context = playerView.getContext();
      if (e.type == ExoPlaybackException.TYPE_RENDERER) {
        Exception cause = e.getRendererException();
        if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
          // Special case for decoder initialization failures.
          MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
              (MediaCodecRenderer.DecoderInitializationException) cause;
          if (decoderInitializationException.decoderName == null) {
            if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
              errorString = context.getString(R.string.error_querying_decoders);
            } else if (decoderInitializationException.secureDecoderRequired) {
              errorString = context.getString(R.string.error_no_secure_decoder,
                  decoderInitializationException.mimeType);
            } else {
              errorString = context.getString(R.string.error_no_decoder,
                  decoderInitializationException.mimeType);
            }
          } else {
            errorString = context.getString(R.string.error_instantiating_decoder,
                decoderInitializationException.decoderName);
          }
        }
      }
      if (errorString != null) {
        Toast.makeText(context, errorString, Toast.LENGTH_SHORT).show();
      }

      needRetrySource = true;
      if (isBehindLiveWindow(e)) {
        clearResumePosition();
        try {
          prepare(ExoPlayerHelper.this.mediaSource);
        } catch (ParserException e1) {
          e1.printStackTrace();
        }
      } else {
        updateResumePosition();
      }

      if (eventListener != null) eventListener.onPlayerError(e);
    }

    @Override public void onPositionDiscontinuity() {
      if (needRetrySource) {
        // This will only occur if the user has performed a seek whilst in the error state. Update the
        // resume position so that if the user then retries, playback will resume from the position to
        // which they seek.
        updateResumePosition();
      }

      if (eventListener != null) eventListener.onPositionDiscontinuity();
    }

    @Override public void onPlaybackParametersChanged(PlaybackParameters parameters) {
      Log.d(TAG, "onPlaybackParametersChanged() called with: parameters = [" + parameters + "]");
      if (eventListener != null) eventListener.onPlaybackParametersChanged(parameters);
    }
  }

  //// static methods

  private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
  private static final CookieManager DEFAULT_COOKIE_MANAGER;

  static {
    DEFAULT_COOKIE_MANAGER = new CookieManager();
    DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
  }

  @SuppressWarnings("unused") //
  public static MediaSource buildMediaSource(Context ctx, Uri uri) {
    return buildMediaSource(ctx, uri, buildDataSourceFactory(ctx, true), new Handler(), null);
  }

  @SuppressWarnings("WeakerAccess")
  public static MediaSource buildMediaSource(Context context, Uri uri,
      DataSource.Factory mediaDataSourceFactory, Handler mainHandler, String overrideExtension) {
    int type = Util.inferContentType(
        !TextUtils.isEmpty(overrideExtension) ? "." + overrideExtension : uri.getLastPathSegment());
    switch (type) {
      case C.TYPE_SS:
        return new SsMediaSource(uri, buildDataSourceFactory(context, false),
            new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, null /* eventLogger */);
      case C.TYPE_DASH:
        return new DashMediaSource(uri, buildDataSourceFactory(context, false),
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

  static boolean isBehindLiveWindow(ExoPlaybackException e) {
    if (e.type != ExoPlaybackException.TYPE_SOURCE) {
      return false;
    }
    Throwable cause = e.getSourceException();
    while (cause != null) {
      if (cause instanceof BehindLiveWindowException) {
        return true;
      }
      cause = cause.getCause();
    }
    return false;
  }

  private static DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(Context context,
      UUID uuid, String licenseUrl, String[] keyRequestPropertiesArray, Handler mainHandler)
      throws UnsupportedDrmException {
    if (Util.SDK_INT < 18) {
      return null;
    }
    HttpMediaDrmCallback drmCallback =
        new HttpMediaDrmCallback(licenseUrl, buildHttpDataSourceFactory(context, false));
    if (keyRequestPropertiesArray != null) {
      for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
        drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
            keyRequestPropertiesArray[i + 1]);
      }
    }
    return new DefaultDrmSessionManager<>(uuid, FrameworkMediaDrm.newInstance(uuid), drmCallback,
        null, mainHandler, null);
  }

  @SuppressWarnings("WeakerAccess")
  static HttpDataSource.Factory buildHttpDataSourceFactory(Context context,
      DefaultBandwidthMeter bandwidthMeter) {
    return new DefaultHttpDataSourceFactory(
        Util.getUserAgent(context.getApplicationContext(), "Toro"), bandwidthMeter);
  }

  private static DataSource.Factory buildDataSourceFactory(Context context,
      DefaultBandwidthMeter bandwidthMeter) {
    return new DefaultDataSourceFactory(context, bandwidthMeter,
        buildHttpDataSourceFactory(context, bandwidthMeter));
  }

  @SuppressWarnings("WeakerAccess")
  static HttpDataSource.Factory buildHttpDataSourceFactory(Context context,
      boolean useBandwidthMeter) {
    return buildHttpDataSourceFactory(context, useBandwidthMeter ? BANDWIDTH_METER : null);
  }

  private static DataSource.Factory buildDataSourceFactory(Context context,
      boolean useBandwidthMeter) {
    return buildDataSourceFactory(context, useBandwidthMeter ? BANDWIDTH_METER : null);
  }

  private static UUID getDrmUuid(String typeString) throws ParserException {
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

  // Adapter for original EventListener
  public static class EventListener implements ExoPlayer.EventListener {

    @Override public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override public void onLoadingChanged(boolean isLoading) {

    }

    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override public void onPlayerError(ExoPlaybackException error) {

    }

    @Override public void onPositionDiscontinuity() {

    }

    @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }
  }
}
