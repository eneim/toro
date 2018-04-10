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

package toro.demo.exoplayer.basic

import android.support.v7.widget.AppCompatTextView
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.safety.Whitelist
import toro.demo.exoplayer.R
import toro.demo.exoplayer.common.htmlText

/**
 * @author eneim (2018/01/23).
 */
internal class TextViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        BaseViewHolder(inflater.inflate(R.layout.article_part_text, parent, false)) {

    private val textView by lazy { this.itemView as AppCompatTextView }

    override fun bind(item: Any?) {
        super.bind(item)
        if (item is Element) {
            textView.htmlText(Jsoup.clean(item.outerHtml(), Whitelist.relaxed()))
            textView.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}