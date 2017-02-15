/*
 * Copyright 2017 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro.sample.feature.home;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.sample.feature.Feature;

/**
 * Created by eneim on 2/15/17.
 */

public class FeatureAdapter extends RecyclerView.Adapter<FeatureItemViewHolder> {

  ItemClickListener itemClickListener;

  public void setItemClickListener(ItemClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
  }

  @Override public FeatureItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(FeatureItemViewHolder.LAYOUT_RES, parent, false);
    final FeatureItemViewHolder viewHolder = new FeatureItemViewHolder(view);
    viewHolder.itemButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        int pos = viewHolder.getAdapterPosition();
        if (pos != RecyclerView.NO_POSITION && itemClickListener != null) {
          itemClickListener.onItemClick(v, Feature.values()[pos]);
        }
      }
    });
    return viewHolder;
  }

  @Override public void onBindViewHolder(FeatureItemViewHolder holder, int position) {
    holder.bind(this, Feature.values()[position]);
  }

  @Override public int getItemCount() {
    return Feature.values().length;
  }

  public static abstract class ItemClickListener {

    public abstract void onItemClick(View view, Feature feature);
  }
}
