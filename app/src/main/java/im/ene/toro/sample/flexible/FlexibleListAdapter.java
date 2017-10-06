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

package im.ene.toro.sample.flexible;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.CacheManager;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;

/**
 * @author eneim (7/6/17).
 */

class FlexibleListAdapter extends RecyclerView.Adapter<FlexiblePlayerViewHolder>
    implements CacheManager {

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") //
  private final MediaList items = new MediaList();
  private final ItemClickListener itemClickListener;

  @SuppressWarnings("WeakerAccess")
  public FlexibleListAdapter(ItemClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
  }

  @SuppressWarnings("unused") public FlexibleListAdapter() {
    this.itemClickListener = null;
  }

  @Override public FlexiblePlayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(FlexiblePlayerViewHolder.LAYOUT_RES, parent, false);
    FlexiblePlayerViewHolder viewHolder = new FlexiblePlayerViewHolder(view);
    ViewCompat.setTransitionName(viewHolder.playerView,
        parent.getContext().getString(R.string.transition_name_single_player));
    viewHolder.setClickListener(v -> {
      int pos = viewHolder.getAdapterPosition();
      if (itemClickListener != null && pos != RecyclerView.NO_POSITION) {
        itemClickListener.onItemClick(viewHolder.playerView, pos, items.get(pos),
            viewHolder.getCurrentPlaybackInfo());
      }
    });
    return viewHolder;
  }

  @Override public void onBindViewHolder(FlexiblePlayerViewHolder holder, int position) {
    holder.bind(items.get(position), position);
  }

  @Override public int getItemCount() {
    return items.size();
  }

  boolean swap(int from, int to) {
    if (from == to) return false;
    items.move(from, to);
    notifyItemMoved(from, to);
    return true;
  }

  @Nullable @Override public Object getKeyForOrder(int order) {
    return items.get(order);
  }

  @Nullable @Override public Integer getOrderForKey(@NonNull Object key) {
    return key instanceof Content.Media ? items.indexOf(key) : null;
  }

  static abstract class ItemClickListener {

    abstract void onItemClick(View view, int position, Content.Media media,
        PlaybackInfo playbackInfo);
  }
}
