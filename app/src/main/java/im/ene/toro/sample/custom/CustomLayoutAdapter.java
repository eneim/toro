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

package im.ene.toro.sample.custom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import im.ene.toro.CacheManager;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;

/**
 * @author eneim (7/1/17).
 */

class CustomLayoutAdapter extends RecyclerView.Adapter<CustomExoPlayerViewHolder>
    implements CacheManager {

  private final MediaList mediaList;
  private final ItemClickListener itemClickListener;

  CustomLayoutAdapter(MediaList mediaList, ItemClickListener itemClickListener) {
    this.mediaList = mediaList;
    this.itemClickListener = itemClickListener;
  }

  @NonNull @Override
  public CustomExoPlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(CustomExoPlayerViewHolder.LAYOUT_RES, parent, false);
    CustomExoPlayerViewHolder viewHolder = new CustomExoPlayerViewHolder(view);
    ViewCompat.setTransitionName(viewHolder.playerView,
        parent.getContext().getString(R.string.transition_name_single_player));
    viewHolder.setClickListener(v -> {
      int pos = viewHolder.getAdapterPosition();
      if (itemClickListener != null && pos != RecyclerView.NO_POSITION) {
        itemClickListener.onItemClick(viewHolder.playerView, pos, mediaList.get(pos),
            viewHolder.getCurrentPlaybackInfo());
      }
    });
    return viewHolder;
  }

  @Override public void onBindViewHolder(@NonNull CustomExoPlayerViewHolder holder, int position) {
    holder.bind(mediaList.get(position));
  }

  @Override public int getItemCount() {
    return mediaList.size();
  }

  @NonNull @Override public Object getKeyForOrder(int order) {
    return this.mediaList.get(order);
  }

  @Nullable @Override public Integer getOrderForKey(@NonNull Object key) {
    return key instanceof Content.Media ? this.mediaList.indexOf(key) : null;
  }

  static abstract class ItemClickListener {

    abstract void onItemClick(View view, int position, Content.Media media,
        PlaybackInfo playbackInfo);
  }
}
