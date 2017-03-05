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

package im.ene.toro.sample.experiment.action;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.ene.toro.ToroAdapter;
import im.ene.toro.sample.R;

/**
 * Created by eneim on 2/18/17.
 */

public class ActionViewHolder extends ToroAdapter.ViewHolder {

  static final int LAYOUT_RES = R.layout.vh_action_button;

  @BindView(R.id.action_button) Button actionButton;

  public ActionViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  @Override public void onAttachedToWindow() {

  }

  @Override public void onDetachedFromWindow() {

  }

  @Override public void bind(RecyclerView.Adapter adapter, @Nullable Object object) {
    if (object != null) {
      actionButton.setText(object.toString());
    }
  }
}
