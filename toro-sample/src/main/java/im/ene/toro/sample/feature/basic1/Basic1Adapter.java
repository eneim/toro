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

package im.ene.toro.sample.feature.basic1;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.ToroAdapter;
import im.ene.toro.sample.data.SimpleObject;
import im.ene.toro.sample.data.SimpleVideoObject;

/**
 * Created by eneim on 6/29/16.
 */
public class Basic1Adapter extends ToroAdapter<ToroAdapter.ViewHolder> {

  static int TYPE_VIDEO = 1;

  static int TYPE_NORMAL = 2;

  private LayoutInflater inflater;

  public Basic1Adapter() {
    super();
  }

  @Override public ToroAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final View view;
    final ToroAdapter.ViewHolder viewHolder;
    if (inflater == null) {
      inflater = LayoutInflater.from(parent.getContext());
    }

    if (viewType == TYPE_VIDEO) {
      view = inflater.inflate(Basic1VideoViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new Basic1VideoViewHolder(view);
    } else {
      view = inflater.inflate(Basic1NormalViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new Basic1NormalViewHolder(view);
    }

    return viewHolder;
  }

  // Comment out because parent class already deal with this.
  // TODO Un-comment for custom behaviour
  //@Override public void onBindViewHolder(ToroAdapter.ViewHolder holder, int position) {
  //  super.onBindViewHolder(holder, position);
  //}

  @Nullable @Override protected Object getItem(int position) {
    if (position % 3 == 0) {
      return new SimpleVideoObject("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
    } else {
      return new SimpleObject();
    }
  }

  @Override public int getItemViewType(int position) {
    return position % 3 == 0 ? TYPE_VIDEO : TYPE_NORMAL;
  }

  @Override public int getItemCount() {
    return 512;
  }

}
