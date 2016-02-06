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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.adapter.HorizontalVideosListAdapter;
import im.ene.lab.toro.sample.adapter.MultiVideosSimpleListAdapter;

/**
 * Created by eneim on 2/3/16.
 */
public class DualVideoListFragment extends Fragment {

  public static final String TAG = "DualVideoListFragment";

  public static DualVideoListFragment newInstance() {
    return new DualVideoListFragment();
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_dual_list, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    getChildFragmentManager().beginTransaction()
        .replace(R.id.fragment_top, TopFragment.newInstance()).commit();

    getChildFragmentManager().beginTransaction()
        .replace(R.id.fragment_bottom, BottomFragment.newInstance()).commit();
  }

  public static class TopFragment extends RecyclerViewFragment {

    public static TopFragment newInstance() {
      return new TopFragment();
    }

    @NonNull @Override protected RecyclerView.LayoutManager getLayoutManager() {
      return new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
    }

    @NonNull @Override protected RecyclerView.Adapter getAdapter() {
      return new HorizontalVideosListAdapter();
    }
  }

  public static class BottomFragment extends RecyclerViewFragment {

    public static BottomFragment newInstance() {
      return new BottomFragment();
    }

    @NonNull @Override protected RecyclerView.LayoutManager getLayoutManager() {
      return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
    }

    @NonNull @Override protected RecyclerView.Adapter getAdapter() {
      return new MultiVideosSimpleListAdapter();
    }
  }
}
