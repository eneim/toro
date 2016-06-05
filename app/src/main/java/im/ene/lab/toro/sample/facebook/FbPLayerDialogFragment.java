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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import butterknife.Bind;
import butterknife.ButterKnife;
import im.ene.lab.toro.Toro;
import im.ene.lab.toro.ToroPlayer;
import im.ene.lab.toro.ToroStrategy;
import im.ene.lab.toro.VideoPlayerManager;
import im.ene.lab.toro.VideoPlayerManagerImpl;
import im.ene.lab.toro.sample.BuildConfig;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.adapter.SimpleVideoListAdapter;
import im.ene.lab.toro.sample.data.SimpleVideoObject;
import im.ene.lab.toro.sample.widget.DividerItemDecoration;
import im.ene.lab.toro.sample.widget.LargeDialogFragment;
import java.util.List;

/**
 * Created by eneim on 5/13/16.
 */
public class FbPLayerDialogFragment extends LargeDialogFragment {

  public static final String TAG = "FbPLayer";

  public static final String ARGS_INIT_VIDEO = "fb_player_init_video";
  public static final String ARGS_INIT_POSITION = "fb_player_init_position";
  public static final String ARGS_INIT_DURATION = "fb_player_init_duration";
  public static final String ARGS_LATEST_TIMESTAMP = "player_latest_timestamp";

  public static FbPLayerDialogFragment newInstance(SimpleVideoObject initItem, long initPos,
      long initDuration) {
    FbPLayerDialogFragment fragment = new FbPLayerDialogFragment();
    Bundle args = new Bundle();
    args.putParcelable(ARGS_INIT_VIDEO, initItem);
    args.putLong(ARGS_INIT_POSITION, initPos);
    args.putLong(ARGS_INIT_DURATION, initDuration);
    fragment.setArguments(args);
    return fragment;
  }

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
        boolean allowToPlay = (isFirstPlayerDone || player.getPlayOrder() == 0)  //
            && oldStrategy.allowsToPlay(player, parent);

        // A work-around to keep track of first video on top.
        if (player.getPlayOrder() == 0) {
          isFirstPlayerDone = true;
        }
        return allowToPlay;
      }
    });
  }

  @Bind(R.id.recycler_view) RecyclerView recyclerView;
  private SimpleVideoObject initItem;
  private long initPosition;
  private long initDuration;
  private Adapter adapter;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      initItem = getArguments().getParcelable(ARGS_INIT_VIDEO);
      initPosition = getArguments().getLong(ARGS_INIT_POSITION);
      initDuration = getArguments().getLong(ARGS_INIT_DURATION);
    }

    if (initItem == null) {
      if (BuildConfig.DEBUG) {
        throw new IllegalStateException("Unexpected state");
      }
      getActivity().finish();
    }
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.generic_recycler_view, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    RecyclerView.LayoutManager layoutManager =
        new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
        ((LinearLayoutManager) layoutManager).getOrientation()));

    adapter = new Adapter(initItem);
    recyclerView.setHasFixedSize(false);
    recyclerView.setAdapter(adapter);
    recyclerView.smoothScrollToPosition(0);
  }

  @Override public void onResume() {
    super.onResume();
    Toro.register(recyclerView);
    adapter.saveVideoState(initItem.toString() + "-0", initPosition, initDuration);
  }

  @Override public void onPause() {
    super.onPause();
    Toro.unregister(recyclerView);
  }

  @Override public void onDismiss(DialogInterface dialog) {
    Long latestPosition = adapter.getSavedPosition(initItem.toString() + "-0"); // first item
    if (getTargetFragment() != null && latestPosition != null) {
      Intent result = new Intent();
      result.putExtra(ARGS_LATEST_TIMESTAMP, latestPosition);
      getTargetFragment().onActivityResult(FbFeedFragment.RESUME_REQUEST_CODE, Activity.RESULT_OK,
          result);
    }
    super.onDismiss(dialog);
  }

  private static class Adapter extends SimpleVideoListAdapter implements VideoPlayerManager {

    private final SimpleVideoObject initItem;
    private final VideoPlayerManager delegate;

    public Adapter(SimpleVideoObject initItem) {
      this.initItem = initItem;
      this.delegate = new VideoPlayerManagerImpl();
    }

    @Nullable @Override protected Object getItem(int position) {
      if (position == 0) {
        return initItem;
      }

      return super.getItem(position - 1);
    }

    @Override public int getItemCount() {
      return super.getItemCount() + 1;
    }

    @Override public ToroPlayer getPlayer() {
      return delegate.getPlayer();
    }

    @Override public void setPlayer(ToroPlayer player) {
      delegate.setPlayer(player);
    }

    @Override public void onRegistered() {
      delegate.onRegistered();
    }

    @Override public void onUnregistered() {
      delegate.onUnregistered();
    }

    @Override public void startPlayback() {
      delegate.startPlayback();
    }

    @Override public void startPlayback(long position) {
      delegate.startPlayback(position);
    }

    @Override public void pausePlayback() {
      delegate.pausePlayback();
    }

    @Override public void stopPlayback() {
      delegate.stopPlayback();
    }

    @Override
    public void saveVideoState(String videoId, @Nullable Long position, long duration) {
      delegate.saveVideoState(videoId, position, duration);
    }

    @Override public void restoreVideoState(String videoId) {
      delegate.restoreVideoState(videoId);
    }

    @Nullable @Override public Long getSavedPosition(String videoId) {
      if (getPlayer() instanceof TrackablePlayer && videoId.equals(getPlayer().getVideoId())) {
        return ((TrackablePlayer) getPlayer()).getLatestPosition();
      }
      return delegate.getSavedPosition(videoId);
    }
  }
}
