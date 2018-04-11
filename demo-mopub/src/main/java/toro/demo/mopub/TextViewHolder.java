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

package toro.demo.mopub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

/**
 * @author eneim (2018/03/13).
 */

@SuppressWarnings({ "WeakerAccess", "FieldCanBeLocal" })  //
public class TextViewHolder extends BaseViewHolder {

  // My own photo, all right reserved.
  private static String image =
      "http://78.media.tumblr.com/d1929212f56afe00873be1dcb817d021/tumblr_n81obgYeRm1rsj2h0o5_1280.jpg";
  private static RequestOptions options = new RequestOptions().fitCenter();

  final ImageView imageView;
  final TextView textView;

  public TextViewHolder(ViewGroup parent, LayoutInflater inflater, int layoutRes) {
    super(parent, inflater, layoutRes);
    textView = itemView.findViewById(R.id.textContent);
    imageView = itemView.findViewById(R.id.imageContent);
  }

  @Override void bind(Object item) {
    super.bind(item);
    if (item instanceof Boolean) {
      if (Boolean.TRUE.equals(item)) {
        imageView.setVisibility(View.VISIBLE);
        Glide.with(itemView).load(image).apply(options.clone()).into(imageView);
      } else {
        imageView.setVisibility(View.GONE);
      }
    }

    textView.setText(R.string.small_text);
  }
}
