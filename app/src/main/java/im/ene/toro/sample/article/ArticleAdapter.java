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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author eneim (2018/01/06).
 */

public class ArticleAdapter extends RecyclerView.Adapter<BaseViewHolder> {

  static final String textItem = "body-section paragraph";
  static final String videoItem = "body-section video";
  static final String imageItem = "body-section js-image";

  static final int typeText = 1;
  static final int typeVideo = 2;
  static final int typeImage = 3;

  private Elements elements = new Elements();
  private LayoutInflater inflater;

  void setElements(Elements elements) {
    int oldCount = this.elements.size();
    this.elements.clear();
    notifyItemRangeRemoved(0, oldCount);
    this.elements.addAll(elements);
    notifyItemRangeInserted(0, this.elements.size());
  }

  @Override public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (inflater == null || inflater.getContext() != parent.getContext()) {
      inflater = LayoutInflater.from(parent.getContext());
    }

    return viewType == typeImage ? new ImageViewHolder(inflater, parent)
        : viewType == typeVideo ? new VideoViewHolder(inflater, parent)
            : new TextViewHolder(inflater, parent);
  }

  @Override public void onBindViewHolder(BaseViewHolder holder, int position) {
    holder.bind(elements.get(position));
  }

  @Override public int getItemViewType(int position) {
    Element item = elements.get(position);
    return imageItem.equalsIgnoreCase(item.className()) ? typeImage
        : videoItem.equalsIgnoreCase(item.className()) ? typeVideo : typeText;
  }

  @Override public int getItemCount() {
    return elements.size();
  }
}
