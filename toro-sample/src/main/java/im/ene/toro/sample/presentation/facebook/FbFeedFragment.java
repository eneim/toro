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

package im.ene.toro.sample.presentation.facebook;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import im.ene.toro.Toro;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroStrategy;
import im.ene.toro.VideoPlayerManager;
import im.ene.toro.sample.R;
import im.ene.toro.sample.data.SimpleVideoObject;
import im.ene.toro.sample.widget.DividerItemDecoration;
import java.util.List;

/**
 * Created by eneim on 5/12/16.
 *
 * This is the normal Facebook feed list, which is different to 'Video playlist' which contains
 * only Video.
 */
public class FbFeedFragment extends Fragment {

  public static final String TAG = "Toro:Facebook";

  public static FbFeedFragment newInstance() {
    return new FbFeedFragment();
  }

  @NonNull protected RecyclerView.LayoutManager getLayoutManager() {
    return new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
  }

  @NonNull protected RecyclerView.Adapter getAdapter() {
    return new FbFeedAdapter();
  }

  protected RecyclerView mRecyclerView;
  protected RecyclerView.Adapter mAdapter;

  private int firstVideoPosition;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    final ToroStrategy oldStrategy = Toro.getStrategy();

    Toro.setStrategy(new ToroStrategy() {
      boolean isFirstPlayerDone = false;

      @Override public String getDescription() {
        return "First video plays first";
      }

      @Override public ToroPlayer findBestPlayer(List<ToroPlayer> candidates) {
        return oldStrategy.findBestPlayer(candidates);
      }

      @Override public boolean allowsToPlay(ToroPlayer player, ViewParent parent) {
        boolean allowToPlay = (isFirstPlayerDone || player.getPlayOrder() == firstVideoPosition)  //
            && oldStrategy.allowsToPlay(player, parent);

        // A work-around to keep track of first video on top.
        if (player.getPlayOrder() == firstVideoPosition) {
          isFirstPlayerDone = true;
        }
        return allowToPlay;
      }
    });
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.generic_recycler_view, container, false);
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2) @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
    RecyclerView.LayoutManager layoutManager = getLayoutManager();
    mRecyclerView.setLayoutManager(layoutManager);
    if (layoutManager instanceof LinearLayoutManager) {
      mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
          ((LinearLayoutManager) layoutManager).getOrientation()));
    }

    mAdapter = getAdapter();
    mRecyclerView.setHasFixedSize(false);
    mRecyclerView.setAdapter(mAdapter);

    if (mAdapter instanceof OrderedPlayList) {
      firstVideoPosition = ((OrderedPlayList) mAdapter).firstVideoPosition();
    }
  }

  @Override public void onResume() {
    super.onResume();
    Toro.register(mRecyclerView);
  }

  @Override public void onPause() {
    super.onPause();
    Toro.unregister(mRecyclerView);
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    if (mAdapter instanceof FbFeedAdapter) {
      ((FbFeedAdapter) mAdapter).setOnItemClickListener(listener);
    }
  }

  public static final int RESUME_REQUEST_CODE = 1024;

  private OnItemClickListener listener = new OnItemClickListener() {
    @Override
    public void onItemClick(RecyclerView.Adapter adapter, RecyclerView.ViewHolder viewHolder,
        View view, int adapterPosition, long itemId) {
      SimpleVideoObject initItem = null;
      long initPosition = 0;
      long initDuration = 0;
      final ToroPlayer player;
      if (adapter instanceof FbFeedAdapter) {
        player = ((FbFeedAdapter) adapter).getPlayer();
        initItem = (SimpleVideoObject) ((FbFeedAdapter) adapter).getItem(adapterPosition);
        if (player != null) {
          initPosition = player.getCurrentPosition();
          initDuration = player.getDuration();
        }
      }

      if (initItem != null) {
        Bundle extras = new Bundle();
        extras.putLong(FbPLayerDialogFragment.ARGS_INIT_DURATION, initDuration);
        extras.putLong(FbPLayerDialogFragment.ARGS_INIT_POSITION, initPosition);
        extras.putParcelable(FbPLayerDialogFragment.ARGS_INIT_VIDEO, initItem);

        FbPLayerDialogFragment playlist =
            FbPLayerDialogFragment.newInstance(initItem, initPosition, initDuration);
        playlist.setTargetFragment(FbFeedFragment.this, RESUME_REQUEST_CODE);
        playlist.show(getChildFragmentManager(), FbPLayerDialogFragment.TAG);
      }
    }
  };

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RESUME_REQUEST_CODE) {
      Toro.rest(true);
      VideoPlayerManager manager = ((VideoPlayerManager) mAdapter);
      if (manager.getPlayer() != null) {
        long latestPosition = data.getLongExtra(FbPLayerDialogFragment.ARGS_LATEST_TIMESTAMP, 0);
        manager.saveVideoState(manager.getPlayer().getMediaId(), latestPosition,
            manager.getPlayer().getDuration());
      }
      Toro.rest(false);
    }
  }
}
