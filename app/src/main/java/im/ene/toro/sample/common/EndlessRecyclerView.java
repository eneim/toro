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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import im.ene.toro.sample.data.DataSource;

/**
 * @author eneim | 6/7/17.
 */

public abstract class EndlessRecyclerView extends RecyclerView.OnScrollListener {

  // The minimum number of items remaining before we should loading more.
  private static final int VISIBLE_THRESHOLD = 5;

  private final LinearLayoutManager layoutManager;
  private final DataSource dataLoading;

  public EndlessRecyclerView(@NonNull LinearLayoutManager layoutManager,
      @NonNull DataSource dataLoading) {
    this.layoutManager = layoutManager;
    this.dataLoading = dataLoading;
  }

  @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
    // bail out if scrolling upward or already loading data
    if (dy < 0 || dataLoading.isLoading()) return;

    final int visibleItemCount = recyclerView.getChildCount();
    final int totalItemCount = layoutManager.getItemCount();
    final int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

    if ((totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBLE_THRESHOLD)) {
      onLoadMore();
    }
  }

  public abstract void onLoadMore();
}
