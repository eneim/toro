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
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import im.ene.toro.ToroPlayer;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;

import static com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
import static com.google.android.exoplayer2.util.Util.getUserAgent;
import static im.ene.toro.exoplayer.BuildConfig.LIB_NAME;

/**
 * @author eneim (2018/01/05).
 */

public class ExoPlayerViewHelper extends ToroPlayerHelper {

  private EventListener listener = new EventListener() {
    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      ExoPlayerViewHelper.super.onPlayerStateUpdated(playWhenReady, playbackState);
      super.onPlayerStateChanged(playWhenReady, playbackState);
    }
  };

  @NonNull final Uri mediaUri;
  @NonNull final Handler handler;
  @NonNull final SimpleExoPlayerView playerView;

  @NonNull protected final DataSource.Factory manifestDataSourceFactory;
  @NonNull protected final DataSource.Factory mediaDataSourceFactory;

  private Playback playback;

  public ExoPlayerViewHelper(@NonNull Container container, @NonNull ToroPlayer player,
      @NonNull Uri mediaUri) {
    super(container, player);
    if (!(player.getPlayerView() instanceof SimpleExoPlayerView)) {
      throw new IllegalArgumentException("Only SimpleExoPlayerView is required.");
    }
    //noinspection ConstantConditions
    if (mediaUri == null) throw new NullPointerException("Media Uri is null.");
    this.playerView = (SimpleExoPlayerView) player.getPlayerView();
    this.mediaUri = mediaUri;
    this.handler = new Handler();
    Context context = container.getContext().getApplicationContext();
    String appName = getUserAgent(context, LIB_NAME);
    this.manifestDataSourceFactory = new DefaultDataSourceFactory(context, appName);
    this.mediaDataSourceFactory =
        new DefaultDataSourceFactory(context, appName, new DefaultBandwidthMeter());
  }

  public final void setEventListener(Player.EventListener eventListener) {
    this.listener.setDelegate(eventListener);
  }

  protected MediaSource createMediaSource(@NonNull Uri uri, @Nullable Handler handler,
      @Nullable MediaSourceEventListener listener) {
    @C.ContentType int type = Util.inferContentType(uri);
    switch (type) {
      case C.TYPE_DASH:
        return new DashMediaSource.Factory(
            new DefaultDashChunkSource.Factory(mediaDataSourceFactory), manifestDataSourceFactory)//
            .createMediaSource(uri, handler, listener);
      case C.TYPE_HLS:
        return new HlsMediaSource.Factory(mediaDataSourceFactory) //
            .createMediaSource(uri, handler, listener);
      case C.TYPE_OTHER:
        return new ExtractorMediaSource.Factory(mediaDataSourceFactory) //
            .createMediaSource(uri, handler, listener);
      case C.TYPE_SS:
        return new SsMediaSource.Factory( //
            new DefaultSsChunkSource.Factory(mediaDataSourceFactory), manifestDataSourceFactory) //
            .createMediaSource(uri, handler, listener);
      default:
        throw new IllegalStateException("Unsupported type: " + type);
    }
  }

  @Override public void initialize(@Nullable PlaybackInfo playbackInfo) {
    if (this.playback == null) {
      Playback.Resource resource = new Playback.Resource(this.playerView,
          createMediaSource(this.mediaUri, this.handler, null));
      this.playback = ExoPlayerManager.Factory.with(container.getContext())
          .prepare(resource, playbackInfo, null, EXTENSION_RENDERER_MODE_OFF, listener);
    } else {
      this.playback.setPlaybackInfo(playbackInfo);
    }
  }

  // To get the playback instance for any behavior after initialization.
  public final Playback getPlayback() {
    if (this.playback == null) throw new IllegalStateException("Playback has not been initialized");
    return this.playback;
  }

  @Override public void play() {
    getPlayback().play();
  }

  @Override public void pause() {
    getPlayback().pause();
  }

  @Override public boolean isPlaying() {
    return playback != null && playback.isPlaying();
  }

  @NonNull @Override public PlaybackInfo getLatestPlaybackInfo() {
    return getPlayback().getPlaybackInfo();
  }

  @Override public void release() {
    getPlayback().release();
    this.playback = null;
    super.release();
  }

  // Adapter for original EventListener
  public static class EventListener implements Player.EventListener {

    private Player.EventListener delegate;

    public EventListener(Player.EventListener delegate) {
      this.delegate = delegate;
    }

    public EventListener() {
      this.delegate = null;
    }

    public void setDelegate(Player.EventListener delegate) {
      this.delegate = delegate;
    }

    @Override public void onRepeatModeChanged(int repeatMode) {
      if (this.delegate != null) this.delegate.onRepeatModeChanged(repeatMode);
    }

    @Override public void onTimelineChanged(Timeline timeline, Object manifest) {
      if (this.delegate != null) this.delegate.onTimelineChanged(timeline, manifest);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
      if (this.delegate != null) this.delegate.onTracksChanged(trackGroups, trackSelections);
    }

    @Override public void onLoadingChanged(boolean isLoading) {
      if (this.delegate != null) this.delegate.onLoadingChanged(isLoading);
    }

    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      if (this.delegate != null) this.delegate.onPlayerStateChanged(playWhenReady, playbackState);
    }

    @Override public void onPlayerError(ExoPlaybackException error) {
      if (this.delegate != null) this.delegate.onPlayerError(error);
    }

    @Override public void onPositionDiscontinuity(int reason) {
      if (this.delegate != null) this.delegate.onPositionDiscontinuity(reason);
    }

    @Override public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
      if (this.delegate != null) this.delegate.onShuffleModeEnabledChanged(shuffleModeEnabled);
    }

    @Override public void onSeekProcessed() {
      if (this.delegate != null) this.delegate.onSeekProcessed();
    }

    @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
      if (this.delegate != null) this.delegate.onPlaybackParametersChanged(playbackParameters);
    }
  }
}
