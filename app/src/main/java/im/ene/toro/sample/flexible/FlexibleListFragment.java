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

package im.ene.toro.sample.flexible;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroPlayer;
import im.ene.toro.sample.R;
import im.ene.toro.sample.common.BaseFragment;
import im.ene.toro.widget.Container;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;
import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;
import static android.support.v7.widget.helper.ItemTouchHelper.RIGHT;
import static android.support.v7.widget.helper.ItemTouchHelper.UP;

/**
 * @author eneim (7/6/17).
 */

public class FlexibleListFragment extends BaseFragment {

  public static FlexibleListFragment newInstance() {
    Bundle args = new Bundle();
    FlexibleListFragment fragment = new FlexibleListFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle bundle) {
    return inflater.inflate(R.layout.fragment_flexible, container, false);
  }

  @BindView(R.id.toolbar_layout) CollapsingToolbarLayout toolbarLayout;
  @BindView(R.id.player_container) Container container;
  GridLayoutManager layoutManager;
  FlexibleListAdapter adapter;
  ItemTouchHelper itemTouchHelper;

  @Override public void onViewCreated(View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    toolbarLayout.setTitle(getString(R.string.title_flexible_grid));

    layoutManager = new GridLayoutManager(getContext(), 2);
    layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
      @Override public int getSpanSize(int position) {
        return position % 3 == 0 ? 2 : 1;
      }
    });
    container.setLayoutManager(layoutManager);
    adapter = new FlexibleListAdapter();
    container.setAdapter(adapter);
    container.setCacheManager(adapter);

    itemTouchHelper =
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(UP | DOWN | LEFT | RIGHT, 0) {
          @Override public boolean onMove(RecyclerView recyclerView, //
              RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return adapter.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
          }

          @Override public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

          }
        });
    itemTouchHelper.attachToRecyclerView(container);

    // Custom player selector for a complicated playback in grid
    activeSelector = new PlayerSelector() {
      @NonNull @Override public Collection<ToroPlayer> select(@NonNull Container container,
          @NonNull List<ToroPlayer> items) {
        List<ToroPlayer> toSelect;
        int count = items.size();
        if (count < 1) {
          toSelect = Collections.emptyList();
        } else {
          int firstOrder = items.get(0).getPlayerOrder();
          int span = layoutManager.getSpanSizeLookup().getSpanSize(firstOrder);
          count = Math.min(count, layoutManager.getSpanCount() / span);
          toSelect = new ArrayList<>();
          for (int i = 0; i < count; i++) {
            toSelect.add(items.get(i));
          }
        }

        return toSelect;
      }

      @NonNull @Override public PlayerSelector reverse() {
        return this;
      }
    };

    if (viewPagerMode) {
      if (getUserVisibleHint()) {
        selector = activeSelector;
      } else {
        selector = PlayerSelector.NONE;
      }
      container.setPlayerSelector(null);
      // Using TabLayout has a downside: once we click to a tab to change page, there will be no animation,
      // which will cause our setup doesn't work well. We need a delay to make things work.
      handler.postDelayed(() -> {
        //noinspection ConstantConditions
        if (container != null) container.setPlayerSelector(selector);
      }, 200);
    } else {
      container.setPlayerSelector(activeSelector);
    }
  }

  @Override public void onDestroyView() {
    handler.removeCallbacksAndMessages(null);
    itemTouchHelper.attachToRecyclerView(null);
    layoutManager = null;
    adapter = null;
    container.setPlayerSelector(null);
    activeSelector = null;
    selector = null;
    itemTouchHelper = null;
    super.onDestroyView();
  }

  PlayerSelector activeSelector = PlayerSelector.DEFAULT;
  PlayerSelector selector;
  final Handler handler = new Handler();  // post a delay due to the visibility change

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (!isVisibleToUser) {
      selector = PlayerSelector.NONE;
    } else {
      selector = activeSelector;
    }

    // Using TabLayout has a downside: once we click to a tab to change page, there will be no animation,
    // which will cause our setup doesn't work well. We need a delay to make things work.
    handler.postDelayed(() -> {
      //noinspection ConstantConditions
      if (container != null) container.setPlayerSelector(selector);
    }, 200);
  }
}
