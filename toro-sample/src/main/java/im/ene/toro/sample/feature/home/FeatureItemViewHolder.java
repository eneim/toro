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

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.ene.toro.sample.R;
import im.ene.toro.sample.feature.Feature;
import im.ene.toro.sample.widget.SampleItemButton;

/**
 * Created by eneim on 2/15/17.
 */

public class FeatureItemViewHolder extends RecyclerView.ViewHolder {

  static final int LAYOUT_RES = R.layout.vh_feature_item;

  @BindView(R.id.item_button) SampleItemButton itemButton;
  final Context context;

  public FeatureItemViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
    this.context = itemView.getContext();
  }

  public void bind(FeatureAdapter adapter, Feature feature) {
    itemButton.setTitle(context.getString(feature.title));
    itemButton.setSubTitle(context.getString(feature.description));
    itemButton.setImageResource(0);

    if (feature == Feature.EXPERIMENT) {
      itemButton.setBackgroundColor(Color.parseColor("#ffc5f507"));
    } else {
      itemButton.setBackgroundColor(Color.parseColor("#ffffffff"));
    }
  }
}
