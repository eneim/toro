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

package toro.exoplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.DefaultRenderersFactory.ExtensionRendererMode;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import im.ene.toro.media.PlaybackInfo;

import static toro.exoplayer.ToroExo.toro;

/**
 * @author eneim (2018/01/23).
 */

public final class PlayerHub {

  @NonNull final Config config;
  @NonNull private final DataSource.Factory manifestDataSourceFactory;

  PlayerHub(@NonNull Context context, @NonNull Config config) {
    this.config = config;
    this.manifestDataSourceFactory = new DefaultDataSourceFactory(context, toro.appName);
  }

  SimpleExoPlayer requestPlayer() {
    SimpleExoPlayer player = toro.getPool(this).acquire();
    if (player == null) {
      TrackSelector trackSelector = new DefaultTrackSelector(config.meter);
      RenderersFactory renderersFactory =
          new DefaultRenderersFactory(toro.context, config.drmSessionManager, config.extensionMode);
      player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
    }

    return player;
  }

  @SuppressWarnings("SameParameterValue") //
  Playback.Helper createHelper(@NonNull SimpleExoPlayerView playerView, @NonNull Uri uri,
      @NonNull MediaSourceBuilder builder, @Nullable Handler handler,
      @Nullable MediaSourceEventListener listener, @Nullable Player.EventListener eventListener) {
    MediaSource mediaSource = builder.buildMediaSource(playerView.getContext(), uri, handler, //
        manifestDataSourceFactory, toro.getDataSourceFactory(this), listener);
    return new HelperImpl(this, playerView, mediaSource, eventListener);
  }

  static class HelperImpl implements Playback.Helper {

    final PlayerHub playerHub;
    final MediaSource mediaSource;
    final SimpleExoPlayerView playerView;
    final Player.EventListener listener;

    private final PlaybackInfo playbackInfo = new PlaybackInfo();

    HelperImpl(PlayerHub playerHub, SimpleExoPlayerView playerView, MediaSource mediaSource,
        Player.EventListener listener) {
      this.mediaSource = mediaSource;
      this.playerView = playerView;
      this.listener = listener;
      this.playerHub = playerHub;
    }

    SimpleExoPlayer player;
    ListenerWrapper listenerWrapper;

    @Override public void prepare() {
      if (player == null) player = playerHub.requestPlayer();
      if (listenerWrapper == null) {
        listenerWrapper = new ListenerWrapper(listener);
        player.addListener(listenerWrapper);
      }

      if (playerView.getPlayer() != this.player) playerView.setPlayer(this.player);
      boolean haveResumePosition = playbackInfo.getResumeWindow() != C.INDEX_UNSET;
      if (haveResumePosition) {
        player.seekTo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition());
      }
      player.prepare(mediaSource, !haveResumePosition, false);
    }

    @Override public void play() {
      player.setPlayWhenReady(true);
    }

    @Override public void pause() {
      player.setPlayWhenReady(false);
    }

    // Should not be called before prepare() or after release()
    @Override public boolean isPlaying() {
      return player.getPlayWhenReady();
    }

    @Override public void setVolume(float volume) {
      player.setVolume(volume);
    }

    @Override public float getVolume() {
      return player.getVolume();
    }

    @Override public long getDuration() {
      return player.getDuration();
    }

    @Override public void reset() {
      playbackInfo.reset();
    }

    @NonNull @Override public PlaybackInfo getPlaybackInfo() {
      updateResumePosition();
      return this.playbackInfo;
    }

    @Override public void setPlaybackInfo(@NonNull PlaybackInfo playbackInfo) {
      this.playbackInfo.setResumeWindow(playbackInfo.getResumeWindow());
      this.playbackInfo.setResumePosition(playbackInfo.getResumePosition());

      if (player != null) {
        boolean haveResumePosition = this.playbackInfo.getResumeWindow() != C.INDEX_UNSET;
        if (haveResumePosition) {
          player.seekTo(this.playbackInfo.getResumeWindow(), this.playbackInfo.getResumePosition());
        }
      }
    }

    @Override public void release() {
      this.playerView.setPlayer(null);
      if (this.player != null) {
        if (listenerWrapper != null) {
          player.removeListener(listenerWrapper);
          listenerWrapper = null;
        }
        toro.getPool(playerHub).release(this.player);
      }

      this.player = null;
    }

    void updateResumePosition() {
      if (player == null || player.getPlaybackState() == 1) return;
      playbackInfo.setResumeWindow(player.getCurrentWindowIndex());
      playbackInfo.setResumePosition(
          player.isCurrentWindowSeekable() ? Math.max(0, player.getCurrentPosition()) : C.TIME_UNSET);
    }
  }

  static class ListenerWrapper implements Player.EventListener {

    final Player.EventListener delegate;

    ListenerWrapper(Player.EventListener delegate) {
      this.delegate = delegate;
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
  }

  public static class Builder {

    final Context context;
    final Config config = new Config(); // default config

    /* package */ Builder(Context context) {
      this.context = context;
    }

    public Builder meter(@NonNull BaseMeter meter) {
      this.config.meter = meter;
      return this;
    }

    public Builder drmSessionManager(DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
      this.config.drmSessionManager = drmSessionManager;
      return this;
    }

    public Builder extensionMode(@ExtensionRendererMode int extensionMode) {
      this.config.extensionMode = extensionMode;
      return this;
    }

    public Builder cache(Cache cache) {
      this.config.cache = cache;
      return this;
    }

    public PlayerHub build() {
      PlayerHub playerHub = toro.configToHub.get(config);
      if (playerHub == null) {
        playerHub = new PlayerHub(context, config);
        toro.configToHub.put(config, playerHub);
      }

      return playerHub;
    }
  }
}
