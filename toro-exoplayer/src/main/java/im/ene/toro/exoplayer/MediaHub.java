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

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.view.ViewGroup;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.ads.AdsMediaSource.MediaSourceFactory;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import im.ene.toro.ToroPlayer;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.VolumeInfo;
import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import im.ene.toro.media.Media;
import im.ene.toro.media.MediaItem;

import static com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
import static com.google.android.exoplayer2.util.Util.getUserAgent;
import static im.ene.toro.ToroUtil.checkNotNull;
import static im.ene.toro.exoplayer.BuildConfig.LIB_NAME;

/**
 * @author eneim (2018/09/30).
 * @since 3.7.0.2900
 *
 * A helper class to access to default implementations of {@link ExoPlayerManager},
 * {@link MediaSourceFactoryProvider} and other dependencies with ease.
 *
 * Client can now create a {@link ToroPlayerHelper} by calling {@link #requestHelper(ToroPlayer,
 * Media)} or build a custom {@link Playable} and use it to create a {@link ToroPlayerHelper} by
 * calling {@link #requestHelper(ToroPlayer, Playable)}.
 */
public final class MediaHub {

  private static final String CONTENT_DIR = "toro_content";
  private static final int CACHE_SIZE = 32 * 1024 * 1024; // 32 Megabytes

  @SuppressLint("StaticFieldLeak") //
  private static volatile MediaHub mediaHub;

  @NonNull private final Context context;
  @NonNull private final String userAgent;
  @NonNull private final ExoPlayerManager playerManager;
  @NonNull private final MediaSourceFactoryProvider mediaSourceFactoryProvider;

  // Create on demand
  private DataSource.Factory upstreamFactory;
  private MediaSourceFactory adsMediaSourceFactory;

  public static MediaHub get(Context context) {
    if (mediaHub != null && mediaHub.context != context.getApplicationContext()) {
      // FIXME I think we should through an exception here, as the app context is not consistent.
      mediaHub.cleanUp();
      mediaHub = null;
    }

    if (mediaHub == null) {
      synchronized (MediaHub.class) {
        if (mediaHub == null) mediaHub = new MediaHub.Builder(context).build();
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
  @SuppressWarnings("unused")
  public static void setSingleton(@NonNull MediaHub hub) {
    if (mediaHub == checkNotNull(hub)) return;
    if (mediaHub != null) mediaHub.cleanUp();
    mediaHub = hub;
  }

  public static final class Builder {
    final Context context;
    final String userAgent;

    ExoPlayerManager playerManager;
    MediaSourceFactoryProvider mediaSourceFactoryProvider;
    MediaSourceFactory adsMediaSourceFactory;

    public Builder(@NonNull Context context) {
      this(checkNotNull(context), getUserAgent(context.getApplicationContext(), LIB_NAME));
    }

    public Builder(@NonNull Context context, @NonNull String userAgent) {
      this.context = checkNotNull(context).getApplicationContext();
      this.userAgent = userAgent;

      // Common components
      DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
      HttpDataSource.Factory httpDataSource =
          new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter.getTransferListener());

      // ExoPlayerManager
      playerManager = new DefaultExoPlayerManager( //
          this.context, //
          new DefaultBandwidthMeterFactory(), //
          new DefaultDrmSessionManagerProvider(this.context, httpDataSource), //
          new DefaultLoadControl(), //
          new DefaultTrackSelector(), //
          new DefaultRenderersFactory(this.context, EXTENSION_RENDERER_MODE_OFF) //
      );

      // MediaSourceFactoryProvider
      File tempDir = this.context.getExternalFilesDir(null);
      if (tempDir == null) tempDir = this.context.getFilesDir();
      File contentDir = new File(tempDir, CONTENT_DIR);
      Cache mediaCache = new SimpleCache(contentDir, new LeastRecentlyUsedCacheEvictor(CACHE_SIZE));
      DataSource.Factory upstreamFactory = new DefaultDataSourceFactory(this.context, //
          bandwidthMeter.getTransferListener(), httpDataSource);
      mediaSourceFactoryProvider =
          new DefaultMediaSourceFactoryProvider(this.context, upstreamFactory, mediaCache);
    }

    @SuppressWarnings("unused")
    public Builder setPlayerManager(@NonNull ExoPlayerManager playerManager) {
      this.playerManager = checkNotNull(playerManager);
      return this;
    }

    @SuppressWarnings("unused")
    public Builder setMediaSourceFactoryProvider(
        @NonNull MediaSourceFactoryProvider factoryProvider) {
      this.mediaSourceFactoryProvider = checkNotNull(factoryProvider);
      return this;
    }

    @SuppressWarnings("unused")
    public Builder setAdsMediaSourceFactory(@Nullable MediaSourceFactory adsMediaSourceFactory) {
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

  @SuppressWarnings("WeakerAccess") MediaHub(
      @NonNull Context context,
      @NonNull String userAgent,
      @NonNull ExoPlayerManager playerManager,
      @NonNull MediaSourceFactoryProvider mediaSourceFactoryProvider,
      @Nullable MediaSourceFactory adsMediaSourceFactory
  ) {
    this.context = context.getApplicationContext();
    this.userAgent = userAgent;
    this.playerManager = playerManager;
    this.mediaSourceFactoryProvider = mediaSourceFactoryProvider;
    this.adsMediaSourceFactory = adsMediaSourceFactory;

    // Adapt from ExoPlayer demo app. Start this on demand.
    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    if (CookieHandler.getDefault() != cookieManager) {
      CookieHandler.setDefault(cookieManager);
    }
  }

  @SuppressWarnings("unused") @NonNull
  public ToroPlayerHelper requestHelper(ToroPlayer player, Uri mediaUri) {
    return this.requestHelper(player, new MediaItem(mediaUri, null));
  }

  @NonNull
  public ToroPlayerHelper requestHelper(ToroPlayer player, Media media) {
    return this.requestHelper(player, media, true);
  }

  @SuppressWarnings("WeakerAccess") @NonNull
  public ToroPlayerHelper requestHelper(ToroPlayer player, Media media, boolean lazyPrepare) {
    Playable playable = this.createPlayable(media);
    return this.requestHelper(player, playable, lazyPrepare);
  }

  @SuppressWarnings("WeakerAccess") @NonNull
  public ToroPlayerHelper requestHelper(ToroPlayer player, Playable playable, boolean lazyPrepare) {
    return new PlayerHelper(player, playable, lazyPrepare);
  }

  @NonNull
  public ToroPlayerHelper requestHelper(ToroPlayer player, Playable playable) {
    return this.requestHelper(player, playable, true);
  }

  @NonNull
  public Playable createAdsPlayable(Media media, AdsLoader adsLoader, ViewGroup adsContainer) {
    return new DefaultAdsPlayable(this.context, media, mediaSourceFactoryProvider, //
        playerManager, adsLoader, adsContainer, tryGetAdsMediaSourceFactory());
  }

  @NonNull public Playable createPlayable(Media media) {
    return new DefaultPlayable(context, media, mediaSourceFactoryProvider, playerManager);
  }

  private DataSource.Factory tryGetUpstreamFactory() {
    if (upstreamFactory == null) {
      upstreamFactory = new DefaultDataSourceFactory(this.context, this.userAgent);
    }
    return upstreamFactory;
  }

  private MediaSourceFactory tryGetAdsMediaSourceFactory() {
    if (adsMediaSourceFactory == null) {
      adsMediaSourceFactory = new DefaultAdsPlayable.AdsMediaSourceFactory(tryGetUpstreamFactory());
    }
    return adsMediaSourceFactory;
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) //
  public static boolean setVolumeInfo(@NonNull Player player,
      @NonNull VolumeInfo volumeInfo) {
    if (player instanceof VolumeInfoController) {
      return ((LazyPlayer) player).setVolumeInfo(volumeInfo);
    } else if (player instanceof SimpleExoPlayer) {
      SimpleExoPlayer simpleExoPlayer = (SimpleExoPlayer) player;
      float current = simpleExoPlayer.getVolume();
      boolean changed = volumeInfo.getVolume() == current || (current == 0 && volumeInfo.isMute());
      if (volumeInfo.isMute()) {
        simpleExoPlayer.setVolume(0f);
      } else {
        simpleExoPlayer.setVolume(volumeInfo.getVolume());
      }
      return changed;
    } else {
      return false;
    }
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) @NonNull
  public static VolumeInfo getVolumeInfo(Player player) {
    if (player instanceof VolumeInfoController) {
      return new VolumeInfo(((VolumeInfoController) player).getVolumeInfo());
    } else if (player instanceof SimpleExoPlayer) {
      float volume = ((SimpleExoPlayer) player).getVolume();
      return new VolumeInfo(volume == 0, volume);
    } else {
      return new VolumeInfo(false, 1.0f);
    }
  }

  public static void addEventListener(Player player, Playable.EventListener listener) {
    player.addListener(listener);

    Player.VideoComponent videoComponent = player.getVideoComponent();
    if (videoComponent != null) videoComponent.addVideoListener(listener);

    Player.AudioComponent audioComponent = player.getAudioComponent();
    if (audioComponent != null) audioComponent.addAudioListener(listener);

    Player.TextComponent textComponent = player.getTextComponent();
    if (textComponent != null) textComponent.addTextOutput(listener);

    if (player instanceof SimpleExoPlayer) {
      ((SimpleExoPlayer) player).addMetadataOutput(listener);
    } else if (player instanceof LazyPlayer) {
      ((LazyPlayer) player).addMetadataOutput(listener);
    }
  }

  public static void removeEventListener(Player player, Playable.EventListener listener) {
    player.removeListener(listener);

    Player.VideoComponent videoComponent = player.getVideoComponent();
    if (videoComponent != null) videoComponent.removeVideoListener(listener);

    Player.AudioComponent audioComponent = player.getAudioComponent();
    if (audioComponent != null) audioComponent.removeAudioListener(listener);

    Player.TextComponent textComponent = player.getTextComponent();
    if (textComponent != null) textComponent.removeTextOutput(listener);

    if (player instanceof SimpleExoPlayer) {
      ((SimpleExoPlayer) player).removeMetadataOutput(listener);
    } else if (player instanceof LazyPlayer) {
      ((LazyPlayer) player).removeMetadataOutput(listener);
    }
  }
}
