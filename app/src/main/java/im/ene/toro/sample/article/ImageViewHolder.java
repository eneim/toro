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

package im.ene.toro.sample.article;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import im.ene.toro.sample.R;
import org.jsoup.nodes.Element;

/**
 * @author eneim (2018/01/06).
 */

public class ImageViewHolder extends BaseViewHolder {

  final ImageView imageView;
  final RequestOptions options = new RequestOptions().placeholder(R.drawable.exo_edit_mode_logo);

  ImageViewHolder(LayoutInflater inflater, ViewGroup parent) {
    super(inflater.inflate(R.layout.article_part_image, parent, false));
    this.imageView = (ImageView) itemView;
  }

  private String imageUrl;

  @Override void bind(Object object) {
    super.bind(object);
    Element element = object instanceof Element ? (Element) object : null;
    if (element != null) {
      imageUrl = element.getElementsByTag("img").attr("src");
    }

    if (imageUrl != null) {
      Glide.with(itemView.getContext()).load(imageUrl).apply(options).into(imageView);
    }
  }
}
