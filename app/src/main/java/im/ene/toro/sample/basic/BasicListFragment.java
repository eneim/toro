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

package im.ene.toro.sample.basic;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import im.ene.toro.PlayerSelector;
import im.ene.toro.sample.R;
import im.ene.toro.sample.common.BaseFragment;
import im.ene.toro.widget.Container;
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

/**
 * @author eneim (7/1/17).
 */

public class BasicListFragment extends BaseFragment {

  @SuppressWarnings("unused") public static BasicListFragment newInstance() {
    Bundle args = new Bundle();
    BasicListFragment fragment = new BasicListFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle bundle) {
    return inflater.inflate(R.layout.fragment_basic, container, false);
  }

  @BindView(R.id.toolbar_layout) CollapsingToolbarLayout toolbarLayout;
  @BindView(R.id.player_container) Container container;
  LinearLayoutManager layoutManager;
  BasicListAdapter adapter;

  @Override public void onViewCreated(View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    toolbarLayout.setTitle(getString(R.string.title_demo_basic));

    layoutManager = new LinearLayoutManager(getContext());
    container.setLayoutManager(layoutManager);
    adapter = new BasicListAdapter();
    container.setAdapter(adapter);

    // FIXME Only use the following workaround when using this Fragment in ViewPager.
    if (viewPagerMode) {
      container.setPlayerSelector(null);
      // Using TabLayout has a downside: once we click to a tab to change page, there will be no animation,
      // which will cause our setup doesn't work well. We need a delay to make things work.
      handler.postDelayed(() -> {
        if (container != null) container.setPlayerSelector(selector);
      }, 200);
    } else {
      // Normal case, just go forward.
      container.setPlayerSelector(selector);
    }
  }

  @Override public void onDestroyView() {
    handler.removeCallbacksAndMessages(null);
    layoutManager = null;
    adapter = null;
    selector = null;
    super.onDestroyView();
  }

  PlayerSelector selector = PlayerSelector.DEFAULT; // visible to user by default.
  final Handler handler = new Handler();  // post a delay due to the visibility change

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (isVisibleToUser) {
      selector = PlayerSelector.DEFAULT;
    } else {
      selector = PlayerSelector.NONE;
    }

    // Using TabLayout has a downside: once we click to a tab to change page, there will be no animation,
    // which will cause our setup doesn't work well. We need a delay to make things work.
    handler.postDelayed(() -> {
      if (container != null) container.setPlayerSelector(selector);
    }, 200);
  }
}
