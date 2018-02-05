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

import android.net.Uri;
import android.support.annotation.NonNull;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory.ExtensionRendererMode;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.cache.Cache;
import im.ene.toro.media.PlaybackInfo;
import java.util.List;

import static im.ene.toro.exoplayer.ToroExo.toro;

/**
 * @author eneim (2018/01/23).
 */

public final class PlayerHub {

  private static final String TAG = "Toro:PlayerHub";

  @NonNull final ExoCreator creator;

  PlayerHub(@NonNull Config config) {
    this.creator = toro.getCreator(config);
  }

  @NonNull SimpleExoPlayer requestPlayer() {
    //noinspection UnusedAssignment
    SimpleExoPlayer player = toro.getPool(this).acquire();
    if (player == null) {
      player = creator.createPlayer();
    }

    return player;
  }

  @SuppressWarnings("SameParameterValue") //
  public final Playback.Helper createHelper(@NonNull SimpleExoPlayerView playerView,
      @NonNull Uri uri, @NonNull Playback.EventListener eventListener) {
    MediaSource mediaSource = creator.createMediaSource(uri);
    return new HelperImpl(this, playerView, mediaSource, eventListener);
  }

  static class HelperImpl implements Playback.Helper {

    final PlayerHub playerHub;
    final MediaSource mediaSource;
    final SimpleExoPlayerView playerView;
    final Playback.EventListener listener;

    boolean initialized = false;

    private final PlaybackInfo playbackInfo = new PlaybackInfo();

    HelperImpl(PlayerHub playerHub, SimpleExoPlayerView playerView, MediaSource mediaSource,
        Playback.EventListener listener) {
      this.mediaSource = mediaSource;
      this.playerView = playerView;
      this.listener = listener;
      this.playerHub = playerHub;
    }

    SimpleExoPlayer player;
    ListenerWrapper listenerWrapper;

    @Override public void prepare() {
      if (player == null) {
        player = playerHub.requestPlayer();
      }

      if (listenerWrapper == null) {
        listenerWrapper = new ListenerWrapper(listener);
        player.addListener(listenerWrapper);
        player.addVideoListener(listenerWrapper);
      }

      if (playerView.getPlayer() != this.player) playerView.setPlayer(this.player);
      boolean haveResumePosition = playbackInfo.getResumeWindow() != C.INDEX_UNSET;
      if (haveResumePosition) {
        player.seekTo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition());
      }
      player.prepare(mediaSource, !haveResumePosition, false);
      initialized = true;
    }

    @Override public boolean initialized() {
      return initialized;
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
      initialized = false;
      this.playerView.setPlayer(null);
      if (this.player != null) {
        if (listenerWrapper != null) {
          player.removeListener(listenerWrapper);
          player.removeVideoListener(listenerWrapper);
          listenerWrapper = null;
        }
        toro.getPool(playerHub).release(this.player);
      }

      this.player = null;
    }

    @Override public void recycle() {
      // do nothing now
    }

    void updateResumePosition() {
      if (player == null || player.getPlaybackState() == 1) return;
      playbackInfo.setResumeWindow(player.getCurrentWindowIndex());
      playbackInfo.setResumePosition(
          player.isCurrentWindowSeekable() ? Math.max(0, player.getCurrentPosition())
              : C.TIME_UNSET);
    }
  }

  static class ListenerWrapper implements Playback.EventListener {

    final Playback.EventListener delegate;

    ListenerWrapper(Playback.EventListener delegate) {
      this.delegate = delegate;
    }

    @Override public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
        float pixelWidthHeightRatio) {
      delegate.onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio);
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

  @SuppressWarnings("unused") public static class Builder {
    final Config config = new Config(); // default config

    Builder() {
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

    public Builder loadControl(LoadControl loadControl) {
      this.config.loadControl = loadControl;
      return this;
    }

    public Builder mediaSourceBuilder(MediaSourceBuilder builder) {
      this.config.mediaSourceBuilder = builder;
      return this;
    }

    public Builder cache(Cache cache) {
      this.config.cache = cache;
      return this;
    }

    public final PlayerHub build() {
      //noinspection ConstantConditions
      if (config == null) throw new NullPointerException("Config is null");
      return new PlayerHub(config);
    }
  }
}
