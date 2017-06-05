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
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
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
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.UUID;

import static com.google.android.exoplayer2.drm.UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME;

/**
 * @author eneim | 6/5/17.
 */

public class ExoPlayerHelper {

  private static final String TAG = "ToroLib:ExoPlayer";

  @NonNull private final SimpleExoPlayerView playerView;

  @DefaultRenderersFactory.ExtensionRendererMode private final int extensionMode;

  private Handler mainHandler;
  private DefaultTrackSelector trackSelector;
  private boolean needRetrySource;
  private boolean shouldAutoPlay;
  private int resumeWindow;
  private long resumePosition;

  private SimpleExoPlayer player;
  private ComponentListener componentListener;

  @SuppressWarnings("WeakerAccess") //
  public ExoPlayerHelper(@NonNull SimpleExoPlayerView playerView,
      @DefaultRenderersFactory.ExtensionRendererMode int extensionMode) {
    this.playerView = playerView;
    this.extensionMode = extensionMode;
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
      boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
      if (haveResumePosition) {
        player.seekTo(resumeWindow, resumePosition);
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
      player = null;
      trackSelector = null;
    }

    componentListener = null;
    if (mainHandler != null) {
      mainHandler.removeCallbacksAndMessages(null);
      mainHandler = null;
    }
  }

  public void play() {
    if (player != null) player.setPlayWhenReady(true);
  }

  public void pause() {
    if (player != null) player.setPlayWhenReady(false);
  }

  public void setVolume(@FloatRange(from = 0.0, to = 1.0) float volume) {
    if (player != null) player.setVolume(volume);
  }

  private void updateResumePosition() {
    resumeWindow = player.getCurrentWindowIndex();
    resumePosition =
        player.isCurrentWindowSeekable() ? Math.max(0, player.getCurrentPosition()) : C.TIME_UNSET;
  }

  private void clearResumePosition() {
    resumeWindow = C.INDEX_UNSET;
    resumePosition = C.TIME_UNSET;
  }

  private class ComponentListener implements SimpleExoPlayer.EventListener {

    ComponentListener() {
    }

    @Override public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override public void onLoadingChanged(boolean isLoading) {

    }

    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      Log.d(TAG, "onPlayerStateChanged() called with: playWhenReady = ["
          + playWhenReady
          + "], playbackState = ["
          + playbackState
          + "]");
    }

    @Override public void onPlayerError(ExoPlaybackException error) {

    }

    @Override public void onPositionDiscontinuity() {

    }

    @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

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
}
