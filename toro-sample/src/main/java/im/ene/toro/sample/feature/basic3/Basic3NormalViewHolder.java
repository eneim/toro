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

package im.ene.toro.sample.feature.basic3;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import im.ene.toro.sample.R;

/**
 * Created by eneim on 6/29/16.
 *
 * Sample ViewHolder which holds no Video. Or it doesn't request support from Toro.
 */
public class Basic3NormalViewHolder extends Basic3ViewHolder {

  public static final int LAYOUT_RES = R.layout.vh_normal_view;

  public Basic3NormalViewHolder(View itemView) {
    super(itemView);
  }

  @Override public void bind(RecyclerView.Adapter adapter, Object item) {

  }

  @Override public void onAttachedToWindow() {

  }

  @Override public void onDetachedFromWindow() {

  }
}
