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

package toro.demo.ads.common

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder

/**
 * @author eneim (2018/08/21).
 */
open class BaseViewHolder(itemView: View) : ViewHolder(itemView) {

  open fun onBind(payload: Any?) {
  }
}