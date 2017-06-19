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

package im.ene.toro.sample.features.facebook.timeline;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import im.ene.toro.sample.R;
import im.ene.toro.sample.common.BaseFragment;
import im.ene.toro.widget.Container;

/**
 * @author eneim | 6/18/17.
 */

public class TimelineFragment extends BaseFragment {

  @SuppressWarnings("unused") public static TimelineFragment newInstance() {
    Bundle args = new Bundle();
    TimelineFragment fragment = new TimelineFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @BindView(R.id.recycler_view) Container container;
  TimelineAdapter adapter;
  RecyclerView.LayoutManager layoutManager;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle bundle) {
    return inflater.inflate(R.layout.layout_container_facebook, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    adapter = new TimelineAdapter(System.currentTimeMillis());
    layoutManager = new LinearLayoutManager(getContext());
    container.setAdapter(adapter);
    container.setLayoutManager(layoutManager);
    container.setPlayerStateManager(adapter);
  }

  @Override public void onDestroyView() {
    container.setAdapter(null);
    super.onDestroyView();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    adapter = null;
    layoutManager = null;
  }
}
