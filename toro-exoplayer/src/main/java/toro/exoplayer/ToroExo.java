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

package toro.exoplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pools;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Map;

import static com.google.android.exoplayer2.util.Util.getUserAgent;
import static im.ene.toro.ToroUtil.LIB_NAME;
import static java.lang.Runtime.getRuntime;

/**
 * @author eneim (2018/01/26).
 */

public class ToroExo {

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

  final Context context;  // Application context
  final Map<Config, PlayerHub> configToHub;
  final String appName;

  @NonNull private final CookieManager cookieManager;
  @NonNull private final Map<Config, Pools.Pool<SimpleExoPlayer>> playerPools;
  @NonNull private final Map<Config, DataSource.Factory> factories;

  private ToroExo(Context context) {
    this.context = context.getApplicationContext();
    this.appName = getUserAgent(context.getApplicationContext(), LIB_NAME);
    this.configToHub = new HashMap<>();
    this.playerPools = new HashMap<>();
    this.factories = new HashMap<>();
    this.cookieManager = new CookieManager();
    this.cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
  }

  public PlayerHub.Builder builder() {
    return new PlayerHub.Builder(context);
  }

  public PlayerHub getHub() {
    return builder().build();
  }

  /// internal APIs
  Pools.Pool<SimpleExoPlayer> getPool(PlayerHub hub) {
    Pools.Pool<SimpleExoPlayer> pool = playerPools.get(hub.config);
    if (pool == null) {
      pool = new Pools.SimplePool<>(MAX_POOL_SIZE);
      playerPools.put(hub.config, pool);
    }

    return pool;
  }

  DataSource.Factory getDataSourceFactory(PlayerHub hub) {
    DataSource.Factory factory = factories.get(hub.config);
    if (factory == null) {
      //noinspection unchecked
      factory = new DefaultDataSourceFactory(context, appName, hub.config.meter);
      if (hub.config.cache != null) {
        factory = new CacheDataSourceFactory(hub.config.cache, factory);
      }
      factories.put(hub.config, factory);
    }

    return factory;
  }
}
