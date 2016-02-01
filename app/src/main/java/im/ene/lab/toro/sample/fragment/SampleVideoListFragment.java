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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.lab.toro.sample.adapter.SampleVideoUrlListAdapter;

/**
 * Created by eneim on 2/1/16.
 */
public class SampleVideoListFragment extends RecyclerViewFragment {

  public static SampleVideoListFragment newInstance() {
    Bundle args = new Bundle();
    SampleVideoListFragment fragment = new SampleVideoListFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @NonNull @Override protected RecyclerView.LayoutManager getLayoutManager() {
    return new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
  }

  @NonNull @Override protected RecyclerView.Adapter getAdapter() {
    return new SampleVideoUrlListAdapter();
  }
}
