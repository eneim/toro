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
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.StringRes;
import android.support.v4.util.Pools;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import im.ene.toro.media.DrmMedia;
import im.ene.toro.media.VolumeInfo;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static android.widget.Toast.LENGTH_SHORT;
import static com.google.android.exoplayer2.drm.UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME;
import static com.google.android.exoplayer2.util.Util.getDrmUuid;
import static com.google.android.exoplayer2.util.Util.getUserAgent;
import static im.ene.toro.ToroUtil.checkNotNull;
import static im.ene.toro.exoplayer.BuildConfig.LIB_NAME;
import static java.lang.Runtime.getRuntime;

/**
 * Global helper class to manage {@link ExoCreator} and {@link SimpleExoPlayer} instances.
 * In this setup, {@link ExoCreator} and SimpleExoPlayer pools are cached. A {@link Config}
 * is a key for each {@link ExoCreator}.
 *
 * A suggested usage is as below:
 * <pre><code>
 * ExoCreator creator = ToroExo.with(this).getDefaultCreator();
 * Playable playable = creator.createPlayable(uri);
 * playable.prepare();
 * // next: setup PlayerView and start the playback.
 * </code></pre>
 *
 * @author eneim (2018/01/26).
 * @since 3.4.0
 */

public final class ToroExo {

  private static final String TAG = "ToroExo";

  // Magic number: Build.VERSION.SDK_INT / 6 --> API 16 ~ 18 will set pool size to 2, etc.
  @SuppressWarnings("WeakerAccess") //
  static final int MAX_POOL_SIZE = Math.max(Util.SDK_INT / 6, getRuntime().availableProcessors());
  @SuppressLint("StaticFieldLeak")  //
  static volatile ToroExo toro;

  public static ToroExo with(Context context) {
    if (toro == null) {
      synchronized (ToroExo.class) {
        if (toro == null) toro = new ToroExo(context.getApplicationContext());
      }
    }
    return toro;
  }

  @NonNull final String appName;
  @NonNull final Context context;  // Application context
  @NonNull private final Map<Config, ExoCreator> creators;
  @NonNull private final Map<ExoCreator, Pools.Pool<SimpleExoPlayer>> playerPools;

  private Config defaultConfig; // will be created on the first time it is used.

  private ToroExo(@NonNull Context context /* Application context */) {
    this.context = context;
    this.appName = getUserAgent(context, LIB_NAME);
    this.playerPools = new HashMap<>();
    this.creators = new HashMap<>();

    // Adapt from ExoPlayer demo app. Start this on demand.
    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    if (CookieHandler.getDefault() != cookieManager) {
      CookieHandler.setDefault(cookieManager);
    }
  }

  /**
   * Utility method to produce {@link ExoCreator} instance from a {@link Config}.
   */
  public final ExoCreator getCreator(Config config) {
    ExoCreator creator = this.creators.get(config);
    if (creator == null) {
      creator = new DefaultExoCreator(this, config);
      this.creators.put(config, creator);
    }

    return creator;
  }

  public final Config getDefaultConfig() {
    if (defaultConfig == null) defaultConfig = new Config.Builder().build();
    return defaultConfig;
  }

  /**
   * Get the default {@link ExoCreator}. This ExoCreator is configured by {@link #defaultConfig}.
   */
  public final ExoCreator getDefaultCreator() {
    return getCreator(getDefaultConfig());
  }

  /**
   * Request an instance of {@link SimpleExoPlayer}. It can be an existing instance cached by Pool
   * or new one.
   *
   * The creator may or may not be the one created by either {@link #getCreator(Config)} or
   * {@link #getDefaultCreator()}.
   *
   * @param creator the {@link ExoCreator} that is scoped to the {@link SimpleExoPlayer} config.
   * @return an usable {@link SimpleExoPlayer} instance.
   */
  @NonNull  //
  public final SimpleExoPlayer requestPlayer(@NonNull ExoCreator creator) {
    SimpleExoPlayer player = getPool(checkNotNull(creator)).acquire();
    if (player == null) player = creator.createPlayer();
    return player;
  }

  /**
   * Release player to Pool attached to the creator.
   *
   * @param creator the {@link ExoCreator} that created the player.
   * @param player the {@link SimpleExoPlayer} to be released back to the Pool
   * @return true if player is released to relevant Pool, false otherwise.
   */
  @SuppressWarnings({ "WeakerAccess", "UnusedReturnValue" }) //
  public final boolean releasePlayer(@NonNull ExoCreator creator, @NonNull SimpleExoPlayer player) {
    return getPool(checkNotNull(creator)).release(player);
  }

  /**
   * Release and clear all current cached ExoPlayer instances. This should be called when
   * client Application runs out of memory ({@link Application#onTrimMemory(int)} for example).
   */
  public final void cleanUp() {
    // TODO [2018/03/07] Test this. Ref: https://stackoverflow.com/a/1884916/1553254
    for (Iterator<Map.Entry<ExoCreator, Pools.Pool<SimpleExoPlayer>>> it =
        playerPools.entrySet().iterator(); it.hasNext(); ) {
      Pools.Pool<SimpleExoPlayer> pool = it.next().getValue();
      SimpleExoPlayer item;
      while ((item = pool.acquire()) != null) item.release();
      it.remove();
    }
  }

  /// internal APIs
  private Pools.Pool<SimpleExoPlayer> getPool(ExoCreator creator) {
    Pools.Pool<SimpleExoPlayer> pool = playerPools.get(creator);
    if (pool == null) {
      pool = new Pools.SimplePool<>(MAX_POOL_SIZE);
      playerPools.put(creator, pool);
    }

    return pool;
  }

  /**
   * Get a possibly-non-localized String from existing resourceId.
   */
  /* pkg */ String getString(@StringRes int resId, @Nullable Object... params) {
    return params == null ? this.context.getString(resId) : this.context.getString(resId, params);
  }

  /**
   * Utility method to build a {@link DrmSessionManager} that can be used in {@link Config}
   *
   * Usage:
   * <pre><code>
   *   DrmSessionManager manager = ToroExo.with(context).createDrmSessionManager(mediaDrm, null);
   *   Config config = new Config.Builder().setDrmSessionManagers([manager]);
   *   ExoCreator creator = ToroExo.with(context).getCreator(config);
   * </code></pre>
   */
  @RequiresApi(18) @Nullable public DrmSessionManager<FrameworkMediaCrypto> createDrmSessionManager(
      @NonNull DrmMedia drmMedia, @Nullable Handler handler) {
    DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
    int errorStringId = R.string.error_drm_unknown;
    String subString = null;
    if (Util.SDK_INT < 18) {
      errorStringId = R.string.error_drm_not_supported;
    } else {
      UUID drmSchemeUuid = getDrmUuid(checkNotNull(drmMedia).getType());
      if (drmSchemeUuid == null) {
        errorStringId = R.string.error_drm_unsupported_scheme;
      } else {
        HttpDataSource.Factory factory = new DefaultHttpDataSourceFactory(appName);
        try {
          drmSessionManager = buildDrmSessionManagerV18(drmSchemeUuid, drmMedia.getLicenseUrl(),
              drmMedia.getKeyRequestPropertiesArray(), drmMedia.multiSession(), factory, handler);
        } catch (UnsupportedDrmException e) {
          e.printStackTrace();
          errorStringId = e.reason == REASON_UNSUPPORTED_SCHEME ? //
              R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown;
          if (e.reason == REASON_UNSUPPORTED_SCHEME) {
            subString = drmMedia.getType();
          }
        }
      }
    }

    if (drmSessionManager == null) {
      String error = TextUtils.isEmpty(subString) ? context.getString(errorStringId)
          : context.getString(errorStringId) + ": " + subString;
      Toast.makeText(context, error, LENGTH_SHORT).show();
      Log.e(TAG, "createDrmSessionManager: " + error);
    }

    return drmSessionManager;
  }

  @RequiresApi(18) private static DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManagerV18(
      @NonNull UUID uuid, @Nullable String licenseUrl, @Nullable String[] keyRequestPropertiesArray,
      boolean multiSession, @NonNull HttpDataSource.Factory httpDataSourceFactory,
      @Nullable Handler handler) throws UnsupportedDrmException {
    HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl, httpDataSourceFactory);
    if (keyRequestPropertiesArray != null) {
      for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
        drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
            keyRequestPropertiesArray[i + 1]);
      }
    }
    return new DefaultDrmSessionManager<>(uuid, FrameworkMediaDrm.newInstance(uuid), drmCallback,
        null, handler, null, multiSession);
  }

  // Share the code of setting Volume. For use inside library only.
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public static void setVolumeInfo(@NonNull SimpleExoPlayer player, @NonNull VolumeInfo volumeInfo) {
    if (player instanceof ToroExoPlayer) {
      ((ToroExoPlayer) player).setVolumeInfo(volumeInfo);
    } else {
      if (volumeInfo.isMute()) {
        player.setVolume(0f);
      } else {
        player.setVolume(volumeInfo.getVolume());
      }
    }
  }

  @SuppressWarnings("WeakerAccess") @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public static VolumeInfo getVolumeInfo(SimpleExoPlayer player) {
    if (player instanceof ToroExoPlayer) {
      return new VolumeInfo(((ToroExoPlayer) player).getVolumeInfo());
    } else {
      float volume = player.getVolume();
      return new VolumeInfo(volume == 0, volume);
    }
  }
}