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

package im.ene.toro.sample.feature.advance1;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import im.ene.toro.Toro;
import im.ene.toro.sample.R;

/**
 * Created by eneim on 6/30/16.
 */
public class Advance1ListFragment extends Fragment {

  protected RecyclerView mRecyclerView;
  protected RecyclerView.Adapter mAdapter;

  public static Advance1ListFragment newInstance() {
    return new Advance1ListFragment();
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.generic_recycler_view, container, false);
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2) @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
    mRecyclerView.getLayoutParams().height =
        getResources().getDimensionPixelSize(R.dimen.carousel_recycler_view_height);
    mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    Advance1LayoutManager layoutManager = getLayoutManager();
    layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());

    mRecyclerView.setLayoutManager(layoutManager);
    mAdapter = getAdapter();
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setAdapter(mAdapter);
    mRecyclerView.addOnScrollListener(new CenterScrollListener());
  }

  @Override public void onResume() {
    super.onResume();
    Toro.register(mRecyclerView);
  }

  @Override public void onPause() {
    super.onPause();
    Toro.unregister(mRecyclerView);
  }

  Advance1LayoutManager getLayoutManager() {
    return new Advance1LayoutManager(CarouselLayoutManager.VERTICAL);
  }

  RecyclerView.Adapter getAdapter() {
    return new Advance1Adapter();
  }
}
