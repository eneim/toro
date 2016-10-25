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

package im.ene.toro.sample.feature.legacy;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.ToroAdapter;
import im.ene.toro.sample.data.SimpleObject;
import im.ene.toro.sample.data.SimpleVideoObject;

/**
 * Created by eneim on 10/1/16.
 */

public class LegacyAdapter extends ToroAdapter<ToroAdapter.ViewHolder> {

  @Nullable @Override protected Object getItem(int position) {
    if (position % 3 == 0) {
      return new SimpleVideoObject("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
    } else {
      return new SimpleObject();
    }
  }

  @Override public ToroAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final View view;
    final ToroAdapter.ViewHolder viewHolder;
    if (viewType == LegacyViewHolder.TYPE_VIDEO) {
      view = LayoutInflater.from(parent.getContext())
          .inflate(LegacyVideoViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new LegacyVideoViewHolder(view);
    } else {
      view = LayoutInflater.from(parent.getContext())
          .inflate(LegacyNormalViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new LegacyNormalViewHolder(view);
    }

    return viewHolder;
  }

  @Override public int getItemViewType(int position) {
    return position % 3 == 0 ? LegacyViewHolder.TYPE_VIDEO : LegacyViewHolder.TYPE_NORMAL;
  }

  @Override public int getItemCount() {
    return 512;
  }
}
