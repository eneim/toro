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
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import im.ene.toro.media.PlaybackInfo;
import java.io.IOException;
import java.util.List;

import static im.ene.toro.exoplayer.ToroExo.toro;
import static im.ene.toro.media.PlaybackInfo.INDEX_UNSET;
import static im.ene.toro.media.PlaybackInfo.TIME_UNSET;

/**
 * @author eneim (2018/02/04).
 *
 *         Usage: use this as-it or inheritance.
 */

@SuppressWarnings({ "unused", "WeakerAccess" }) //
final class DefaultExoCreator implements ExoCreator, MediaSourceEventListener {

  private final Context context;  // per application
  private final TrackSelector trackSelector;  // 'maybe' stateless
  private final LoadControl loadControl;  // stateless
  private final MediaSourceBuilder mediaSourceBuilder;  // stateless
  private final RenderersFactory renderersFactory;  // stateless
  private final DataSource.Factory mediaDataSourceFactory;  // stateless
  private final DataSource.Factory manifestDataSourceFactory; // stateless

  @SuppressWarnings("unchecked")  //
  public DefaultExoCreator(Context context, Config config) {
    this.context = context.getApplicationContext();
    trackSelector = new DefaultTrackSelector(config.meter);
    loadControl = config.loadControl;
    mediaSourceBuilder = config.mediaSourceBuilder;
    renderersFactory = new DefaultRenderersFactory(this.context,  //
        null /* config.drmSessionManager */, config.extensionMode);
    DataSource.Factory factory =
        new DefaultDataSourceFactory(this.context, toro.appName, config.meter);
    if (config.cache != null) {
      factory = new CacheDataSourceFactory(config.cache, factory);
    }
    mediaDataSourceFactory = factory;
    manifestDataSourceFactory = new DefaultDataSourceFactory(this.context, toro.appName);
  }

  public DefaultExoCreator(Context context) {
    this(context, toro.defaultConfig);
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
   * I'm trying to reuse this thing. Not only to save resource, improve
   * performance, but also to have a way to keep the playback smooth.
   */
  static class PlayableImpl implements Playable {

    private final PlaybackInfo playbackInfo = new PlaybackInfo();
    private final EventListeners listeners = new EventListeners();

    private final Uri mediaUri; // immutable
    private final ExoCreator creator; // cached

    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private ListenerWrapper listenerWrapper;
    private MediaSource mediaSource;

    PlayableImpl(ExoCreator creator, Uri uri) {
      this.creator = creator;
      this.mediaUri = uri;
    }

    @Override public void prepare() {
      if (player == null) {
        player = requestPlayer(creator);
      }

      if (listenerWrapper == null) {
        listenerWrapper = new ListenerWrapper(listeners);
        player.addListener(listenerWrapper);
        player.addVideoListener(listenerWrapper);
        player.addTextOutput(listenerWrapper);
      }

      if (playerView != null && playerView.getPlayer() != player) playerView.setPlayer(player);
      boolean haveResumePosition = playbackInfo.getResumeWindow() != C.INDEX_UNSET;
      if (haveResumePosition) {
        player.seekTo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition());
      }

      if (mediaSource == null) {
        mediaSource = creator.createMediaSource(mediaUri);
        player.prepare(mediaSource, !haveResumePosition, false);
      }
    }

    @Override public void attachView(@NonNull SimpleExoPlayerView playerView) {
      if (this.player == null) throw new IllegalStateException("Player is null, prepare it first.");
      //noinspection ConstantConditions
      if (playerView == null || this.playerView == playerView) return;
      SimpleExoPlayerView.switchTargetView(this.player, this.playerView, playerView);
      this.playerView = playerView;
    }

    @Override public void detachView() {
      if (this.playerView != null) {
        this.playerView.setPlayer(null);
        this.playerView = null;
      }
    }

    @Override public SimpleExoPlayerView getPlayerView() {
      return this.playerView;
    }

    @Override public void play() {
      if (this.player != null) this.player.setPlayWhenReady(true);
    }

    @Override public void pause() {
      if (this.player != null) this.player.setPlayWhenReady(false);
    }

    @Override public void reset() {
      this.playbackInfo.reset();
      if (player != null) {
        boolean haveResumePosition = this.playbackInfo.getResumeWindow() != INDEX_UNSET;
        if (haveResumePosition) {
          player.seekTo(this.playbackInfo.getResumeWindow(), this.playbackInfo.getResumePosition());
        }
        // re-prepare using new MediaSource instance.
        mediaSource = creator.createMediaSource(mediaUri);
        player.prepare(mediaSource, !haveResumePosition, false);
      }
    }

    @Override public void release() {
      if (playerView != null) throw new IllegalStateException("Detach PlayerView first.");
      if (this.player != null) {
        if (listenerWrapper != null) {
          player.removeListener(listenerWrapper);
          player.removeVideoListener(listenerWrapper);
          player.removeTextOutput(listenerWrapper);
          listenerWrapper = null;
        }
        toro.getPool(creator).release(this.player);
      }

      this.player = null;
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

    Playable createCopy() {
      PlayableImpl playable = new PlayableImpl(this.creator, this.mediaUri);
      playable.player = requestPlayer(creator);
      playable.playbackInfo.setResumePosition(this.playbackInfo.getResumePosition());
      playable.playbackInfo.setResumeWindow(this.playbackInfo.getResumeWindow());
      return playable;
    }

    void updatePlaybackInfo() {
      if (player == null || player.getPlaybackState() == 1) return;
      playbackInfo.setResumeWindow(player.getCurrentWindowIndex());
      playbackInfo.setResumePosition(player.isCurrentWindowSeekable() ? //
          Math.max(0, player.getCurrentPosition()) : TIME_UNSET);
    }

    @NonNull static SimpleExoPlayer requestPlayer(ExoCreator creator) {
      //noinspection UnusedAssignment
      SimpleExoPlayer player = toro.getPool(creator).acquire();
      if (player == null) {
        player = creator.createPlayer();
      }

      return player;
    }
  }

  private static class ListenerWrapper implements Playable.EventListener {

    @NonNull final Playable.EventListener delegate;

    ListenerWrapper(@NonNull Playable.EventListener delegate) {
      this.delegate = delegate;
    }

    @Override public void onVideoSizeChanged(int width, int height, int unPppliedRotationDegrees,
        float pixelWidthHeightRatio) {
      delegate.onVideoSizeChanged(width, height, unPppliedRotationDegrees, pixelWidthHeightRatio);
    }

    @Override public void onRenderedFirstFrame() {
      delegate.onRenderedFirstFrame();
    }

    @Override public void onTimelineChanged(Timeline timeline, Object manifest) {
      delegate.onTimelineChanged(timeline, manifest);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
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
      delegate.onPlayerError(error);
    }

    @Override public void onPositionDiscontinuity(int reason) {
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
  }
}
