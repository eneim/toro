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

package im.ene.toro.youtube;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.CacheManager;

/**
 * @author eneim (8/1/17).
 */

public class YoutubeVideosAdapter extends RecyclerView.Adapter<YoutubePlayerViewHolder>
    implements CacheManager {

  private final FragmentManager fragmentManager;

  YoutubeVideosAdapter(FragmentManager fragmentManager) {
    setHasStableIds(true);
    this.fragmentManager = fragmentManager;
  }

  @Override public YoutubePlayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(YoutubePlayerViewHolder.LAYOUT_RES, parent, false);
    return new YoutubePlayerViewHolder(view);
  }

  @Override public void onBindViewHolder(YoutubePlayerViewHolder holder, int position) {
    holder.bind(this.fragmentManager, YOU_TUBES[position % YOU_TUBES.length]);
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override public int getItemCount() {
    return Integer.MAX_VALUE;
  }

  private static final String[] YOU_TUBES = {
      "J3IvOfvH1ys", "e7t3svG9PTk", "9m6MoBM-sFI", "7BR7Iee_mz8", "PZqzvs-AXYA", "Pms0pcyPbAM"
  };

  @Nullable @Override public Object getKeyForOrder(int order) {
    return order;
  }

  @Nullable @Override public Integer getOrderForKey(@NonNull Object key) {
    return (Integer) key;
  }
}
