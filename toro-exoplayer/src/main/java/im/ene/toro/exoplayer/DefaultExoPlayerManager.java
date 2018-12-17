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
import android.support.annotation.NonNull;
import android.support.v4.util.Pools;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.util.Util;
import java.util.Map;
import java.util.WeakHashMap;
import im.ene.toro.media.Media;

import static com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;

/**
 * @author eneim (2018/10/12).
 */
@SuppressWarnings("WeakerAccess") //
public class DefaultExoPlayerManager implements ExoPlayerManager {

  // Application context.
  private final Context context;

  // Create new BandwidthMeter for every new Player, make sure no 2 Players use the same meter.
  private final BandwidthMeterFactory bandwidthMeterFactory;
  private final LoadControl loadControl;
  private final RenderersFactory renderersFactory;
  private final TrackSelector trackSelector;
  private final DrmSessionManagerProvider drmSessionManagerProvider;

  // Cache...
  private final Pools.Pool<ExoPlayer> plainPlayerPool = new Pools.SimplePool<>(3);
  private final WeakHashMap<ExoPlayer, Long> drmPlayerCache = new WeakHashMap<>();

  public DefaultExoPlayerManager( //
      Context context, //
      BandwidthMeterFactory bandwidthMeterFactory, //
      DrmSessionManagerProvider drmSessionManagerProvider, //
      LoadControl loadControl, //
      TrackSelector trackSelector, //
      RenderersFactory renderersFactory //
  ) {
    this.context = context.getApplicationContext();
    this.bandwidthMeterFactory = bandwidthMeterFactory;
    this.drmSessionManagerProvider = drmSessionManagerProvider;
    this.trackSelector = trackSelector;
    this.loadControl = loadControl;
    this.renderersFactory = renderersFactory;
  }

  public DefaultExoPlayerManager(Context context, BandwidthMeterFactory bandwidthMeterFactory,
      DrmSessionManagerProvider drmSessionManagerProvider) {
    this( //
        context, //
        bandwidthMeterFactory, //
        drmSessionManagerProvider, //
        new DefaultLoadControl(), //
        new DefaultTrackSelector(), //
        new DefaultRenderersFactory(context.getApplicationContext(), EXTENSION_RENDERER_MODE_OFF) //
    );
  }

  public DefaultExoPlayerManager(Context context, BandwidthMeterFactory bandwidthMeterFactory) {
    this(context, bandwidthMeterFactory, null);
  }

  @SuppressWarnings("unused") public DefaultExoPlayerManager(Context context) {
    this(context, new DefaultBandwidthMeterFactory());
  }

  /* package */ TrackSelector getTrackSelector() {
    return trackSelector;
  }

  @NonNull @Override public ExoPlayer acquireExoPlayer(@NonNull Media media) {
    DrmSessionManager<FrameworkMediaCrypto> drmSessionManager =
        drmSessionManagerProvider == null ? null
            : drmSessionManagerProvider.provideDrmSessionManager(media);

    if (drmSessionManager == null) { // No DRM support requires, use Pool to cache plain Player.
      // use Pool for plain player
      ExoPlayer player = plainPlayerPool.acquire();
      if (player == null) {
        player = new LazyPlayer( //
            this.context, //
            this.renderersFactory, //
            this.trackSelector, //
            this.loadControl, //
            this.bandwidthMeterFactory.createBandwidthMeter(), //
            null, //
            Util.getLooper() //
        );
      }
      return player;
    } else {
      // Need DRM support, we'd better use clean/new Player instances.
      ExoPlayer player = new LazyPlayer( //
          this.context, //
          this.renderersFactory, //
          this.trackSelector, //
          this.loadControl, //
          this.bandwidthMeterFactory.createBandwidthMeter(), //
          drmSessionManager, //
          Util.getLooper() //
      );
      drmPlayerCache.put(player, System.currentTimeMillis());
      return player;
    }
  }

  @Override public void releasePlayer(@NonNull Media media, @NonNull ExoPlayer player) {
    player.stop(true);
    if (drmPlayerCache.containsKey(player)) {
      player.release();
      drmPlayerCache.remove(player);
    } else {
      plainPlayerPool.release(player);
    }
  }

  @Override public final void cleanUp() {
    for (Map.Entry<ExoPlayer, Long> entry : drmPlayerCache.entrySet()) {
      ExoPlayer key = entry.getKey();
      if (key != null) key.release();
    }
    drmPlayerCache.clear();
    ExoPlayer player;
    while ((player = plainPlayerPool.acquire()) != null) {
      player.release();
    }
  }
}
