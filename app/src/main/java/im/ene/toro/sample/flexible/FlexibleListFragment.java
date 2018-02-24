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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroPlayer;
import im.ene.toro.annotations.Sorted;
import im.ene.toro.media.PlaybackInfo;
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
import static im.ene.toro.sample.SinglePlayerActivity.createIntent;

/**
 * @author eneim (7/6/17).
 */

public class FlexibleListFragment extends BaseFragment {

  static final int RQ_PLAYBACK_INFO = 100;

  public static FlexibleListFragment newInstance() {
    Bundle args = new Bundle();
    FlexibleListFragment fragment = new FlexibleListFragment();
    fragment.setArguments(args);
    return fragment;
  }

  static final int[] contents =
      { R.string.license_tos, R.string.license_bbb, R.string.license_cosmos };

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
    adapter = new FlexibleListAdapter(new FlexibleListAdapter.ItemClickListener() {
      @Override void onItemClick(View view, int position, Content.Media media, PlaybackInfo info) {
        String content = getString(contents[position % contents.length]);
        Point viewSize = new Point(view.getWidth(), view.getHeight());
        Point videoSize = new Point(view.getWidth(), view.getHeight());
        if (view instanceof PlayerView && ((PlayerView) view).getPlayer() != null) {
          Player player = ((PlayerView) view).getPlayer();
          Format videoFormat =  //
              player instanceof SimpleExoPlayer ? ((SimpleExoPlayer) player).getVideoFormat()
                  : null;
          if (videoFormat != null
              && videoFormat.width != Format.NO_VALUE
              && videoFormat.height != Format.NO_VALUE) {
            videoSize.set(videoFormat.width, videoFormat.height);
          }
        }

        Intent intent = createIntent(getContext(), position, media.mediaUri,  //
            content, info, viewSize, videoSize, true);
        ActivityOptionsCompat options = ActivityOptionsCompat.
            makeSceneTransitionAnimation(getActivity(), view, ViewCompat.getTransitionName(view));
        startActivityForResult(intent, RQ_PLAYBACK_INFO, options.toBundle());
      }
    });
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
          @Sorted(order = Sorted.Order.ASCENDING) @NonNull List<ToroPlayer> items) {
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
      }, 500);
    } else {
      selector = activeSelector;
      container.setPlayerSelector(selector);
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

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK && requestCode == RQ_PLAYBACK_INFO && data != null) {
      PlaybackInfo info = data.getParcelableExtra(RESULT_EXTRA_PLAYBACK_INFO);
      int order = data.getIntExtra(RESULT_EXTRA_PLAYER_ORDER, -1);
      if (order >= 0 && container != null) {
        container.setPlayerSelector(PlayerSelector.NONE);
        container.savePlaybackInfo(order, info);
        container.setPlayerSelector(selector);
      }
    }
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
    }, 500);
  }
}
