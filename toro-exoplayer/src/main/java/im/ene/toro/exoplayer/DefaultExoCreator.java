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
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import java.io.IOException;

import static im.ene.toro.exoplayer.ToroExo.toro;

/**
 * @author eneim (2018/02/04).
 *
 *         Usage: use this as-it or inheritance.
 */

@SuppressWarnings({ "unused", "WeakerAccess" }) //
public class DefaultExoCreator implements ExoCreator, MediaSourceEventListener {

  private final Context context;  // per application
  private final TrackSelector trackSelector;  // 'maybe' stateless
  private final RenderersFactory renderersFactory;  // stateless
  private final LoadControl loadControl;
  private final DataSource.Factory mediaDataSourceFactory;  // stateless
  private final DataSource.Factory manifestDataSourceFactory; // stateless
  private final MediaSourceBuilder mediaSourceBuilder;

  @SuppressWarnings("unchecked")
  public DefaultExoCreator(Context context, Config config) {
    this.context = context.getApplicationContext();
    trackSelector = new DefaultTrackSelector(config.meter);
    renderersFactory =
        new DefaultRenderersFactory(this.context, config.drmSessionManager, config.extensionMode);
    loadControl = config.loadControl;
    mediaDataSourceFactory = new DefaultDataSourceFactory(this.context, toro.appName, config.meter);
    manifestDataSourceFactory = new DefaultDataSourceFactory(this.context, toro.appName);
    mediaSourceBuilder = config.mediaSourceBuilder;
  }

  public DefaultExoCreator(Context context) {
    this(context, new Config());
  }

  @Override public SimpleExoPlayer createPlayer() {
    return ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
  }

  @Override public MediaSource createMediaSource(Uri uri) {
    return mediaSourceBuilder.buildMediaSource(this.context, uri, new Handler(),
        manifestDataSourceFactory, mediaDataSourceFactory, this);
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
