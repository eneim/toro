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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import im.ene.lab.toro.sample.adapter.MultiVideosComplicatedListAdapter;

/**
 * Created by eneim on 2/3/16.
 */
public class MultiVideoComplicatedGridFragment extends RecyclerViewFragment {

  public static final String TAG = "MultiVideoComplicatedGridFragment";

  public static MultiVideoComplicatedGridFragment newInstance() {
    return new MultiVideoComplicatedGridFragment();
  }

  @NonNull @Override protected RecyclerView.LayoutManager getLayoutManager() {
    GridLayoutManager layoutManager =
        new GridLayoutManager(getContext(), 6, LinearLayoutManager.VERTICAL, false);
    layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
      @Override public int getSpanSize(int position) {
        return position % 6 == 0 ? 2 : position % 6 == 1 ? 4 : 3;
      }
    });

    return layoutManager;
  }

  @NonNull @Override protected RecyclerView.Adapter getAdapter() {
    return new MultiVideosComplicatedListAdapter();
  }
}
