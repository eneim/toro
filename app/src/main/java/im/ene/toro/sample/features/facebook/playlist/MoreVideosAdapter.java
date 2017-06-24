/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package im.ene.toro.sample.features.facebook.playlist;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.PlayerStateManager;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.common.DemoUtil;
import im.ene.toro.sample.features.facebook.data.FbVideo;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author eneim | 6/19/17.
 */

@SuppressWarnings("Range") public class MoreVideosAdapter
    extends RecyclerView.Adapter<MoreVideoItemViewHolder> implements PlayerStateManager {

  @NonNull private final FbVideo baseItem;
  private final long initTimeStamp;
  private final List<FbVideo> items = new ArrayList<>();

  public MoreVideosAdapter(@NonNull FbVideo baseItem, long initTimeStamp) {
    super();
    setHasStableIds(true);
    this.initTimeStamp = initTimeStamp;
    this.baseItem = baseItem;
  }

  @Override public long getItemId(int position) {
    return position;
  }

  public FbVideo getItem(@IntRange(from = 0) int position) {
    if (position == 0) return baseItem;
    int posInList = position - 1; // shift by 1.
    if (posInList >= items.size()) {
      for (int i = items.size(); i <= posInList; i++) {
        items.add(FbVideo.getItem(i, i, initTimeStamp + i * 60_000));
      }
    }

    return items.get(posInList);
  }

  @Override public MoreVideoItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(MoreVideoItemViewHolder.LAYOUT_RES, parent, false);
    return new MoreVideoItemViewHolder(view);
  }

  @Override public void onBindViewHolder(MoreVideoItemViewHolder holder, int position) {
    holder.bind(this, getItem(position), null);
  }

  @Override public int getItemCount() {
    return Integer.MAX_VALUE;
  }

  // Implement the PlayerStateManager;

  private final Map<FbVideo, PlaybackInfo> stateCache =
      new TreeMap<>((o1, o2) -> DemoUtil.compare(o1.getIndex(), o2.getIndex()));

  @Override public void savePlaybackInfo(int order, @NonNull PlaybackInfo playbackInfo) {
    if (order >= 0) stateCache.put(getItem(order), playbackInfo);
  }

  @NonNull @Override public PlaybackInfo getPlaybackInfo(int order) {
    FbVideo entity = order >= 0 ? getItem(order) : null;
    PlaybackInfo state = new PlaybackInfo();
    if (entity != null) {
      state = stateCache.get(entity);
      if (state == null) {
        state = new PlaybackInfo();
        stateCache.put(entity, state);
      }
    }
    return state;
  }

  // TODO return null if client doesn't want to save playback states on config change.
  @Nullable @Override public Collection<Integer> getSavedPlayerOrders() {
    return Observable.fromIterable(stateCache.keySet()).map(items::indexOf).toList().blockingGet();
  }
}
