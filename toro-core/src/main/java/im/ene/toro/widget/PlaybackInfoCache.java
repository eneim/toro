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
import android.support.v7.widget.RecyclerView.OnChildAttachStateChangeListener;
import android.support.v7.widget.RecyclerView.RecyclerListener;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.media.PlaybackInfo;
import java.util.HashMap;

/**
 * @author eneim (2018/04/24).
 */
final class PlaybackInfoCache extends RecyclerView.AdapterDataObserver
    implements OnAttachStateChangeListener, OnChildAttachStateChangeListener, RecyclerListener {

  @SuppressWarnings("unused") //
  private static final String TAG = "ToroLib:InfoCache";
  // Mark an entry as scrap playback info, so that it requires an initialization.
  private static final PlaybackInfo SCRAP = new PlaybackInfo();

  @NonNull private final Container container;
  private HashMap<Object, PlaybackInfo> cache;
  private HashMap<Integer, PlaybackInfo> liveCache; // only cache attached Views.

  PlaybackInfoCache(@NonNull Container container) {
    this.container = container;
    // Setup
    this.container.addOnAttachStateChangeListener(this);
    this.container.addOnChildAttachStateChangeListener(this);
  }

  @SuppressLint("UseSparseArrays")  //
  @Override public void onViewAttachedToWindow(View v) {
    liveCache = new HashMap<>();
  }

  @Override public void onViewDetachedFromWindow(View v) {
    this.container.removeOnAttachStateChangeListener(this);
    if (liveCache != null) {
      liveCache.clear();
      liveCache = null;
    }
  }

  // Get called after Container#onChildViewAttachedToWindow(View).
  @Override public void onChildViewAttachedToWindow(View view) {
    RecyclerView.ViewHolder viewHolder = this.container.getChildViewHolder(view);
    if (!(viewHolder instanceof ToroPlayer)) return;
    final ToroPlayer player = (ToroPlayer) viewHolder;
    Integer playerOrder = player.getPlayerOrder();
    Object key = getKey(playerOrder);

    if (key == null) return;

    if (!getCache().containsKey(key)) {
      getCache().put(key, SCRAP);
    }

    PlaybackInfo current = getCache().get(key);
    if (current == null) throw new IllegalStateException("Unstable cache state: " + playerOrder);
    if (current == SCRAP) {
      current = container.playerInitializer.initPlaybackInfo(playerOrder);
      getCache().put(key, current);
    }

    // this.savePlaybackInfo(playerOrder, current);
    if (liveCache != null) liveCache.put(playerOrder, current);
  }

  @Override public void onChildViewDetachedFromWindow(View view) {
    // no-ops
    RecyclerView.ViewHolder viewHolder = this.container.getChildViewHolder(view);
    if (!(viewHolder instanceof ToroPlayer)) return;
    final ToroPlayer player = (ToroPlayer) viewHolder;
    Integer playerOrder = player.getPlayerOrder();
    if (liveCache != null) liveCache.remove(playerOrder);
  }

  @Override public void onViewRecycled(RecyclerView.ViewHolder holder) {
    if (!(holder instanceof ToroPlayer)) return;
    final ToroPlayer player = (ToroPlayer) holder;
    Integer playerOrder = player.getPlayerOrder();
    getCache().put(getKey(playerOrder), SCRAP);
    if (liveCache != null) liveCache.remove(playerOrder);
  }

  @Override public void onChanged() {
    super.onChanged();
    this.clearCache(); // we have no idea what is changed, so back to empty until we are all set.
  }

  @Override public void onItemRangeChanged(int positionStart, int itemCount) {
    for (int i = 0; i < itemCount; i++) {
      getCache().put(getKey(positionStart + i), SCRAP);
    }
  }

  @Override public void onItemRangeInserted(int positionStart, int itemCount) {
    for (int i = 0; i < itemCount; i++) {
      getCache().put(getKey(positionStart + i), SCRAP);
    }
  }

  @Override public void onItemRangeRemoved(int positionStart, int itemCount) {
    for (int i = 0; i < itemCount; i++) {
      getCache().remove(getKey(positionStart + i));
    }
  }

  @Override public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
    // TODO implement this.
  }

  @SuppressLint("UseSparseArrays") @NonNull final HashMap<Object, PlaybackInfo> getCache() {
    if (this.cache == null) this.cache = new HashMap<>();
    return this.cache;
  }

  @Nullable private Object getKey(int position) {
    return container.getCacheManager() == null ? null
        : container.getCacheManager().getKeyForOrder(position);
  }

  @NonNull final PlaybackInfo getPlaybackInfo(int position) {
    Object key = getKey(position);
    PlaybackInfo liveCached = liveCache != null ? liveCache.get(position) : new PlaybackInfo();
    return liveCached != null ? liveCached :  //
        key == null ? container.playerInitializer.initPlaybackInfo(position) : getCache().get(key);
  }

  final void savePlaybackInfo(int position, @NonNull PlaybackInfo playbackInfo) {
    getCache().put(getKey(position), ToroUtil.checkNotNull(playbackInfo));
    if (liveCache != null) liveCache.put(position, playbackInfo);
  }

  final void clearCache() {
    getCache().clear();
  }
}
