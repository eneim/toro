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

package im.ene.toro;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import im.ene.toro.widget.Container;

/**
 * @author eneim | 5/31/17.
 *
 *         This interface helps {@link Container} to observe the position of {@link ToroPlayer}
 *         correctly. {@link Container}, which is just another {@link RecyclerView}, uses the
 *         information provided by {@link RecyclerView.LayoutManager} to watch the {@link
 *         RecyclerView.ViewHolder}s those are bound. {@link LinearLayoutManager}, {@link
 *         GridLayoutManager}, {@link StaggeredGridLayoutManager} come with useful method to tell
 *         {@link Container} about its child position.
 *
 *         If user uses a custom {@link RecyclerView.LayoutManager}, it must also be able to tell
 *         {@link Container} the same level of information. So user must implement this interface
 *         into his/her custom {@link RecyclerView.LayoutManager} to help {@link Container} to
 *         behave correctly.
 *
 *         See {@link LinearLayoutManager}
 *         See {@link GridLayoutManager}
 *         See {@link StaggeredGridLayoutManager}
 */

public interface ToroLayoutManager {

  /**
   * @return The Adapter position of the first visible {@link RecyclerView.ViewHolder}.
   */
  int getFirstVisibleItemPosition();

  /**
   * @return The Adapter position of the last visible {@link RecyclerView.ViewHolder}.
   */
  int getLastVisibleItemPosition();
}
