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

package im.ene.toro.sample.common;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import im.ene.toro.sample.R;
import im.ene.toro.sample.data.TextItem;
import java.util.List;

/**
 * @author eneim | 6/6/17.
 */

class TextViewHolder extends BaseViewHolder {

  static final int LAYOUT_RES = R.layout.vh_basic_text;

  @BindView(R.id.text_content) TextView content;

  TextViewHolder(View itemView) {
    super(itemView);
  }

  @Override
  public void bind(@NonNull RecyclerView.Adapter adapter, Object item, List<Object> payloads) {
    if (item instanceof TextItem) {
      content.setText(((TextItem) item).getContent());
    }
  }
}
