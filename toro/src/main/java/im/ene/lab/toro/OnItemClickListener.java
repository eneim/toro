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

package im.ene.lab.toro;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by eneim on 1/30/16.
 */
public interface OnItemClickListener {

  /**
   * @param itemId value from {@link RecyclerView.Adapter#getItemId(int)}
   */
  void onItemClick(ToroAdapter adapter, ToroAdapter.ViewHolder viewHolder, View view,
      int adapterPosition, long itemId);
}
