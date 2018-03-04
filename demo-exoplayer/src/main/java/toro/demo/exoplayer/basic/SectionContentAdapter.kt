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

import android.support.v4.util.Pair
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.ArrayList

/**
 * @author eneim (2018/01/23).
 */
class SectionContentAdapter : Adapter<BaseViewHolder>() {

    companion object {
        const val videoItem = "div > video"

        const val typeText = 1
        const val typeVideo = 2
    }

    private val elements = Elements()

    fun updateElements(elements: ArrayList<Pair<String, Element>>) {
        val oldLength = this.elements.size
        this.elements.clear()
        super.notifyItemRangeRemoved(0, oldLength)
        elements.forEach({ it -> Motion.flatten(this.elements, it.second) })
        super.notifyItemRangeInserted(0, this.elements.size)
    }

    private var inflater: LayoutInflater? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        if (inflater === null || inflater!!.context !== parent.context) {
            inflater = LayoutInflater.from(parent.context)
        }

        return if (viewType == typeVideo) VideoViewHolder(inflater, parent)
        else TextViewHolder(inflater, parent)
    }

    override fun getItemCount() = this.elements.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(elements[position])
    }

    override fun getItemViewType(position: Int): Int {
        val item = elements[position]
        val vidCount = item.select(videoItem).size
        return if (vidCount > 0) typeVideo else typeText
    }
}