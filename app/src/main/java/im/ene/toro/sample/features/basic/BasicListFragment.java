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

package im.ene.toro.sample.features.basic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.SimpleCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import im.ene.toro.sample.R;
import im.ene.toro.sample.common.BaseFragment;
import im.ene.toro.sample.common.ContentAdapter;
import im.ene.toro.sample.common.EndlessScroller;
import im.ene.toro.sample.data.DataSource;
import im.ene.toro.widget.Container;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;
import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;
import static android.support.v7.widget.helper.ItemTouchHelper.RIGHT;
import static android.support.v7.widget.helper.ItemTouchHelper.UP;

/**
 * @author eneim | 6/6/17.
 */

public class BasicListFragment extends BaseFragment {

  @SuppressWarnings("unused") public static BasicListFragment newInstance() {
    Bundle args = new Bundle();
    BasicListFragment fragment = new BasicListFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout refreshLayout;
  @BindView(R.id.recycler_view) Container container;
  ContentAdapter adapter;
  RecyclerView.LayoutManager layoutManager;
  ItemTouchHelper touchHelper;
  RecyclerView.OnScrollListener infiniteScrollListener;

  @SuppressWarnings("SpellCheckingInspection")  //
  final CompositeDisposable disposibles = new CompositeDisposable();

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle bundle) {
    return inflater.inflate(R.layout.layout_container_basic, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    adapter = new ContentAdapter();
    layoutManager =
        // new GridLayoutManager(getContext(), getResources().getInteger(R.integer.grid_span));
        new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
    // layoutManager.setRecycleChildrenOnDetach(true);
    adapter.addMany(true, DataSource.getInstance().getEntities());

    container.setAdapter(adapter);
    container.setLayoutManager(layoutManager);
    container.setMaxPlayerNumber(2);  // optional, 1 is by default.

    refreshLayout.setOnRefreshListener(() -> {
      refreshLayout.setRefreshing(true);
      dispatchLoadData(false);
    });

    infiniteScrollListener = new EndlessScroller(layoutManager, DataSource.getInstance()) {
      @Override public void onLoadMore() {
        if (refreshLayout != null) {
          refreshLayout.setRefreshing(true);
        }
        dispatchLoadData(true);
      }
    };
    container.addOnScrollListener(infiniteScrollListener);

    touchHelper = new ItemTouchHelper(new SimpleCallback(UP | DOWN | LEFT | RIGHT, 0) {
      @Override
      public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder target) {
        return adapter.onItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
      }

      @Override public void onSwiped(ViewHolder viewHolder, int direction) {
        // do nothing.
      }
    });
    touchHelper.attachToRecyclerView(container);
  }

  @Override public void onViewStateRestored(@Nullable Bundle bundle) {
    super.onViewStateRestored(bundle);
    // Only fetch for completely new data if this Fragment is created from scratch.
    if (bundle == null) dispatchLoadData(false);
  }

  void dispatchLoadData(boolean loadMore) {
    DataSource.getInstance()
        .getFromCloud(loadMore, 50)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnComplete(() -> {
          if (refreshLayout != null && refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(false);
          }
        })
        .doOnSubscribe(disposibles::add)
        .subscribe(entities -> adapter.addMany(!loadMore, entities));
  }

  @Override public void onDestroyView() {
    disposibles.clear();  // Clear but not dispose, by intent
    touchHelper.attachToRecyclerView(null);
    // In case of LinearLayoutManager, setting it to "recycler child on detach" will also detach
    // children Views properly, so this setup is optional.
    // See: https://github.com/airbnb/epoxy/wiki/Avoiding-Memory-Leaks for more information.
    // Otherwise (using StaggeredGridLayoutManager or custom LayoutManager), it is recommended to have this setup to clear View cache.
    container.setAdapter(null);
    // BaseFragment (super) class will unbind all views here, so this super call must be called last.
    super.onDestroyView();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    // clean up
    touchHelper = null;
    adapter = null;
    layoutManager = null;
  }
}
