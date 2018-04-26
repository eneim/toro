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

package im.ene.toro.widget;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import im.ene.toro.CacheManager;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.media.PlaybackInfo;
import java.util.HashMap;

import static im.ene.toro.media.PlaybackInfo.SCRAP;

/**
 * @author eneim (2018/04/24).
 *
 * Design Target:
 *
 * [1] Manage the {@link PlaybackInfo} of current {@link ToroPlayer}. Should match 1-1 with the
 * {@link ToroPlayer}s that {@link PlayerManager} is managing.
 *
 * [2] If a non-null {@link CacheManager} provided to the {@link Container}, this class must
 * properly manage the {@link PlaybackInfo} of detached {@link ToroPlayer} and restore it to
 * previous state when being re-attached.
 */
final class PlaybackInfoCache extends RecyclerView.AdapterDataObserver
    implements OnAttachStateChangeListener {

  @SuppressWarnings("unused") //
  private static final String TAG = "ToroLib:InfoCache";

  @NonNull private final Container container;
  private HashMap<Object, PlaybackInfo> coldCache;
  private HashMap<Integer, PlaybackInfo> hotCache; // only cache attached Views.

  PlaybackInfoCache(@NonNull Container container) {
    this.container = container;
    // Setup
    this.container.addOnAttachStateChangeListener(this);
  }

  @SuppressLint("UseSparseArrays")  //
  @Override public void onViewAttachedToWindow(View v) {
    hotCache = new DebugHashMap<>();
  }

  @Override public void onViewDetachedFromWindow(View v) {
    this.container.removeOnAttachStateChangeListener(this);
    if (hotCache != null) {
      hotCache.clear();
      hotCache = null;
    }
  }

  final void onPlayerAttached(ToroPlayer player) {
    Integer playerOrder = player.getPlayerOrder();
    // [1] Check if there is cold cache for this player
    Object key = getKey(playerOrder);
    PlaybackInfo cache = key == null ? null : getCache().get(key);
    if (cache == null || cache == SCRAP) {
      // We init this even if there is no CacheManager available, because this is what User expects.
      cache = container.playerInitializer.initPlaybackInfo(playerOrder);
      // Only save to cold cache when there is a valid CacheManager (key is not null).
      if (key != null) getCache().put(key, cache);
    }

    if (hotCache != null) hotCache.put(playerOrder, cache);
  }

  // Will be called from Container#onChildViewDetachedFromWindow(View)
  // Therefore, it may not be called on all views. For example: when user close the App, by default
  // when RecyclerView is detached from Window, it will not call onChildViewDetachedFromWindow for
  // its children.
  // This method will:
  // [1] Take current hot cache entry of the player, and put back to cold cache.
  // [2] Remove the hot cache entry of the player.
  final void onPlayerDetached(ToroPlayer player) {
    Integer playerOrder = player.getPlayerOrder();
    if (hotCache != null && hotCache.containsKey(playerOrder)) {
      PlaybackInfo cache = hotCache.remove(playerOrder);
      Object key = getKey(playerOrder);
      if (key != null) getCache().put(key, cache);
    }
  }

  @SuppressWarnings("unused") final void onPlayerRecycled(ToroPlayer player) {
    // TODO do anything here?
  }

  @Override public void onChanged() {
    // TODO implement this
  }

  @Override public void onItemRangeChanged(int positionStart, int itemCount) {
    // TODO implement this
  }

  @Override public void onItemRangeInserted(int positionStart, int itemCount) {
    // TODO implement this
  }

  @Override public void onItemRangeRemoved(int positionStart, int itemCount) {
    // TODO implement this
  }

  @Override public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
    // TODO implement this.
  }

  @SuppressLint("UseSparseArrays") @NonNull //
  final HashMap<Object, PlaybackInfo> getCache() {
    if (this.coldCache == null) this.coldCache = new HashMap<>();
    return this.coldCache;
  }

  @Nullable private Object getKey(int position) {
    return position == RecyclerView.NO_POSITION ? null :  //
        container.getCacheManager() == null ? null
            : container.getCacheManager().getKeyForOrder(position);
  }

  @NonNull final PlaybackInfo getPlaybackInfo(int position) {
    if (position >= 0 && (hotCache != null && !hotCache.containsKey(position))) {
      throw new IllegalStateException("Unstable cache state: position is not in hot cache.");
    }

    Object key = getKey(position);
    PlaybackInfo info = hotCache != null ? hotCache.get(position) : null;
    if (info != null && info == SCRAP) {
      info = container.playerInitializer.initPlaybackInfo(position);
    }

    return info != null ? info :  //
        key != null ? getCache().get(key) : container.playerInitializer.initPlaybackInfo(position);
  }

  final void savePlaybackInfo(int position, @NonNull PlaybackInfo playbackInfo) {
    Object key = getKey(position);
    if (key != null) getCache().put(key, ToroUtil.checkNotNull(playbackInfo));
    if (hotCache != null) hotCache.put(position, playbackInfo);
  }

  final void clearCache() {
    getCache().clear();
    if (hotCache != null) hotCache.clear();
  }

  static class DebugHashMap<K, V> extends HashMap<K, V> {

    @Override public V put(K key, V value) {
      Log.d(TAG, "put() called with: key = [" + key + "], value = [" + value + "]");
      return super.put(key, value);
    }
  }
}
