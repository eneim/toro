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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import im.ene.lab.toro.ToroPlayer;
import im.ene.lab.toro.VideoPlayerManager;
import im.ene.lab.toro.VideoPlayerManagerImpl;
import im.ene.lab.toro.sample.adapter.SimpleVideoListAdapter;
import im.ene.lab.toro.sample.fragment.RecyclerViewFragment;

/**
 * Created by eneim on 5/12/16.
 *
 * This is the normal Facebook feed list, which is different to 'Video playlist' which contains
 * only
 * Video.
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

  private class Adapter extends SimpleVideoListAdapter implements VideoPlayerManager {

    private final VideoPlayerManager mDelegate;

    public Adapter() {
      mDelegate = new VideoPlayerManagerImpl();
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      final ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
      // overwriting the setting
      viewHolder.setOnItemClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          FbPLayerDialogFragment player = FbPLayerDialogFragment.newInstance();
          player.show(getChildFragmentManager(), FbPLayerDialogFragment.TAG);
        }
      });

      return viewHolder;
    }

    @Override public ToroPlayer getPlayer() {
      return mDelegate.getPlayer();
    }

    @Override public void setPlayer(ToroPlayer player) {
      mDelegate.setPlayer(player);
    }

    @Override public void onRegistered() {
      mDelegate.onRegistered();
    }

    @Override public void onUnregistered() {
      mDelegate.onUnregistered();
    }

    @Override public void startPlayback() {
      mDelegate.startPlayback();
    }

    @Override public void pausePlayback() {
      mDelegate.pausePlayback();
    }

    @Override
    public void saveVideoState(String videoId, @Nullable Integer position, long duration) {
      mDelegate.saveVideoState(videoId, position, duration);
    }

    @Override public void restoreVideoState(String videoId) {
      mDelegate.restoreVideoState(videoId);
    }
  }
}
