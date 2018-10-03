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

package toro.v4;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pools;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import im.ene.toro.ToroPlayer;
import im.ene.toro.exoplayer.Config;
import im.ene.toro.exoplayer.ExoCreator;
import im.ene.toro.exoplayer.Playable;
import im.ene.toro.exoplayer.ToroExo;
import im.ene.toro.helper.ToroPlayerHelper;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author eneim (2018/09/30).
 * @since 4.0.0
 *
 * Design:
 *
 * - Allow client to queue media Uri. Once queued, depending on the situation, it may start
 * prefetching the media.
 * - Create {@link ToroPlayerHelper} for the Uri on demand. One Uri can be used for multiple {@link
 * ToroPlayerHelper}.
 * - Manage total number of {@link ExoPlayer} instances, make sure the Client doesn't use too much
 * system resource.
 */
public final class MediaHub {

  static final class DebugPool<T> extends Pools.SimplePool<T> {

    DebugPool(int maxPoolSize) {
      super(maxPoolSize);
    }

    @Override public boolean release(@NonNull T instance) {
      return super.release(instance);
    }
  }

  @SuppressLint("StaticFieldLeak") //
  private static volatile MediaHub mediaHub;

  @SuppressWarnings("WeakerAccess") final HashMap<Media, Pools.Pool<Playable>> poolCache;
  @SuppressWarnings("WeakerAccess") final Context context;
  @SuppressWarnings("WeakerAccess") final ExoCreator creator;

  public static MediaHub getHub(Context context) {
    if (mediaHub == null) {
      synchronized (MediaHub.class) {
        if (mediaHub == null) mediaHub = new MediaHub(context);
      }
    }
    return mediaHub;
  }

  @SuppressWarnings("WeakerAccess") //
  MediaHub(Context context) {
    this.context = context.getApplicationContext();
    Cache cache =
        new SimpleCache(new File(this.context.getCacheDir(), "torox"), new NoOpCacheEvictor());
    Config config = new Config.Builder().setCache(cache).build();
    this.creator = ToroExo.with(this.context).getCreator(config);
    this.poolCache = new LinkedHashMap<>(1);
  }

  // Highest priority.
  @NonNull public ToroPlayerHelper requestHelper(ToroPlayer player, Media media) {
    Pools.Pool<Playable> pool = getPool(media);
    Playable playable = pool.acquire();
    if (playable == null) {
      playable = this.creator.createPlayable(media.getUri(), media.getExtension());
    }
    return new PlayerHelper(player, playable, media);
  }

  // Simpler version, allow developer to customize the behaviour while still make sure to have control.
  @NonNull public ToroPlayerHelper requestHelper(ToroPlayer player, Media media, Creator creator) {
    return creator.createHelper(player, media);
  }

  public void releaseHelper(ToroPlayerHelper helper) {
    helper.release();
    if (helper instanceof PlayerHelper) {
      PlayerHelper playerHelper = (PlayerHelper) helper;
      getPool(playerHelper.media).release(playerHelper.playable);
    }
  }

  private Pools.Pool<Playable> getPool(Media media) {
    Pools.Pool<Playable> pool = poolCache.get(media);
    if (pool == null) {
      pool = new DebugPool<>(4 /* TODO [20181003] Need a better limit */);
      poolCache.put(media, pool);
    }

    return pool;
  }

  public interface Creator {

    ToroPlayerHelper createHelper(ToroPlayer player, Media media);
  }
}
