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

package im.ene.lab.toro.sample.basic2;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.lab.toro.sample.data.SimpleObject;
import im.ene.lab.toro.sample.data.SimpleVideoObject;

/**
 * Created by eneim on 6/29/16.
 */
public class Basic2Adapter extends RecyclerView.Adapter<Basic2ViewHolder>
    implements OrderedPlaylist {

  public Basic2Adapter() {
    super();
    setHasStableIds(true);  // MUST have this.
  }

  @Override public Basic2ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final View view;
    final Basic2ViewHolder viewHolder;
    if (viewType == Basic2ViewHolder.TYPE_VIDEO) {
      view = LayoutInflater.from(parent.getContext())
          .inflate(Basic2VideoViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new Basic2VideoViewHolder(view);
    } else {
      view = LayoutInflater.from(parent.getContext())
          .inflate(Basic2NormalViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new Basic2NormalViewHolder(view);
    }

    return viewHolder;
  }

  @Override public void onBindViewHolder(Basic2ViewHolder holder, int position) {
    holder.bind(this, getItem(position));
  }

  Object getItem(int position) {
    if (position % 3 != 2) {
      return new SimpleVideoObject("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
    } else {
      return new SimpleObject();
    }
  }

  @Override public int getItemViewType(int position) {
    return position % 3 != 2 ? Basic2ViewHolder.TYPE_VIDEO : Basic2ViewHolder.TYPE_NORMAL;
  }

  @Override public int getItemCount() {
    return 512;
  }

  // Toro requires this method to return item's unique Id.
  @Override public long getItemId(int position) {
    return position;
  }

  /**
   * See {@link OrderedPlaylist#getFirstVideoPosition()}
   *
   * @return Position of the Video on top, or the Video user prefer to be played first.
   */
  @Override public int getFirstVideoPosition() {
    return 0; // In this sample, Videos are at position 0, 1, 3, 4 ...
    // HINT: Change this to 1 to see the Video number 1 be played first, instead of number 0.
  }
}
