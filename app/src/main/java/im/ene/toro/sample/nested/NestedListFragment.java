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

package im.ene.toro.sample.nested;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import butterknife.BindView;
import im.ene.toro.PlayerSelector;
import im.ene.toro.sample.R;
import im.ene.toro.sample.common.BaseFragment;
import im.ene.toro.widget.Container;
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

/**
 * A list of content that contains a {@link Container} as one of its child. We gonna use a
 * {@link PagerSnapHelper} to mimic a Pager-inside-RecyclerView. Other contents will be
 * normal text to preserve the performance and also to not make user confused.
 *
 * @author eneim (7/1/17).
 */

@SuppressWarnings("unused") public class NestedListFragment extends BaseFragment {

  public static NestedListFragment newInstance() {
    Bundle args = new Bundle();
    NestedListFragment fragment = new NestedListFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle bundle) {
    return inflater.inflate(R.layout.fragment_nested_list, container, false);
  }

  @BindView(R.id.toolbar_layout) CollapsingToolbarLayout toolbarLayout;
  @BindView(R.id.player_container) Container container;
  LinearLayoutManager layoutManager;
  NestedListAdapter adapter;

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    toolbarLayout.setTitle(getString(R.string.title_nested_list));

    layoutManager = new LinearLayoutManager(getContext());
    container.setLayoutManager(layoutManager);
    layoutManager.setItemPrefetchEnabled(true);
    adapter = new NestedListAdapter();
    container.setAdapter(adapter);
    container.setCacheManager(adapter);

    // FIXME Only use the following workaround when using this Fragment in ViewPager.
    if (viewPagerMode) {
      container.setPlayerSelector(null);
      // Using TabLayout has a downside: once we click to a tab to change page, there will be no animation,
      // which will cause our setup doesn't work well. We need a delay to make things work.
      handler.postDelayed(() -> {
        if (container != null) container.setPlayerSelector(selector);
      }, 500);
    } else {
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
    }, 500);
  }
}
