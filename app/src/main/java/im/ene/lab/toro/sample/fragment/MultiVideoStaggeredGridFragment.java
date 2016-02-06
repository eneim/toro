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
import android.support.v7.widget.StaggeredGridLayoutManager;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.adapter.MultiVideosSimpleListAdapter;

/**
 * Created by eneim on 2/1/16.
 */
public class MultiVideoStaggeredGridFragment extends RecyclerViewFragment {

  public static final String TAG = "StaggeredVideoListFragment";

  public static MultiVideoStaggeredGridFragment newInstance() {
    return new MultiVideoStaggeredGridFragment();
  }

  @NonNull @Override protected RecyclerView.LayoutManager getLayoutManager() {
    return new StaggeredGridLayoutManager(
        getContext().getResources().getInteger(R.integer.staggered_span_count),
        StaggeredGridLayoutManager.VERTICAL);
  }

  @NonNull @Override protected RecyclerView.Adapter getAdapter() {
    return new MultiVideosSimpleListAdapter();
  }
}
