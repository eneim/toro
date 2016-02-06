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

package im.ene.lab.toro.sample.viewholder;

import android.view.View;
import android.widget.TextView;
import im.ene.lab.toro.ToroAdapter;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.data.SimpleObject;

/**
 * Created by eneim on 1/30/16.
 */
public class NormalViewHolder extends ToroAdapter.ViewHolder {

  public static final int LAYOUT_RES = R.layout.vh_normal_view;

  private TextView mTextView;

  public NormalViewHolder(View itemView) {
    super(itemView);
    mTextView = (TextView) itemView.findViewById(R.id.text);
    if (mTextView == null) {
      throw new NullPointerException("Unusable ViewHolder");
    }
  }

  @Override public void bind(Object item) {
    if (!(item instanceof SimpleObject)) {
      throw new IllegalStateException("Unexpected object");
    }

    mTextView.setText(((SimpleObject) item).name);
  }

  @Override public String toString() {
    return "Normal " + getAdapterPosition();
  }
}
