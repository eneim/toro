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
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.util.Log;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import im.ene.toro.CacheManager;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.media.PlaybackInfo;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
@SuppressWarnings({ "WeakerAccess", "unused" }) @SuppressLint("UseSparseArrays")
final class PlaybackInfoCache extends AdapterDataObserver implements OnAttachStateChangeListener {

  private static final String TAG = "ToroLib:InfoCache";

  private static final Comparator<Integer> ORDER_COMPARATOR = new Comparator<Integer>() {
    @Override public int compare(Integer o1, Integer o2) {
      return o1.compareTo(o2);
    }
  };

  @NonNull private final Container container;
  /* pkg */ Map<Object, PlaybackInfo> coldCache = new HashMap<>();
  /* pkg */ Map<Integer, PlaybackInfo> hotCache; // only cache attached Views.
  /* pkg */ Map<Integer, Object> coldKeyToOrderMap = new TreeMap<>(ORDER_COMPARATOR);

  PlaybackInfoCache(@NonNull Container container) {
    this.container = container;
    this.container.addOnAttachStateChangeListener(this);
  }

  @Override public void onViewAttachedToWindow(View v) {
    hotCache = new TreeMap<>(ORDER_COMPARATOR);
  }

  @Override public void onViewDetachedFromWindow(View v) {
    this.container.removeOnAttachStateChangeListener(this);
    if (hotCache != null) {
      hotCache.clear();
      hotCache = null;
    }
    coldKeyToOrderMap.clear();
  }

  final void onPlayerAttached(ToroPlayer player) {
    Integer playerOrder = player.getPlayerOrder();
    // [1] Check if there is cold cache for this player
    Object key = getKey(playerOrder);
    coldKeyToOrderMap.put(playerOrder, key);

    PlaybackInfo cache = key == null ? null : coldCache.get(key);
    if (cache == null || cache == SCRAP) {
      // We init this even if there is no CacheManager available, because this is what User expects.
      cache = container.playerInitializer.initPlaybackInfo(playerOrder);
      // Only save to cold cache when there is a valid CacheManager (key is not null).
      if (key != null) coldCache.put(key, cache);
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
      if (key != null) coldCache.put(key, cache);
    }
  }

  @SuppressWarnings("unused") final void onPlayerRecycled(ToroPlayer player) {
    // TODO do anything here?
  }

  @Override public void onChanged() {
    if (container.getCacheManager() != null) {
      for (Integer key : coldKeyToOrderMap.keySet()) {
        coldCache.put(getKey(key), SCRAP);
        coldKeyToOrderMap.put(key, getKey(key));
      }
    }

    if (hotCache != null) {
      for (Integer key : hotCache.keySet()) {
        hotCache.put(key, SCRAP);
      }
    }
  }

  @Override public void onItemRangeChanged(final int positionStart, final int itemCount) {
    if (itemCount == 0) return;
    if (container.getCacheManager() != null) {
      Set<Integer> changedColdKeys = new TreeSet<>(ORDER_COMPARATOR);
      for (Integer key : coldKeyToOrderMap.keySet()) {
        if (key >= positionStart && key < positionStart + itemCount) {
          changedColdKeys.add(key);
        }
      }

      for (Integer key : changedColdKeys) {
        coldCache.put(getKey(key), SCRAP);
        coldKeyToOrderMap.put(key, getKey(key));
      }
    }

    if (hotCache != null) {
      Set<Integer> changedHotKeys = new TreeSet<>(ORDER_COMPARATOR);
      for (Integer key : hotCache.keySet()) {
        if (key >= positionStart && key < positionStart + itemCount) {
          changedHotKeys.add(key);
        }
      }

      for (Integer key : changedHotKeys) {
        hotCache.put(key, SCRAP);
      }
    }
  }

  @Override public void onItemRangeInserted(final int positionStart, final int itemCount) {
    if (itemCount == 0) return;
    // Cold cache update
    if (container.getCacheManager() != null) {
      // [1] Take keys of old one.
      // 1.1 Extract subset of keys only:
      Set<Integer> changedColdKeys = new TreeSet<>(ORDER_COMPARATOR);
      for (Integer key : coldKeyToOrderMap.keySet()) {
        if (key >= positionStart) {
          changedColdKeys.add(key);
        }
      }

      // 1.2 Extract entries from cold cache to a temp cache.
      final Map<Object, PlaybackInfo> changeColdEntriesCache = new HashMap<>();
      for (Integer key : changedColdKeys) {
        changeColdEntriesCache.put(key, coldCache.remove(coldKeyToOrderMap.get(key)));
      }

      // 1.2 Update cold Cache with new keys
      for (Integer key : changedColdKeys) {
        coldCache.put(getKey(key + itemCount), changeColdEntriesCache.get(key));
      }

      // 1.3 Update coldKeyToOrderMap;
      for (Integer key : changedColdKeys) {
        coldKeyToOrderMap.put(key, getKey(key));
      }
    }

    // [1] Remove cache if there is any appearance
    if (hotCache != null) {
      // [2] Shift cache by specific number
      Map<Integer, PlaybackInfo> changedHotEntriesCache = new HashMap<>();
      Set<Integer> changedHotKeys = new TreeSet<>(ORDER_COMPARATOR);
      for (Integer key : hotCache.keySet()) {
        if (key >= positionStart) {
          changedHotKeys.add(key);
        }
      }

      for (Integer key : changedHotKeys) {
        changedHotEntriesCache.put(key, hotCache.remove(key));
      }

      for (Integer key : changedHotKeys) {
        hotCache.put(key + itemCount, changedHotEntriesCache.get(key));
      }
    }
  }

  @Override public void onItemRangeRemoved(final int positionStart, final int itemCount) {
    if (itemCount == 0) return;
    // Cold cache update
    if (container.getCacheManager() != null) {
      // [1] Take keys of old one.
      // 1.1 Extract subset of keys only:
      Set<Integer> changedColdKeys = new TreeSet<>(ORDER_COMPARATOR);
      for (Integer key : coldKeyToOrderMap.keySet()) {
        if (key >= positionStart + itemCount) changedColdKeys.add(key);
      }
      // 1.2 Extract entries from cold cache to a temp cache.
      final Map<Object, PlaybackInfo> changeColdEntriesCache = new HashMap<>();
      for (Integer key : changedColdKeys) {
        changeColdEntriesCache.put(key, coldCache.remove(coldKeyToOrderMap.get(key)));
      }

      // 1.2 Update cold Cache with new keys
      for (Integer key : changedColdKeys) {
        coldCache.put(getKey(key - itemCount), changeColdEntriesCache.get(key));
      }

      // 1.3 Update coldKeyToOrderMap;
      for (Integer key : changedColdKeys) {
        coldKeyToOrderMap.put(key, getKey(key));
      }
    }

    // [1] Remove cache if there is any appearance
    if (hotCache != null) {
      for (int i = 0; i < itemCount; i++) {
        hotCache.remove(positionStart + i);
      }

      // [2] Shift cache by specific number
      Map<Integer, PlaybackInfo> changedHotEntriesCache = new HashMap<>();
      Set<Integer> changedHotKeys = new TreeSet<>(ORDER_COMPARATOR);
      for (Integer key : hotCache.keySet()) {
        if (key >= positionStart + itemCount) changedHotKeys.add(key);
      }

      for (Integer key : changedHotKeys) {
        changedHotEntriesCache.put(key, hotCache.remove(key));
      }

      for (Integer key : changedHotKeys) {
        hotCache.put(key - itemCount, changedHotEntriesCache.get(key));
      }
    }
  }

  // Dude I wanna test this thing >.<
  @Override public void onItemRangeMoved(final int fromPos, final int toPos, int itemCount) {
    if (fromPos == toPos) return;
    final int left = fromPos < toPos ? fromPos : toPos;
    final int right = fromPos + toPos - left;
    final int shift = fromPos < toPos ? -1 : 1;  // how item will be shifted

    // [1] Migrate cold cache.
    if (container.getCacheManager() != null) {
      // 1.1 Extract subset of keys only:
      Set<Integer> changedColdKeys = new TreeSet<>(ORDER_COMPARATOR);
      for (Integer key : coldKeyToOrderMap.keySet()) {
        if (key >= left && key <= right) changedColdKeys.add(key);
      }
      // 1.2 Extract entries from cold cache to a temp cache.
      final Map<Object, PlaybackInfo> changeColdEntriesCache = new HashMap<>();
      for (Integer key : changedColdKeys) {
        changeColdEntriesCache.put(key, coldCache.remove(coldKeyToOrderMap.get(key)));
      }

      // 1.2 Update cold Cache with new keys
      for (Integer key : changedColdKeys) {
        if (key == left) {
          coldCache.put(getKey(right), changeColdEntriesCache.get(key));
        } else {
          coldCache.put(getKey(key + shift), changeColdEntriesCache.get(key));
        }
      }

      // 1.3 Update coldKeyToOrderMap;
      for (Integer key : changedColdKeys) {
        coldKeyToOrderMap.put(key, getKey(key));
      }
    }

    // Update hot cache.
    if (hotCache != null) {
      Set<Integer> changedHotKeys = new TreeSet<>(ORDER_COMPARATOR);
      for (Integer key : hotCache.keySet()) {
        if (key >= left && key <= right) changedHotKeys.add(key);
      }

      Map<Integer, PlaybackInfo> changedHotEntriesCache = new HashMap<>();
      for (Integer key : changedHotKeys) {
        changedHotEntriesCache.put(key, hotCache.remove(key));
      }

      for (Integer key : changedHotKeys) {
        if (key == left) {
          hotCache.put(right, changedHotEntriesCache.get(key));
        } else {
          hotCache.put(key + shift, changedHotEntriesCache.get(key));
        }
      }
    }
  }

  /* pkg */
  @Nullable Object getKey(int position) {
    return position == RecyclerView.NO_POSITION ? null : container.getCacheManager() == null ? null
        : container.getCacheManager().getKeyForOrder(position);
  }

  @Nullable private Integer getOrder(Object key) {
    return container.getCacheManager() == null ? null
        : container.getCacheManager().getOrderForKey(key);
  }

  @NonNull final PlaybackInfo getPlaybackInfo(int position) {
    if (position >= 0 && (hotCache != null && !hotCache.containsKey(position))) {
      Log.e(TAG, "getPlaybackInfo: " + "Position is not in hot cache: " + position);
      hotCache.put(position, SCRAP);  // should not happen.
    }

    Object key = getKey(position);
    PlaybackInfo info = hotCache != null ? hotCache.get(position) : null;
    if (info != null && info == SCRAP) {  // has hot cache, but was SCRAP.
      info = container.playerInitializer.initPlaybackInfo(position);
    }

    return info != null ? info :  //
        key != null ? coldCache.get(key) : container.playerInitializer.initPlaybackInfo(position);
  }

  // Call by Container#savePlaybackInfo and that method is called right before any pausing.
  final void savePlaybackInfo(int position, @NonNull PlaybackInfo playbackInfo) {
    ToroUtil.checkNotNull(playbackInfo);
    if (hotCache != null) hotCache.put(position, playbackInfo);
    Object key = getKey(position);
    if (key != null) coldCache.put(key, playbackInfo);
  }

  final void clearCache() {
    coldCache.clear();
    if (hotCache != null) hotCache.clear();
  }
}
