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
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import java.io.IOException;

import static im.ene.toro.ToroUtil.checkNotNull;
import static im.ene.toro.exoplayer.ToroExo.with;

/**
 * Usage: use this as-it or inheritance.
 *
 * @author eneim (2018/02/04).
 * @since 3.4.0
 */

@SuppressWarnings({ "unused", "WeakerAccess" }) //
public class DefaultExoCreator implements ExoCreator, MediaSourceEventListener {

  final ToroExo toro;  // per application
  private final TrackSelector trackSelector;  // 'maybe' stateless
  private final LoadControl loadControl;  // stateless
  private final MediaSourceBuilder mediaSourceBuilder;  // stateless
  private final RenderersFactory renderersFactory;  // stateless
  private final DataSource.Factory mediaDataSourceFactory;  // stateless
  private final DataSource.Factory manifestDataSourceFactory; // stateless

  @SuppressWarnings("unchecked")  //
  public DefaultExoCreator(ToroExo toro, Config config) {
    this.toro = checkNotNull(toro);
    trackSelector = new DefaultTrackSelector(config.meter);
    loadControl = config.loadControl;
    mediaSourceBuilder = config.mediaSourceBuilder;
    renderersFactory = new MultiDrmRendererFactory(this.toro.context, //
        config.drmSessionManagers, config.extensionMode);
    DataSource.Factory baseFactory = config.dataSourceFactory;
    if (baseFactory == null) {
      baseFactory = new DefaultHttpDataSourceFactory(toro.appName, config.meter);
    }
    DataSource.Factory factory = new DefaultDataSourceFactory(this.toro.context,  //
        config.meter, baseFactory);
    if (config.cache != null) factory = new CacheDataSourceFactory(config.cache, factory);
    mediaDataSourceFactory = factory;
    manifestDataSourceFactory = new DefaultDataSourceFactory(this.toro.context, this.toro.appName);
  }

  public DefaultExoCreator(Context context, Config config) {
    this(with(context), config);
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DefaultExoCreator that = (DefaultExoCreator) o;

    if (!toro.equals(that.toro)) return false;
    if (!trackSelector.equals(that.trackSelector)) return false;
    if (!loadControl.equals(that.loadControl)) return false;
    if (!mediaSourceBuilder.equals(that.mediaSourceBuilder)) return false;
    if (!renderersFactory.equals(that.renderersFactory)) return false;
    if (!mediaDataSourceFactory.equals(that.mediaDataSourceFactory)) return false;
    return manifestDataSourceFactory.equals(that.manifestDataSourceFactory);
  }

  @Override public int hashCode() {
    int result = toro.hashCode();
    result = 31 * result + trackSelector.hashCode();
    result = 31 * result + loadControl.hashCode();
    result = 31 * result + mediaSourceBuilder.hashCode();
    result = 31 * result + renderersFactory.hashCode();
    result = 31 * result + mediaDataSourceFactory.hashCode();
    result = 31 * result + manifestDataSourceFactory.hashCode();
    return result;
  }

  final TrackSelector getTrackSelector() {
    return trackSelector;
  }

  @Nullable @Override public Context getContext() {
    return toro.context;
  }

  @NonNull @Override public SimpleExoPlayer createPlayer() {
    // return ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
    return new ToroExoPlayer(renderersFactory, trackSelector, loadControl);
  }

  @NonNull @Override public MediaSource createMediaSource(@NonNull Uri uri, String fileExt) {
    return mediaSourceBuilder.buildMediaSource(this.toro.context, uri, fileExt, new Handler(),
        manifestDataSourceFactory, mediaDataSourceFactory, this);
  }

  @NonNull @Override public Playable createPlayable(@NonNull Uri uri, String fileExt) {
    return new PlayableImpl(this, uri, fileExt);
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
}
