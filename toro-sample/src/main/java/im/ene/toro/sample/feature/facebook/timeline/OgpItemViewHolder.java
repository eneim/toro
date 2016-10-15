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

package im.ene.toro.sample.feature.facebook.timeline;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import im.ene.toro.sample.R;

/**
 * Created by eneim on 10/11/16.
 */

public class OgpItemViewHolder extends TimelineViewHolder {

  static final int LAYOUT_RES = R.layout.vh_fb_feed_post_text;

  View ogpView;
  ImageView imageView;

  public OgpItemViewHolder(View itemView) {
    super(itemView);
    ogpView = itemView.findViewById(R.id.ogp_item);
    imageView = (ImageView) itemView.findViewById(R.id.item_image);
  }

  @Override public void bind(RecyclerView.Adapter adapter, @Nullable Object object) {
    Glide.with(itemView.getContext()).load(R.drawable.toro_icon).into(imageView);
  }

  @Override public void setOnItemClickListener(View.OnClickListener listener) {
    super.setOnItemClickListener(listener);
    ogpView.setOnClickListener(listener);
  }
}
