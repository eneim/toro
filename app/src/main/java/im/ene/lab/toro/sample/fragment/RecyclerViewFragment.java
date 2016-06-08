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

package im.ene.lab.toro.sample.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import im.ene.lab.toro.Toro;
import im.ene.lab.toro.ToroPlayer;
import im.ene.lab.toro.ToroStrategy;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.adapter.OrderedVideoList;
import im.ene.lab.toro.sample.widget.DividerItemDecoration;
import java.util.List;

/**
 * Created by eneim on 2/1/16.
 */
public abstract class RecyclerViewFragment extends Fragment {

  protected RecyclerView mRecyclerView;
  protected RecyclerView.Adapter mAdapter;

  private int firstVideoPosition;

  private static final String TAG = "ToroRV";

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
    mRecyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override public void onFocusChange(View v, boolean hasFocus) {
        Log.d(TAG, "onFocusChange() called with: " + "v = [" + RecyclerViewFragment.this.getClass()
            .getSimpleName() + "], hasFocus = [" + hasFocus + "]");
      }
    });

    if (mAdapter instanceof OrderedVideoList) {
      firstVideoPosition = ((OrderedVideoList) mAdapter).firstVideoPosition();
    }
  }

  @Override public void onResume() {
    super.onResume();
    // Log.i(TAG, "onResume: " + getClass().getSimpleName() + " | requestFocus()");
    // mRecyclerView.requestFocus();
    Toro.register(mRecyclerView);
  }

  @Override public void onPause() {
    super.onPause();
    Toro.unregister(mRecyclerView);
  }

  @NonNull protected abstract RecyclerView.LayoutManager getLayoutManager();

  @NonNull protected abstract RecyclerView.Adapter getAdapter();
}
