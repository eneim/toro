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

package im.ene.lab.toro.sample.fragment;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import com.fivehundredpx.greedolayout.GreedoLayoutManager;
import com.fivehundredpx.greedolayout.GreedoLayoutSizeCalculator;
import im.ene.lab.toro.ToroLayoutManager;
import im.ene.lab.toro.sample.adapter.GreedoVideoListAdapter;

/**
 * Created by eneim on 2/3/16.
 */
public class GreedoVideoListFragment extends RecyclerViewFragment {

  public static final String TAG = "SimpleVideoListFragment";

  public static GreedoVideoListFragment newInstance() {
    return new GreedoVideoListFragment();
  }

  private final GreedoVideoListAdapter mAdapter = new GreedoVideoListAdapter();

  @NonNull @Override protected RecyclerView.LayoutManager getLayoutManager() {
    return new TreedoLayoutManager(mAdapter);
  }

  @NonNull @Override protected RecyclerView.Adapter getAdapter() {
    return mAdapter;
  }

  // Toro + Greedo
  public static class TreedoLayoutManager extends GreedoLayoutManager implements ToroLayoutManager {

    public TreedoLayoutManager(
        GreedoLayoutSizeCalculator.SizeCalculatorDelegate sizeCalculatorDelegate) {
      super(sizeCalculatorDelegate);
    }

    @Override public int getFirstVisibleItemPosition() {
      return findFirstVisibleItemPosition();
    }

    @Override public int getLastVisibleItemPosition() {
      return getChildCount();
    }
  }
}
