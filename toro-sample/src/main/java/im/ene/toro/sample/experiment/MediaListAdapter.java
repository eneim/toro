/*
 * Copyright 2017 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro.sample.experiment;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.BaseAdapter;
import im.ene.toro.PlaybackState;
import im.ene.toro.sample.data.OrderedVideoObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by eneim on 2/9/17.
 */

public class MediaListAdapter extends BaseAdapter<MediaItemViewHolder> {

  // cache data, mimic a data source
  private final List<OrderedVideoObject> items = new ArrayList<>();
  int maxObject = 0;

  @SuppressWarnings("WeakerAccess") ItemClickHandler itemClickHandler;

  public void setItemClickHandler(ItemClickHandler itemClickHandler) {
    this.itemClickHandler = itemClickHandler;
  }

  @NonNull @Override protected Object getItem(int position) {
    OrderedVideoObject item;
    if (items.size() <= position) {
      item = new OrderedVideoObject("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4", position);
      items.add(item);
    } else {
      item = items.get(position);
    }

    return item;
  }

  // ! IMPORTANT: this is must have for Drag & Drop behaviour
  @Override public long getItemId(int position) {
    return getItem(position).hashCode();
  }

  @Override public MediaItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(MediaItemViewHolder.LAYOUT_RES, parent, false);
    final MediaItemViewHolder viewHolder = new MediaItemViewHolder(view);
    viewHolder.setOnItemClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        // open full screen player?
        int pos = viewHolder.getAdapterPosition();
        if (pos != RecyclerView.NO_POSITION && itemClickHandler != null) {
          itemClickHandler.onItemClick(MediaListAdapter.this, viewHolder, v, pos, getItemId(pos));
        }
      }
    });
    return viewHolder;
  }

  @Override public int getItemCount() {
    return items.size();
  }

  public boolean moveItem(int fromPosition, int toPosition) {
    boolean changed = fromPosition != toPosition;
    if (fromPosition < toPosition) {
      for (int i = fromPosition; i < toPosition; i++) {
        Collections.swap(items, i, i + 1);
      }
    } else {
      for (int i = fromPosition; i > toPosition; i--) {
        Collections.swap(items, i, i - 1);
      }
    }

    if (changed) {
      notifyItemMoved(fromPosition, toPosition);
    }

    return changed;
  }

  // Actions
  void reset() {
    items.clear();
    notifyDataSetChanged();
  }

  void addItemNotify() {
    OrderedVideoObject item =
        new OrderedVideoObject("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4", maxObject++);
    items.add(0, item);
    notifyItemInserted(0);
  }

  void addItemNotifyAll() {
    OrderedVideoObject item =
        new OrderedVideoObject("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4", maxObject++);
    items.add(0, item);
    notifyDataSetChanged();
  }

  void removeItemNotify() {
    if (items.size() == 0) return;
    items.remove(0);
    notifyItemRemoved(0);
  }

  void removeItemNotifyAll() {
    if (items.size() == 0) return;
    items.remove(0);
    notifyDataSetChanged();
  }

  static abstract class ItemClickHandler implements OnItemClickListener {

    public abstract void openVideoPlayer(View view, OrderedVideoObject source, PlaybackState state);

    @Override
    public void onItemClick(RecyclerView.Adapter adapter, RecyclerView.ViewHolder viewHolder,
        View view, int position, long itemId) {
      if (viewHolder instanceof MediaItemViewHolder) {
        MediaItemViewHolder vh = (MediaItemViewHolder) viewHolder;
        PlaybackState state =
            new PlaybackState(vh.getMediaId(), vh.getDuration(), vh.getCurrentPosition());
        openVideoPlayer(view, (OrderedVideoObject) ((MediaListAdapter) adapter).getItem(position),
            state);
      }
    }
  }
}
