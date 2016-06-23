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

package im.ene.lab.toro.sample.adapter;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.lab.toro.ext.ToroAdapter;
import im.ene.lab.toro.sample.data.SimpleObject;
import im.ene.lab.toro.sample.data.SimpleVideoObject;
import im.ene.lab.toro.sample.viewholder.HorizontalMixedToroVideoViewHolder;
import im.ene.lab.toro.sample.viewholder.HorizontalSimpleToroVideoViewHolder;
import im.ene.lab.toro.sample.viewholder.HorizontalTextViewHolder;

/**
 * Created by eneim on 1/30/16.
 */
public class HorizontalVideosListAdapter extends BaseSampleAdapter {

  public HorizontalVideosListAdapter() {
    super();
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final ToroAdapter.ViewHolder viewHolder;
    final View view;
    if (viewType == VIEW_TYPE_VIDEO) {
      view = LayoutInflater.from(parent.getContext())
          .inflate(HorizontalSimpleToroVideoViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new HorizontalSimpleToroVideoViewHolder(view);
    } else if (viewType == VIEW_TYPE_VIDEO_MIXED) {
      view = LayoutInflater.from(parent.getContext())
          .inflate(HorizontalMixedToroVideoViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new HorizontalMixedToroVideoViewHolder(view);
    } else {
      view = LayoutInflater.from(parent.getContext())
          .inflate(HorizontalTextViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new HorizontalTextViewHolder(view);
    }

    return viewHolder;
  }

  @Override public int getItemViewType(int position) {
    Object item = getItem(position);
    // Just for fun
    return !(item instanceof SimpleVideoObject) ? VIEW_TYPE_NO_VIDEO
        : position % 6 == 1 ? VIEW_TYPE_VIDEO_MIXED : VIEW_TYPE_VIDEO;
  }

  @Nullable @Override protected Object getItem(int position) {
    return position % 6 != 0 ? mVideos.get(0) : new SimpleObject();
  }
}
