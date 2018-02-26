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
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import im.ene.toro.media.PlaybackInfo;
import java.io.IOException;

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
   * [20180225]
   *
   * Instance of {@link Playable} should be reusable. Retaining instance of Playable across config
   * change must guarantee that all {@link EventListener} are cleaned up on config change.
   */
  static class PlayableImpl implements Playable {

    private final PlaybackInfo playbackInfo = new PlaybackInfo(); // never expose to outside.
    private final EventListeners listeners = new EventListeners();  // original listener.

    protected final Uri mediaUri; // immutable, parcelable
    protected final ExoCreator creator; // required, cached

    protected SimpleExoPlayer player; // on-demand, cached
    protected PlayerView playerView; // on-demand, not always required.
    protected MediaSource mediaSource;  // on-demand

    private boolean listenerApplied = false;

    PlayableImpl(ExoCreator creator, Uri uri) {
      this.creator = creator;
      this.mediaUri = uri;
    }

    @CallSuper @Override public void prepare(boolean prepareSource) {
      if (player == null) player = toro.requestPlayer(creator);

      if (!listenerApplied) {
        player.addListener(listeners);
        player.addVideoListener(listeners);
        player.addTextOutput(listeners);
        player.addMetadataOutput(listeners);
        listenerApplied = true;
      }

      if (playerView != null && playerView.getPlayer() != player) playerView.setPlayer(player);
      boolean haveResumePosition = playbackInfo.getResumeWindow() != C.INDEX_UNSET;
      if (haveResumePosition) {
        player.seekTo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition());
      }

      if (prepareSource) {
        if (mediaSource == null) {  // Only actually prepare the source when play() is called.
          mediaSource = creator.createMediaSource(mediaUri);
          player.prepare(mediaSource, playbackInfo.getResumeWindow() == C.INDEX_UNSET, false);
        }
      }
    }

    @CallSuper @Override public void setPlayerView(@Nullable PlayerView playerView) {
      if (this.player == null) throw new IllegalStateException("Player is null, prepare it first.");
      if (this.playerView == playerView) return;
      PlayerView.switchTargetView(this.player, this.playerView, playerView);
      this.playerView = playerView;
    }

    @Override public final PlayerView getPlayerView() {
      return this.playerView;
    }

    @CallSuper @Override public void play() {
      checkNotNull(player, "Playable#play(): Player is null!");
      if (mediaSource == null) {  // Only actually prepare the source when play() is called.
        mediaSource = creator.createMediaSource(mediaUri);
        player.prepare(mediaSource, playbackInfo.getResumeWindow() == C.INDEX_UNSET, false);
      }
      player.setPlayWhenReady(true);
    }

    @CallSuper @Override public void pause() {
      checkNotNull(player, "Playable#pause(): Player is null!").setPlayWhenReady(false);
    }

    @CallSuper @Override public void reset() {
      this.playbackInfo.reset();
      if (player != null) player.stop(true);
      // TODO [20180214] double check this when ExoPlayer 2.7.0 is released.
      this.mediaSource = null; // so it will be re-prepared when play() is called.
    }

    @CallSuper @Override public void release() {
      this.setPlayerView(null);
      if (this.player != null) {
        this.player.stop(true);
        if (listenerApplied) {
          player.removeListener(listeners);
          player.removeVideoListener(listeners);
          player.removeTextOutput(listeners);
          player.removeMetadataOutput(listeners);
          listenerApplied = false;
        }
        toro.releasePlayer(this.creator, this.player);
      }
      this.player = null;
      this.mediaSource = null;
    }

    @CallSuper @NonNull @Override public PlaybackInfo getPlaybackInfo() {
      updatePlaybackInfo();
      return new PlaybackInfo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition());
    }

    @CallSuper @Override public void setPlaybackInfo(@NonNull PlaybackInfo playbackInfo) {
      this.playbackInfo.setResumeWindow(playbackInfo.getResumeWindow());
      this.playbackInfo.setResumePosition(playbackInfo.getResumePosition());

      if (player != null) {
        boolean haveResumePosition = this.playbackInfo.getResumeWindow() != INDEX_UNSET;
        if (haveResumePosition) {
          player.seekTo(this.playbackInfo.getResumeWindow(), this.playbackInfo.getResumePosition());
        }
      }
    }

    @Override public final void addEventListener(@NonNull EventListener listener) {
      //noinspection ConstantConditions
      if (listener != null) this.listeners.add(listener);
    }

    @Override public final void removeEventListener(EventListener listener) {
      this.listeners.remove(listener);
    }

    @CallSuper @Override public void setVolume(float volume) {
      checkNotNull(player, "Playable#setVolume(): Player is null!").setVolume(volume);
    }

    @CallSuper @Override public float getVolume() {
      return checkNotNull(player, "Playable#getVolume(): Player is null!").getVolume();
    }

    @Override public boolean isPlaying() {
      return player != null && player.getPlayWhenReady();
    }

    final void updatePlaybackInfo() {
      if (player == null || player.getPlaybackState() == 1) return;
      playbackInfo.setResumeWindow(player.getCurrentWindowIndex());
      playbackInfo.setResumePosition(player.isCurrentWindowSeekable() ? //
          Math.max(0, player.getCurrentPosition()) : TIME_UNSET);
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
