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

package im.ene.toro.sample.nested;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.PlayerStateManager;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.common.DemoUtil;
import io.reactivex.Observable;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author eneim (7/1/17).
 */

public class NestedListAdapter extends RecyclerView.Adapter<BaseViewHolder>
    implements PlayerStateManager {

  private final int MEDIA_LIST_POSITION = 3;

  private final MediaList mediaList = new MediaList();

  @Override public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final View view;
    final BaseViewHolder viewHolder;
    if (viewType == 1000) {
      view = LayoutInflater.from(parent.getContext())
          .inflate(MediaListViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new MediaListViewHolder(view);
    } else {
      view = LayoutInflater.from(parent.getContext())
          .inflate(TextViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new TextViewHolder(view);
    }

    return viewHolder;
  }

  @Override public void onBindViewHolder(BaseViewHolder holder, int position) {
    holder.bind(position, position == MEDIA_LIST_POSITION ? mediaList : getClass().getSimpleName());
  }

  @Override public int getItemViewType(int position) {
    return position == MEDIA_LIST_POSITION ? 1000 : 1;  // 1000 --> MediaList
  }

  @Override public int getItemCount() {
    return Integer.MAX_VALUE;
  }

  @Override public void onViewDetachedFromWindow(BaseViewHolder holder) {
    super.onViewDetachedFromWindow(holder);
    if (holder instanceof MediaListViewHolder) {
      ((MediaListViewHolder) holder).detach();
    }
  }

  //// State manager

  private final Map<Integer, PlaybackInfo> stateCache = new TreeMap<>(DemoUtil::compare);

  @Override public void savePlaybackInfo(int order, @NonNull PlaybackInfo playbackInfo) {
    if (order >= 0) stateCache.put(order, playbackInfo);
  }

  @NonNull @Override public PlaybackInfo getPlaybackInfo(int order) {
    Integer entity = order >= 0 ? order : null;
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

  @Nullable @Override public Collection<Integer> getSavedPlayerOrders() {
    return Observable.fromIterable(stateCache.keySet()).toList().blockingGet();
  }
}
