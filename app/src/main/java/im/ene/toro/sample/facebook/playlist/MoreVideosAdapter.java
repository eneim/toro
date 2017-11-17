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

package im.ene.toro.sample.facebook.playlist;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.CacheManager;
import im.ene.toro.ToroPlayer;
import im.ene.toro.sample.facebook.data.FbVideo;
import java.util.ArrayList;
import java.util.List;

/**
 * @author eneim | 6/19/17.
 */

@SuppressWarnings("Range") public class MoreVideosAdapter
    extends RecyclerView.Adapter<MoreVideoItemViewHolder> implements CacheManager {

  @NonNull private final FbVideo baseItem;
  private final long initTimeStamp;
  private final List<FbVideo> items = new ArrayList<>();

  OnCompleteCallback onCompleteCallback;

  public void setOnCompleteCallback(OnCompleteCallback onCompleteCallback) {
    this.onCompleteCallback = onCompleteCallback;
  }

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
        items.add(FbVideo.getItem(i + 1, i + 1, initTimeStamp + (i + 1) * 60_000));
      }
    }

    return items.get(posInList);
  }

  @Override public MoreVideoItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(MoreVideoItemViewHolder.LAYOUT_RES, parent, false);
    MoreVideoItemViewHolder viewHolder = new MoreVideoItemViewHolder(view);
    viewHolder.setEventListener(new ToroPlayer.EventListener() {
      @Override public void onBuffering() {

      }

      @Override public void onPlaying() {

      }

      @Override public void onPaused() {

      }

      @Override public void onCompleted() {
        if (onCompleteCallback != null) onCompleteCallback.onCompleted(viewHolder);
      }
    });

    return viewHolder;
  }

  @Override public void onBindViewHolder(MoreVideoItemViewHolder holder, int position) {
    holder.bind(this, getItem(position), null);
  }

  @Override public int getItemCount() {
    return Integer.MAX_VALUE;
  }

  // Implement the CacheManager;

  @NonNull @Override public Object getKeyForOrder(int order) {
    return getItem(order);
  }

  @Nullable @Override public Integer getOrderForKey(@NonNull Object key) {
    return key instanceof FbVideo ? items.indexOf(key) : null;
  }

  // on complete stuff
  int findNextPlayerPosition(int base) {
    return base + 1;
  }

  static abstract class OnCompleteCallback {

    abstract void onCompleted(ToroPlayer player);
  }
}
