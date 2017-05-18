/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro.sample.feature.facebook.playlist;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.ToroAdapter;
import im.ene.toro.extended.ExtToroAdapter;
import im.ene.toro.sample.feature.facebook.timeline.TimelineItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eneim on 10/13/16.
 */

public class MoreVideosAdapter extends ExtToroAdapter<ToroAdapter.ViewHolder> {

  static final int VIDEO_TYPE_FIRST = 0;
  static final int VIDEO_TYPE_NEXT = 1;

  private final List<TimelineItem.VideoItem> items;

  public MoreVideosAdapter(@NonNull TimelineItem.VideoItem firstItem) {
    this.items = new ArrayList<>();
    this.items.add(firstItem);
  }

  @Nullable @Override protected TimelineItem.VideoItem getItem(int position) {
    return items.get(position);
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final View view;
    final ViewHolder viewHolder;
    if (viewType == VIDEO_TYPE_FIRST) {
      view = LayoutInflater.from(parent.getContext())
          .inflate(FirstItemViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new FirstItemViewHolder(view);
    } else {
      view = LayoutInflater.from(parent.getContext())
          .inflate(NextItemViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new NextItemViewHolder(view);
    }

    // TODO Setup click listener
    return viewHolder;
  }

  @Override public int getItemViewType(int position) {
    return position == 0 ? VIDEO_TYPE_FIRST : VIDEO_TYPE_NEXT;
  }

  @Override public int getItemCount() {
    return items.size();
  }

  public void addAll(List<TimelineItem.VideoItem> newItems) {
    synchronized (this) {
      int oldLen = getItemCount();
      this.items.addAll(newItems);
      notifyItemRangeInserted(oldLen, newItems.size());
    }
  }
}
