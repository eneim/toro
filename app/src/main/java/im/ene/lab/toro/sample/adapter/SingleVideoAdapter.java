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
import android.view.ViewGroup;
import im.ene.lab.toro.ToroAdapter;
import im.ene.lab.toro.sample.data.SimpleObject;
import im.ene.lab.toro.sample.data.SimpleVideoObject;
import im.ene.lab.toro.sample.data.VideoSource;
import im.ene.lab.toro.sample.viewholder.BaseViewHolder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eneim on 2/3/16.
 */
public class SingleVideoAdapter extends ToroAdapter<ToroAdapter.ViewHolder> {
  private List<SimpleVideoObject> mVideos = new ArrayList<>();

  public SingleVideoAdapter() {
    super();
    setHasStableIds(true);
    for (String item : VideoSource.SOURCES) {
      mVideos.add(new SimpleVideoObject(item));
    }
  }

  @Override public ToroAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
      @BaseViewHolder.Type int viewType) {
    return BaseViewHolder.createViewHolder(parent, viewType);
  }

  @BaseViewHolder.Type @Override public int getItemViewType(int position) {
    Object item = getItem(position);
    return item instanceof SimpleVideoObject ? BaseViewHolder.VIEW_TYPE_VIDEO
        : BaseViewHolder.VIEW_TYPE_NO_VIDEO;
  }

  @Override public long getItemId(int position) {
    if (getItem(position) != null) {
      return getItem(position).hashCode();
    } else {
      return 0;
    }
  }

  @Nullable @Override protected Object getItem(int position) {
    if (position != 3) {
      return new SimpleObject();
    }
    return mVideos.get(position % mVideos.size());
  }

  @Override public void onBindViewHolder(ViewHolder holder, int position) {
    super.onBindViewHolder(holder, position);
  }

  @Override public int getItemCount() {
    return 250;
  }
}
