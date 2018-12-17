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

package im.ene.toro.sample.facebook.v2;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.sample.R;

class TimelineAdapter extends RecyclerView.Adapter<TimelineBaseViewHolder> {

  private final TimelineItems items;
  private LayoutInflater inflater;

  TimelineAdapter(TimelineItems items) {
    this.items = items;
    setHasStableIds(true);
  }

  @NonNull @Override
  public TimelineBaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (inflater == null || inflater.getContext() != parent.getContext()) {
      inflater = LayoutInflater.from(parent.getContext());
    }
    final View view = inflater.inflate(viewType, parent, false);
    if (viewType == R.layout.timeline_vh_video) {
      return new TimelineVideoViewHolder(view);
    }
    return new TimelineBaseViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull TimelineBaseViewHolder holder, int position) {
    holder.bind(getItem(position), position);
  }

  @Override public int getItemViewType(int position) {
    switch (position) {
      case 0:
        return R.layout.timeline_vh_stories;
      default:
        return getViewType(position);
    }
  }

  @Override public long getItemId(int position) {
    return position;
  }

  private int getViewType(int position) {
    Object item = getItem(position);
    if (!(item instanceof VideoItem)) return R.layout.timeline_vh_non_video;
    return R.layout.timeline_vh_video;
  }

  @Override public int getItemCount() {
    return items.size();
  }

  private Object getItem(int position) {
    return items.get(position);
  }
}
