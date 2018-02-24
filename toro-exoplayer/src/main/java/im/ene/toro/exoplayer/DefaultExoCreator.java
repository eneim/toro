/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
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

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import im.ene.toro.media.PlaybackInfo;
import java.io.IOException;
import java.util.List;

import static com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS;
import static im.ene.toro.ToroUtil.checkNotNull;
import static im.ene.toro.exoplayer.ToroExo.toro;
import static im.ene.toro.exoplayer.ToroExo.with;
import static im.ene.toro.media.PlaybackInfo.INDEX_UNSET;
import static im.ene.toro.media.PlaybackInfo.TIME_UNSET;

/**
 * Usage: use this as-it or inheritance.
 *
 * @author eneim (2018/02/04).
 * @since 3.4.0
 */

@SuppressWarnings({ "unused", "WeakerAccess" }) //
public class DefaultExoCreator implements ExoCreator, MediaSourceEventListener {

  private final Context context;  // per application
  private final TrackSelector trackSelector;  // 'maybe' stateless
  private final LoadControl loadControl;  // stateless
  private final MediaSourceBuilder mediaSourceBuilder;  // stateless
  private final RenderersFactory renderersFactory;  // stateless
  private final DataSource.Factory mediaDataSourceFactory;  // stateless
  private final DataSource.Factory manifestDataSourceFactory; // stateless

  @SuppressWarnings("unchecked") DefaultExoCreator(Context context, Config config, String appName) {
    this.context = context.getApplicationContext();
    trackSelector = new DefaultTrackSelector(config.meter);
    loadControl = config.loadControl;
    mediaSourceBuilder = config.mediaSourceBuilder;
    renderersFactory = new DefaultRenderersFactory(this.context,  //
        null /* config.drmSessionManager */, config.extensionMode);
    DataSource.Factory factory = new DefaultDataSourceFactory(this.context, appName, config.meter);
    if (config.cache != null) factory = new CacheDataSourceFactory(config.cache, factory);
    mediaDataSourceFactory = factory;
    manifestDataSourceFactory = new DefaultDataSourceFactory(this.context, appName);
  }

  @SuppressWarnings("unchecked")  //
  public DefaultExoCreator(Context context, Config config) {
    this(context, config, with(context).appName);
  }

  public DefaultExoCreator(Context context) {
    this(context, with(context).defaultConfig);
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DefaultExoCreator that = (DefaultExoCreator) o;

    if (!context.equals(that.context)) return false;
    if (!trackSelector.equals(that.trackSelector)) return false;
    if (!loadControl.equals(that.loadControl)) return false;
    if (!mediaSourceBuilder.equals(that.mediaSourceBuilder)) return false;
    if (!renderersFactory.equals(that.renderersFactory)) return false;
    if (!mediaDataSourceFactory.equals(that.mediaDataSourceFactory)) return false;
    return manifestDataSourceFactory.equals(that.manifestDataSourceFactory);
  }

  @Override public int hashCode() {
    int result = context.hashCode();
    result = 31 * result + trackSelector.hashCode();
    result = 31 * result + loadControl.hashCode();
    result = 31 * result + mediaSourceBuilder.hashCode();
    result = 31 * result + renderersFactory.hashCode();
    result = 31 * result + mediaDataSourceFactory.hashCode();
    result = 31 * result + manifestDataSourceFactory.hashCode();
    return result;
  }

  TrackSelector getTrackSelector() {
    return trackSelector;
  }

  @Override public SimpleExoPlayer createPlayer() {
    return ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
  }

  @Override public MediaSource createMediaSource(Uri uri) {
    return mediaSourceBuilder.buildMediaSource(this.context, uri, new Handler(),
        manifestDataSourceFactory, mediaDataSourceFactory, this);
  }

  @Override public Playable createPlayable(Uri uri) {
    return new PlayableImpl(this, uri);
  }

  /// MediaSourceEventListener

  @Override
  public void onLoadStarted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat,
      int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs,
      long mediaEndTimeMs, long elapsedRealtimeMs) {
    // no-ops
  }

  @Override
  public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat,
      int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs,
      long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
    // no-ops
  }

  @Override
  public void onLoadCanceled(DataSpec dataSpec, int dataType, int trackType, Format trackFormat,
      int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs,
      long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
    // no-ops
  }

  @Override
  public void onLoadError(DataSpec dataSpec, int dataType, int trackType, Format trackFormat,
      int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs,
      long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded,
      IOException error, boolean wasCanceled) {
    // no-ops
  }

  @Override
  public void onUpstreamDiscarded(int trackType, long mediaStartTimeMs, long mediaEndTimeMs) {
    // no-ops
  }

  @Override
  public void onDownstreamFormatChanged(int trackType, Format trackFormat, int trackSelectionReason,
      Object trackSelectionData, long mediaTimeMs) {
    // no-ops
  }

  /// Playable implementation

  /**
   * TODO [20180208]
   * I'm trying to reuse this thing. Not only to save resource, improve performance, but also to
   * have a way to keep the playback smooth across config change.
   */
  private static class PlayableImpl implements Playable {

    private final PlaybackInfo playbackInfo = new PlaybackInfo(); // never expose to outside.
    private final EventListeners listeners = new EventListeners();  // original listener.

    final Uri mediaUri; // immutable
    final ExoCreator creator; // cached

    private SimpleExoPlayer player; // on-demand, cached
    private PlayerView playerView; // on-demand, not always required.
    private ListenerWrapper listenerWrapper;  // proxy to wrap original listener.
    private MediaSource mediaSource;  // on-demand

    // Adapt from ExoPlayer demo.
    boolean inErrorState = false;
    public TrackGroupArray lastSeenTrackGroupArray;

    PlayableImpl(ExoCreator creator, Uri uri) {
      this.creator = creator;
      this.mediaUri = uri;
    }

    @Override public void prepare() {
      if (player == null) player = toro.requestPlayer(creator);

      if (listenerWrapper == null) {
        listenerWrapper = new ListenerWrapper(this, listeners);
        player.addListener(listenerWrapper);
        player.addVideoListener(listenerWrapper);
        player.addTextOutput(listenerWrapper);
        player.addMetadataOutput(listenerWrapper);
      }

      if (playerView != null && playerView.getPlayer() != player) playerView.setPlayer(player);
      boolean haveResumePosition = playbackInfo.getResumeWindow() != C.INDEX_UNSET;
      if (haveResumePosition) {
        player.seekTo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition());
      }

      this.lastSeenTrackGroupArray = null;
      this.inErrorState = false;
    }

    @Override public void setPlayerView(@Nullable PlayerView playerView) {
      if (this.player == null) throw new IllegalStateException("Player is null, prepare it first.");
      if (this.playerView == playerView) return;
      PlayerView.switchTargetView(this.player, this.playerView, playerView);
      this.playerView = playerView;
    }

    @Override public PlayerView getPlayerView() {
      return this.playerView;
    }

    @Override public void play() {
      checkNotNull(player, "Playable#play(): Player is null!");
      if (mediaSource == null) {  // Only actually prepare the source when play() is called.
        mediaSource = creator.createMediaSource(mediaUri);
        player.prepare(mediaSource, playbackInfo.getResumeWindow() == C.INDEX_UNSET, false);
      }
      player.setPlayWhenReady(true);
    }

    @Override public void pause() {
      checkNotNull(player, "Playable#pause(): Player is null!").setPlayWhenReady(false);
    }

    @Override public void reset() {
      this.playbackInfo.reset();
      // TODO [20180219] in 2.7.0, there will be #stop() and #stop(boolean) to reset internal state.
      if (player != null) player.stop();
      // TODO [20180214] double check this when ExoPlayer 2.7.0 is released.
      this.mediaSource = null; // so it will be re-prepared when play() is called.
      this.lastSeenTrackGroupArray = null;
      this.inErrorState = false;
    }

    @Override public void release() {
      this.setPlayerView(null);
      if (this.player != null) {
        this.player.stop();
        if (listenerWrapper != null) {
          player.removeListener(listenerWrapper);
          player.removeVideoListener(listenerWrapper);
          player.removeTextOutput(listenerWrapper);
          player.removeMetadataOutput(listenerWrapper);
          listenerWrapper = null;
        }
        toro.releasePlayer(this.creator, this.player);
      }
      this.player = null;
      this.mediaSource = null;
    }

    @NonNull @Override public PlaybackInfo getPlaybackInfo() {
      updatePlaybackInfo();
      return new PlaybackInfo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition());
    }

    @Override public void setPlaybackInfo(@NonNull PlaybackInfo playbackInfo) {
      this.playbackInfo.setResumeWindow(playbackInfo.getResumeWindow());
      this.playbackInfo.setResumePosition(playbackInfo.getResumePosition());

      if (player != null) {
        boolean haveResumePosition = this.playbackInfo.getResumeWindow() != INDEX_UNSET;
        if (haveResumePosition) {
          player.seekTo(this.playbackInfo.getResumeWindow(), this.playbackInfo.getResumePosition());
        }
      }
    }

    @Override public void addEventListener(@NonNull EventListener listener) {
      //noinspection ConstantConditions
      if (listener != null) this.listeners.add(listener);
    }

    @Override public void removeEventListener(EventListener listener) {
      this.listeners.remove(listener);
    }

    @Override public void setVolume(float volume) {
      if (player != null) player.setVolume(volume);
    }

    @Override public float getVolume() {
      return player != null ? player.getVolume() : 1;
    }

    @Override public boolean isPlaying() {
      return player != null && player.getPlayWhenReady();
    }

    void updatePlaybackInfo() {
      if (player == null || player.getPlaybackState() == 1) return;
      playbackInfo.setResumeWindow(player.getCurrentWindowIndex());
      playbackInfo.setResumePosition(player.isCurrentWindowSeekable() ? //
          Math.max(0, player.getCurrentPosition()) : TIME_UNSET);
    }
  }

  private static class ListenerWrapper implements Playable.EventListener {

    @NonNull final PlayableImpl playable;
    @NonNull final Playable.EventListener delegate;

    ListenerWrapper(@NonNull PlayableImpl playable, @NonNull Playable.EventListener delegate) {
      this.delegate = delegate;
      this.playable = playable;
    }

    @Override public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees,
        float pixelWidthHeightRatio) {
      delegate.onVideoSizeChanged(width, height, unAppliedRotationDegrees, pixelWidthHeightRatio);
    }

    @Override public void onRenderedFirstFrame() {
      delegate.onRenderedFirstFrame();
    }

    @Override public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
      delegate.onTimelineChanged(timeline, manifest, reason);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
      TrackSelector selector = ((DefaultExoCreator) playable.creator).getTrackSelector();
      if (selector != null && selector instanceof DefaultTrackSelector) {
        if (trackGroups != playable.lastSeenTrackGroupArray) {
          MappedTrackInfo trackInfo = ((DefaultTrackSelector) selector).getCurrentMappedTrackInfo();
          if (trackInfo != null) {
            if (trackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
                == RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
              // showToast(R.string.error_unsupported_video);
              // TODO
            }

            if (trackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
                == RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
              // showToast(R.string.error_unsupported_audio);
              // TODO
            }
          }

          playable.lastSeenTrackGroupArray = trackGroups;
        }
      }

      delegate.onTracksChanged(trackGroups, trackSelections);
    }

    @Override public void onLoadingChanged(boolean isLoading) {
      delegate.onLoadingChanged(isLoading);
    }

    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      delegate.onPlayerStateChanged(playWhenReady, playbackState);
    }

    @Override public void onRepeatModeChanged(int repeatMode) {
      delegate.onRepeatModeChanged(repeatMode);
    }

    @Override public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
      delegate.onShuffleModeEnabledChanged(shuffleModeEnabled);
    }

    @Override public void onPlayerError(ExoPlaybackException error) {
      /// Adapt from ExoPlayer Demo
      String errorString = null;
      if (error.type == ExoPlaybackException.TYPE_RENDERER) {
        Exception cause = error.getRendererException();
        if (cause instanceof DecoderInitializationException) {
          // Special case for decoder initialization failures.
          DecoderInitializationException decoderInitializationException =
              (DecoderInitializationException) cause;
          if (decoderInitializationException.decoderName == null) {
            if (decoderInitializationException.getCause() instanceof DecoderQueryException) {
              errorString = toro.getString(R.string.error_querying_decoders);
            } else if (decoderInitializationException.secureDecoderRequired) {
              errorString = toro.getString(R.string.error_no_secure_decoder,
                  decoderInitializationException.mimeType);
            } else {
              errorString = toro.getString(R.string.error_no_decoder,
                  decoderInitializationException.mimeType);
            }
          } else {
            errorString = toro.getString(R.string.error_instantiating_decoder,
                decoderInitializationException.decoderName);
          }
        }
      }

      if (errorString != null) {
        // TODO do something with this.
      }

      playable.inErrorState = true;
      if (isBehindLiveWindow(error)) {
        playable.reset();
      } else {
        playable.updatePlaybackInfo();
      }

      delegate.onPlayerError(error);
    }

    @Override public void onPositionDiscontinuity(int reason) {
      if (playable.inErrorState) {
        // Adapt from ExoPlayer demo.
        // This will only occur if the user has performed a seek whilst in the error state. Update
        // the resume position so that if the user then retries, playback will resume from the
        // position to which they seek.
        playable.updatePlaybackInfo();
      }
      delegate.onPositionDiscontinuity(reason);
    }

    @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
      delegate.onPlaybackParametersChanged(playbackParameters);
    }

    @Override public void onSeekProcessed() {
      delegate.onSeekProcessed();
    }

    @Override public void onCues(List<Cue> cues) {
      delegate.onCues(cues);
    }

    @Override public void onMetadata(Metadata metadata) {
      delegate.onMetadata(metadata);
    }
  }

  static boolean isBehindLiveWindow(ExoPlaybackException error) {
    if (error.type != ExoPlaybackException.TYPE_SOURCE) return false;
    Throwable cause = error.getSourceException();
    while (cause != null) {
      if (cause instanceof BehindLiveWindowException) return true;
      cause = cause.getCause();
    }
    return false;
  }
}
