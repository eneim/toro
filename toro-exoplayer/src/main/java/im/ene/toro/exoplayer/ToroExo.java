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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.util.Pools;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import im.ene.toro.annotations.Beta;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Map;

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
 * <code>
 * ExoCreator creator = ToroExo.with(this).getDefaultCreator();
 * Playable playable = creator.createPlayable(uri);
 * playable.prepare();
 * // next: setup PlayerView and start the playback.
 * </code>
 *
 * @author eneim (2018/01/26).
 * @since 3.4.0
 */

@Beta // Currently in Beta testing.
public final class ToroExo {

  @SuppressLint("StaticFieldLeak") static volatile ToroExo toro;
  private static final int MAX_POOL_SIZE = Math.max(4, getRuntime().availableProcessors());

  public static ToroExo with(Context context) {
    if (toro == null) {
      synchronized (ToroExo.class) {
        if (toro == null) toro = new ToroExo(context);
      }
    }
    return toro;
  }

  @NonNull final String appName;
  @NonNull private final Context context;  // Application context
  @NonNull private final Map<Config, ExoCreator> creators;
  @NonNull private final Map<ExoCreator, Pools.Pool<SimpleExoPlayer>> playerPools;

  /* pkg */ Config defaultConfig; // will be created on the first time it is used.

  private ToroExo(Context context) {
    this.context = context.getApplicationContext();
    this.appName = getUserAgent(context.getApplicationContext(), LIB_NAME);
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
      creator = new DefaultExoCreator(context, config);
      this.creators.put(config, creator);
    }

    return creator;
  }

  /**
   * Get the default {@link ExoCreator}. This ExoCreator is configured by {@link #defaultConfig}.
   */
  public final ExoCreator getDefaultCreator() {
    if (defaultConfig == null) defaultConfig = new Config.Builder().build();
    return getCreator(defaultConfig);
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
    if (player.getPlaybackState() == Player.STATE_IDLE) { // should never happen
      throw new IllegalStateException("Player#" + player.hashCode() + " is not in idle state.");
    }
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
    if (checkNotNull(player).getPlaybackState() != Player.STATE_IDLE) {
      throw new IllegalStateException("Player must be stopped before releasing it back to Pool.");
    }
    return getPool(checkNotNull(creator)).release(player);
  }

  /**
   * Release and clear all current cached ExoPlayer instances. This should be called when
   * client Application runs out of memory ({@link Application#onTrimMemory(int)} for example).
   */
  public final void cleanUp() {
    for (Pools.Pool<SimpleExoPlayer> pool : playerPools.values()) {
      SimpleExoPlayer item;
      while ((item = pool.acquire()) != null) item.release();
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
}