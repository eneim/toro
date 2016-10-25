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

package im.ene.toro.sample.feature.average1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.ToroAdapter;
import im.ene.toro.sample.data.SimpleObject;
import im.ene.toro.sample.data.SimpleVideoObject;

/**
 * Created by eneim on 6/29/16.
 */
public class Average1Adapter extends ToroAdapter<ToroAdapter.ViewHolder> {

  public Average1Adapter() {
    super();
    setHasStableIds(true);  // MUST have this.
  }

  @Override public ToroAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final View view;
    final ToroAdapter.ViewHolder viewHolder;
    if (viewType == Average1ViewHolder.TYPE_VIDEO) {
      view = LayoutInflater.from(parent.getContext())
          .inflate(Average1VideoViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new Average1VideoViewHolder(view);
    } else {
      view = LayoutInflater.from(parent.getContext())
          .inflate(Average1NormalViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new Average1NormalViewHolder(view);
    }

    return viewHolder;
  }

  @Override protected Object getItem(int position) {
    if (position % 3 == 1) {
      return new SimpleVideoObject("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
    } else {
      return new SimpleObject();
    }
  }

  @Override public int getItemViewType(int position) {
    return position % 3 == 1 ? Average1ViewHolder.TYPE_VIDEO : Average1ViewHolder.TYPE_NORMAL;
  }

  @Override public int getItemCount() {
    return 512;
  }

  // Toro requires this method to return item's unique Id.
  @Override public long getItemId(int position) {
    return position;
  }
}
