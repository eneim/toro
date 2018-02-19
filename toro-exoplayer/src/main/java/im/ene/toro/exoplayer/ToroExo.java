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
import com.google.android.exoplayer2.SimpleExoPlayer;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Map;

import static com.google.android.exoplayer2.util.Util.getUserAgent;
import static im.ene.toro.exoplayer.BuildConfig.LIB_NAME;
import static java.lang.Runtime.getRuntime;

/**
 * @author eneim (2018/01/26).
 *
 *         Global helper class to manage {@link ExoCreator} and {@link SimpleExoPlayer} instances.
 *
 *         In this setup, {@link ExoCreator} and SimpleExoPlayer pools are cached. A {@link Config}
 *         is a key for each {@link ExoCreator}.
 *
 *         ExoCreator creator = ToroExo.with(this).getDefaultCreator();
 *         SimpleExoPlayer player = creator.createPlayer();
 *         MediaSource source = creator.createMediaSource(videoUri);
 *         // next: do stuff with SimpleExoPlayer instance and MediaSource instance.
 */

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

  final String appName;
  final Config defaultConfig = new Config.Builder().build();

  @NonNull private final Context context;  // Application context
  @NonNull private final Map<Config, ExoCreator> creators;
  @NonNull private final Map<ExoCreator, Pools.Pool<SimpleExoPlayer>> playerPools;

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

  public final ExoCreator getCreator(Config config) {
    ExoCreator creator = this.creators.get(config);
    if (creator == null) {
      creator = new DefaultExoCreator(context, config);
      this.creators.put(config, creator);
    }

    return creator;
  }

  public final ExoCreator getDefaultCreator() {
    return getCreator(defaultConfig);
  }

  /**
   * Request an instance of {@link SimpleExoPlayer}. It can be an existing instance cached by Pool
   * or new one.
   *
   * @param creator the {@link ExoCreator} that is scoped to the {@link SimpleExoPlayer} config.
   * @return an usable {@link SimpleExoPlayer} instance.
   */
  @SuppressWarnings("WeakerAccess") @NonNull  //
  public final SimpleExoPlayer requestPlayer(ExoCreator creator) {
    //noinspection UnusedAssignment
    SimpleExoPlayer player = getPool(creator).acquire();
    if (player == null) {
      player = creator.createPlayer();
    }

    return player;
  }

  /**
   * @param creator the {@link ExoCreator} that created the player.
   * @param player the {@link SimpleExoPlayer} to be released back to the Pool
   * @return true if player is released to relevant Pool, false otherwise.
   */
  @SuppressWarnings("WeakerAccess") //
  public final boolean releasePlayer(ExoCreator creator, SimpleExoPlayer player) {
    return getPool(creator).release(player);
  }

  /**
   * Release and clear all current cached ExoPlayer instances. This should be called when
   * client Application runs out of memory ({@link Application#onTrimMemory(int)} for example).
   */
  public final void cleanUp() {
    for (Pools.Pool<SimpleExoPlayer> pool : playerPools.values()) {
      SimpleExoPlayer item;
      while ((item = pool.acquire()) != null) {
        item.release();
      }
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

  String getString(@StringRes int resId, @Nullable Object... params) {
    return params == null ? this.context.getString(resId) : this.context.getString(resId, params);
  }
}