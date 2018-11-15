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

package toro.v4.exo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.view.ViewGroup;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import im.ene.toro.ToroPlayer;
import im.ene.toro.exoplayer.BuildConfig;
import im.ene.toro.exoplayer.Playable;
import im.ene.toro.exoplayer.ToroExoPlayer;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.VolumeInfo;
import java.io.File;
import toro.v4.Media;
import toro.v4.exo.factory.BandwidthMeterFactory;
import toro.v4.exo.factory.DownloaderProvider;
import toro.v4.exo.factory.DrmSessionManagerProvider;
import toro.v4.exo.factory.ExoPlayerManager;
import toro.v4.exo.factory.MediaSourceFactoryProvider;

import static com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER;

/**
 * @author eneim (2018/09/30).
 * @since 3.7.0.2900
 *
 * A helper class to access to default implementations of {@link DownloaderProvider}, {@link
 * ExoPlayerManager}, {@link MediaSourceFactoryProvider} and other dependencies with ease.
 *
 * Client can now create a {@link ToroPlayerHelper} by calling {@link #requestHelper(ToroPlayer,
 * Media)} or build a custom {@link Playable} and use it to create a {@link ToroPlayerHelper} by
 * calling {@link #requestHelper(ToroPlayer, Playable)}.
 */
@SuppressWarnings({ "unused", "WeakerAccess" }) //
public final class MediaHub {

  private static final String CONTENT_DIR = "toro_content";
  private static final int CACHE_SIZE = 32 * 1024 * 1024; // 32 Megabytes

  @SuppressLint("StaticFieldLeak") //
  private static volatile MediaHub mediaHub;

  @NonNull private final Context context;
  @NonNull private final String userAgent;
  @NonNull private final ExoPlayerManager playerManager;
  @NonNull private final MediaSourceFactoryProvider mediaSourceFactoryProvider;

  // On Demand
  private DataSource.Factory upstreamFactory;
  private AdsMediaSource.MediaSourceFactory adsMediaSourceFactory;

  public static MediaHub get(Context context) {
    if (mediaHub != null && mediaHub.context != context.getApplicationContext()) {
      mediaHub.cleanUp();
      mediaHub = null;
    }

    if (mediaHub == null) {
      synchronized (MediaHub.class) {
        if (mediaHub == null) mediaHub = new MediaHub(context);
      }
    }
    return mediaHub;
  }

  public void cleanUp() {
    this.playerManager.cleanUp();
  }

  /**
   * // Build and Set singleton.
   * MediaHub hub = new MediaHub.Builder().build()
   * MediaHub.setSingleton(hub)
   *
   * // Use
   * MediaHub hub = MediaHub.get(context)
   */
  public static void setSingleton(MediaHub hub) {
    if (mediaHub == hub) return;
    if (mediaHub != null) mediaHub.cleanUp();
    mediaHub = hub;
  }

  public static class Builder {
    final Context context;
    final String userAgent;

    ExoPlayerManager playerManager;
    MediaSourceFactoryProvider mediaSourceFactoryProvider;
    AdsMediaSource.MediaSourceFactory adsMediaSourceFactory;

    public Builder(Context context) {
      this(context, Util.getUserAgent(context, BuildConfig.LIB_NAME));
    }

    public Builder(Context context, String userAgent) {
      this.context = context;
      this.userAgent = userAgent;

      // Common components
      DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
      HttpDataSource.Factory httpDataSource =
          new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter.getTransferListener());

      // ExoPlayerProvider
      DrmSessionManagerProvider drmSessionManagerProvider =
          new DefaultDrmSessionManagerProvider(this.context, httpDataSource);
      BandwidthMeterFactory meterFactory = new DefaultBandwidthMeterFactory();
      playerManager = new DefaultExoPlayerManager( //
          this.context, //
          meterFactory, //
          drmSessionManagerProvider, //
          new DefaultLoadControl(), //
          new DefaultTrackSelector(), //
          new DefaultRenderersFactory(context, EXTENSION_RENDERER_MODE_PREFER) //
      );

      // MediaSourceFactoryProvider
      File tempDir = this.context.getExternalFilesDir(null);
      if (tempDir == null) tempDir = this.context.getFilesDir();
      File contentDir = new File(tempDir, CONTENT_DIR);
      Cache mediaCache = new SimpleCache(contentDir, new LeastRecentlyUsedCacheEvictor(CACHE_SIZE));
      DataSource.Factory upstreamFactory = new DefaultDataSourceFactory(this.context, //
          bandwidthMeter.getTransferListener(), httpDataSource);
      mediaSourceFactoryProvider = new DefaultMediaSourceFactoryProvider( //
          this.context, upstreamFactory, mediaCache);
    }

    public Builder setPlayerManager(ExoPlayerManager playerManager) {
      this.playerManager = playerManager;
      return this;
    }

    public Builder setMediaSourceFactoryProvider(
        MediaSourceFactoryProvider mediaSourceFactoryProvider) {
      this.mediaSourceFactoryProvider = mediaSourceFactoryProvider;
      return this;
    }

    public Builder setAdsMediaSourceFactory(
        AdsMediaSource.MediaSourceFactory adsMediaSourceFactory) {
      this.adsMediaSourceFactory = adsMediaSourceFactory;
      return this;
    }

    public final MediaHub build() {
      return new MediaHub( //
          context, //
          userAgent, //
          playerManager, //
          mediaSourceFactoryProvider, //
          adsMediaSourceFactory //
      );
    }
  }

  MediaHub(@NonNull Context context, @NonNull String userAgent,
      @NonNull ExoPlayerManager playerManager,
      @NonNull MediaSourceFactoryProvider mediaSourceFactoryProvider,
      AdsMediaSource.MediaSourceFactory adsMediaSourceFactory) {
    this.context = context.getApplicationContext();
    this.userAgent = userAgent;
    this.playerManager = playerManager;
    this.mediaSourceFactoryProvider = mediaSourceFactoryProvider;
    this.adsMediaSourceFactory = adsMediaSourceFactory;
  }

  MediaHub(@NonNull Context context, @NonNull String userAgent,
      @NonNull ExoPlayerManager playerManager,
      @NonNull MediaSourceFactoryProvider mediaSourceFactoryProvider) {
    this.context = context.getApplicationContext();
    this.userAgent = userAgent;
    this.playerManager = playerManager;
    this.mediaSourceFactoryProvider = mediaSourceFactoryProvider;
  }

  MediaHub(Context context) {
    this.context = context.getApplicationContext();
    this.userAgent = Util.getUserAgent(this.context, BuildConfig.LIB_NAME);

    // Common components
    DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
    HttpDataSource.Factory httpDataSource =
        new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter.getTransferListener());

    // ExoPlayerProvider
    DrmSessionManagerProvider drmSessionManagerProvider =
        new DefaultDrmSessionManagerProvider(this.context, httpDataSource);
    BandwidthMeterFactory meterFactory = new DefaultBandwidthMeterFactory();
    playerManager = new DefaultExoPlayerManager( //
        this.context, //
        meterFactory, //
        drmSessionManagerProvider, //
        new DefaultLoadControl(), //
        new DefaultTrackSelector(), //
        new DefaultRenderersFactory(context, EXTENSION_RENDERER_MODE_PREFER) //
    );

    // MediaSourceFactoryProvider
    File tempDir = this.context.getExternalFilesDir(null);
    if (tempDir == null) tempDir = this.context.getFilesDir();
    File contentDir = new File(tempDir, CONTENT_DIR);
    Cache mediaCache = new SimpleCache(contentDir, new LeastRecentlyUsedCacheEvictor(CACHE_SIZE));
    upstreamFactory = new DefaultDataSourceFactory(this.context, //
        bandwidthMeter.getTransferListener(), httpDataSource);
    mediaSourceFactoryProvider = new DefaultMediaSourceFactoryProvider( //
        this.context, upstreamFactory, mediaCache);
  }

  @NonNull public ToroPlayerHelper requestHelper(ToroPlayer player, Media media) {
    return this.requestHelper(player, media, true);
  }

  @NonNull public ToroPlayerHelper requestHelper(ToroPlayer player, Media media, boolean lazy) {
    return new PlayerHelper(player, media, playerManager, mediaSourceFactoryProvider, lazy);
  }

  @NonNull public ToroPlayerHelper requestHelper(ToroPlayer player, Playable playable) {
    return this.requestHelper(player, playable, true);
  }

  @NonNull
  public ToroPlayerHelper requestHelper(ToroPlayer player, Playable playable, boolean lazy) {
    return new PlayerHelper(player, playable, lazy);
  }

  public Playable createAdsPlayable(Media media, AdsLoader adsLoader, ViewGroup adsContainer) {
    return new DefaultAdsPlayable(this.context, media, mediaSourceFactoryProvider, //
        playerManager, adsLoader, adsContainer, tryGetAdsMediaSourceFactory());
  }

  public Playable createPlayable(Media media) {
    return new DefaultPlayable(context, media, mediaSourceFactoryProvider, playerManager);
  }

  private DataSource.Factory tryGetUpstreamFactory() {
    if (upstreamFactory == null) {
      upstreamFactory = new DefaultDataSourceFactory(this.context, this.userAgent);
    }
    return upstreamFactory;
  }

  private AdsMediaSource.MediaSourceFactory tryGetAdsMediaSourceFactory() {
    if (adsMediaSourceFactory == null) {
      adsMediaSourceFactory = new DefaultAdsPlayable.AdsMediaSourceFactory(tryGetUpstreamFactory());
    }
    return adsMediaSourceFactory;
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) //
  public static boolean setVolumeInfo(@NonNull SimpleExoPlayer player,
      @NonNull VolumeInfo volumeInfo) {
    if (player instanceof ToroExoPlayer) {
      return ((ToroExoPlayer) player).setVolumeInfo(volumeInfo);
    } else {
      float current = player.getVolume();
      boolean changed = volumeInfo.getVolume() == current || (current == 0 && volumeInfo.isMute());
      if (volumeInfo.isMute()) {
        player.setVolume(0f);
      } else {
        player.setVolume(volumeInfo.getVolume());
      }
      return changed;
    }
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) //
  public static VolumeInfo getVolumeInfo(SimpleExoPlayer player) {
    if (player instanceof ToroExoPlayer) {
      return new VolumeInfo(((ToroExoPlayer) player).getVolumeInfo());
    } else {
      float volume = player.getVolume();
      return new VolumeInfo(volume == 0, volume);
    }
  }
}
