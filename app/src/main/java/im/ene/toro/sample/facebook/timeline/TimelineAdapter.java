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

package im.ene.toro.sample.facebook.timeline;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.PlayerStateManager;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.common.DemoUtil;
import im.ene.toro.sample.facebook.data.FbVideo;
import im.ene.toro.sample.facebook.data.FbItem;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author eneim | 6/18/17.
 */

@SuppressWarnings({ "unused", "WeakerAccess" }) //
public class TimelineAdapter extends RecyclerView.Adapter<TimelineViewHolder>
    implements PlayerStateManager {

  private static final String TAG = "Toro:Fb:Adapter";

  static final int TYPE_OTHER = 1;
  static final int TYPE_VIDEO = 2;

  private final List<FbItem> items = new ArrayList<>();

  private final long initTimeStamp;
  @Nullable private Callback callback;

  TimelineAdapter(long initTimeStamp) {
    super();
    this.initTimeStamp = initTimeStamp;
    setHasStableIds(true);
  }

  public void setCallback(@Nullable Callback callback) {
    this.callback = callback;
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override public TimelineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final TimelineViewHolder viewHolder = TimelineViewHolder.createViewHolder(parent, viewType);
    viewHolder.setClickListener(v -> {
      int pos = viewHolder.getAdapterPosition();
      if (callback != null && pos != RecyclerView.NO_POSITION) {
        callback.onItemClick(viewHolder, v, getItem(pos), pos);
      }
    });
    return viewHolder;
  }

  @Override public int getItemViewType(int position) {
    FbItem item = getItem(position);
    return item instanceof FbVideo ? TYPE_VIDEO : TYPE_OTHER;
  }

  @Override public void onBindViewHolder(TimelineViewHolder holder, int position) {
    holder.bind(this, getItem(position), null);
  }

  @Override public void onViewRecycled(TimelineViewHolder holder) {
    holder.onRecycled();
  }

  @Override public int getItemCount() {
    return Integer.MAX_VALUE;
  }

  public FbItem getItem(int position) {
    if (position >= items.size()) {
      for (int i = items.size(); i <= position; i++) {
        items.add(FbVideo.getItem(i, i, initTimeStamp + i * 60_000));
      }
    }

    return items.get(position);
  }

  static abstract class Callback {

    abstract void onItemClick(@NonNull TimelineViewHolder viewHolder, @NonNull View view,
        @NonNull FbItem item, int position);
  }

  // Implement the PlayerStateManager;

  private final Map<FbItem, PlaybackInfo> stateCache =
      new TreeMap<>((o1, o2) -> DemoUtil.compare(o1.getIndex(), o2.getIndex()));

  @Override public void savePlaybackInfo(int order, @NonNull PlaybackInfo playbackInfo) {
    if (order >= 0) stateCache.put(getItem(order), playbackInfo);
  }

  @NonNull @Override public PlaybackInfo getPlaybackInfo(int order) {
    FbItem entity = order >= 0 ? getItem(order) : null;
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
