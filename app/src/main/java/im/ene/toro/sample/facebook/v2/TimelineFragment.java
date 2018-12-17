/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
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

package im.ene.toro.sample.facebook.v2;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.toro.CacheManager;
import im.ene.toro.sample.R;
import im.ene.toro.sample.common.BaseFragment;
import im.ene.toro.widget.Container;

public class TimelineFragment extends BaseFragment implements CacheManager {

  public static TimelineFragment newInstance() {
    Bundle args = new Bundle();
    TimelineFragment fragment = new TimelineFragment();
    fragment.setArguments(args);
    return fragment;
  }

  Container container;
  TimelineAdapter timelineAdapter;

  TimelineViewModel viewModel;

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle bundle) {
    return inflater.inflate(R.layout.fragment_facebook_timeline_v2, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    viewModel = ViewModelProviders.of(this).get(TimelineViewModel.class);
    container = view.findViewById(R.id.recycler_view);
    container.setCacheManager(this);
    viewModel.getItemsLiveData().observe(getViewLifecycleOwner(), items -> {
      timelineAdapter = new TimelineAdapter(items);
      container.setAdapter(timelineAdapter);
    });
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    if (container != null) container.setCacheManager(null);
  }

  @Nullable @Override public Object getKeyForOrder(int order) {
    return order;
  }

  @Nullable @Override public Integer getOrderForKey(@NonNull Object key) {
    return key instanceof Number ? ((Number) key).intValue() : null;
  }
}
