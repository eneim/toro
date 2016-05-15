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

package im.ene.lab.toro.sample.facebook;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import im.ene.lab.toro.Toro;
import im.ene.lab.toro.VideoPlayerManager;
import im.ene.lab.toro.sample.data.SimpleVideoObject;
import im.ene.lab.toro.sample.fragment.RecyclerViewFragment;

/**
 * Created by eneim on 5/12/16.
 *
 * This is the normal Facebook feed list, which is different to 'Video playlist' which contains
 * only Video.
 */
public class FbFeedFragment extends RecyclerViewFragment {

  public static final String TAG = "FbVideoListFragment";

  public static FbFeedFragment newInstance() {
    return new FbFeedFragment();
  }

  @NonNull @Override protected RecyclerView.LayoutManager getLayoutManager() {
    return new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
  }

  @NonNull @Override protected RecyclerView.Adapter getAdapter() {
    return new FbFeedAdapter();
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
      int initPosition = 0;
      int initDuration = 0;
      if (adapter instanceof FbFeedAdapter) {
        initItem = (SimpleVideoObject) ((FbFeedAdapter) adapter).getItem(adapterPosition);
        initPosition = ((FbFeedAdapter) adapter).getPlayer().getCurrentPosition();
        initDuration = ((FbFeedAdapter) adapter).getPlayer().getDuration();
      }

      if (initItem != null) {
        FbPLayerDialogFragment player =
            FbPLayerDialogFragment.newInstance(initItem, initPosition, initDuration);
        player.setTargetFragment(FbFeedFragment.this, RESUME_REQUEST_CODE);
        player.show(getChildFragmentManager(), FbPLayerDialogFragment.TAG);
      }
    }
  };

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RESUME_REQUEST_CODE) {
      Toro.rest(true);
      VideoPlayerManager manager = ((VideoPlayerManager) mAdapter);
      int latestPosition = data.getIntExtra(FbPLayerDialogFragment.ARGS_LATEST_TIMESTAMP, 0);
      manager.saveVideoState(manager.getPlayer().getVideoId(), latestPosition,
          manager.getPlayer().getDuration());
      Toro.rest(false);
    }
  }
}
