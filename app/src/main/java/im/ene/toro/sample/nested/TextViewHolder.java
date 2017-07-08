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

package im.ene.toro.sample.nested;

import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.ene.toro.sample.R;

/**
 * @author eneim (7/1/17).
 */

public class TextViewHolder extends BaseViewHolder {

  static final int LAYOUT_RES = R.layout.view_holder_text;

  @BindView(R.id.text) TextView textView;

  public TextViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  @Override void bind(int position, Object object) {
    textView.setText(
        "Open source software powers the internet. Anyone using a computer uses open source, "
            + "either directly or indirectly. Although it has become the industry standard, "
            + "getting involved isn't always straightforward.");
  }
}
