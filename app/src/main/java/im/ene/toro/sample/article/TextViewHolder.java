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

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import im.ene.toro.sample.R;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

/**
 * @author eneim (2018/01/06).
 */

class TextViewHolder extends BaseViewHolder {

  final TextView textView;

  TextViewHolder(LayoutInflater inflater, ViewGroup parent) {
    super(inflater.inflate(R.layout.article_part_text, parent, false));
    this.textView = (TextView) itemView;
  }

  private Element element;

  @Override void bind(Object object) {
    super.bind(object);
    if (object instanceof Element) this.element = (Element) object;
    if (this.element != null) {
      textView.setText(Html.fromHtml(Jsoup.clean(element.html(), Whitelist.relaxed())));
      textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
  }
}
