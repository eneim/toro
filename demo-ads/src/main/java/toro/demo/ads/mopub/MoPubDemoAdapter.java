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

package toro.demo.ads.mopub;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import toro.demo.ads.R;
import toro.demo.ads.common.BaseViewHolder;

/**
 * @author eneim (2018/08/21).
 */
class MoPubDemoAdapter extends RecyclerView.Adapter<BaseViewHolder> {
  private static final int ITEM_COUNT = 150;

  @NonNull @Override
  public BaseViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
    final View itemView =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.vh_video_player, parent, false);
    return new VideoViewHolder(itemView);
  }

  @Override public void onBindViewHolder(@NonNull final BaseViewHolder holder, final int position) {
    holder.onBind(position);
  }

  @Override public long getItemId(final int position) {
    return (long) position;
  }

  @Override public int getItemCount() {
    return ITEM_COUNT;
  }
}
