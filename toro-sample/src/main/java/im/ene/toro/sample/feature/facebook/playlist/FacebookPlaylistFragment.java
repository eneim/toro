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

package im.ene.toro.sample.feature.facebook.playlist;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.google.android.exoplayer2.C;
import im.ene.toro.PlaybackState;
import im.ene.toro.Toro;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroStrategy;
import im.ene.toro.extended.SnapToTopLinearLayoutManager;
import im.ene.toro.sample.R;
import im.ene.toro.sample.feature.facebook.timeline.TimelineItem;
import im.ene.toro.sample.util.Util;
import java.util.List;

/**
 * Created by eneim on 10/13/16.
 */

public class FacebookPlaylistFragment extends DialogFragment {

  private static final String ARGS_BASE_VIDEO = "playlist_base_video";
  private static final String ARGS_BASE_START_POSITION = "playlist_base_position";
  private static final String ARGS_BASE_START_DURATION = "playlist_base_duration";
  private static final String ARGS_BASE_VIDEO_ORDER = "playlist_base_order";

  public static FacebookPlaylistFragment newInstance(TimelineItem.VideoItem baseItem,
      long basePosition, long baseDuration, int baseOrder) {
    Log.i(TAG, "newInstance() called with: basePosition = ["
        + basePosition
        + "], baseOrder = ["
        + baseOrder
        + "]");
    FacebookPlaylistFragment fragment = new FacebookPlaylistFragment();
    if (baseItem != null) {
      Bundle args = new Bundle();
      args.putParcelable(ARGS_BASE_VIDEO, baseItem);
      args.putLong(ARGS_BASE_START_POSITION, basePosition);
      args.putLong(ARGS_BASE_START_DURATION, baseDuration);
      args.putInt(ARGS_BASE_VIDEO_ORDER, baseOrder);
      fragment.setArguments(args);
    }
    return fragment;
  }

  private TimelineItem.VideoItem baseItem;
  private long basePosition;
  private long baseDuration;
  // Used to cache the base video's adapter position. Will be used in onDetach
  private int baseOrder;

  @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    return new Dialog(getContext(), R.style.Toro_Theme_Playlist);
  }

  private ToroStrategy strategyToRestore;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    strategyToRestore = Toro.getStrategy();

    Toro.setStrategy(new ToroStrategy() {
      boolean isFirstPlayerDone = false;

      @Override public String getDescription() {
        return "First video plays first";
      }

      @Override public ToroPlayer findBestPlayer(List<ToroPlayer> candidates) {
        return strategyToRestore.findBestPlayer(candidates);
      }

      @Override public boolean allowsToPlay(ToroPlayer player, ViewParent parent) {
        boolean allowToPlay = (isFirstPlayerDone || player.getPlayOrder() == 0)  //
            && strategyToRestore.allowsToPlay(player, parent);

        // A work-around to keep track of first video on top.
        if (player.getPlayOrder() == 0) {
          isFirstPlayerDone = true;
        }
        return allowToPlay;
      }
    });

    if (getArguments() != null) {
      this.baseItem = getArguments().getParcelable(ARGS_BASE_VIDEO);
      this.basePosition = getArguments().getLong(ARGS_BASE_START_POSITION, C.POSITION_UNSET);
      this.baseDuration = getArguments().getLong(ARGS_BASE_START_DURATION, C.LENGTH_UNSET);
      this.baseOrder = getArguments().getInt(ARGS_BASE_VIDEO_ORDER);
    }
  }

  @Override public void onDestroy() {
    super.onDestroy();
    Toro.setStrategy(strategyToRestore);
  }

  @Bind(R.id.recycler_view) RecyclerView recyclerView;
  private MoreVideoRepo videoRepo;
  private MoreVideosAdapter adapter;

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
    adapter = new MoreVideosAdapter(baseItem);
    RecyclerView.LayoutManager layoutManager =
        new SnapToTopLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);

    // Maybe DI in real practice, not here.
    videoRepo = new MoreVideoRepo(baseItem);
    // Fake the API Request.
    videoRepo.getMoreVideos(new MoreVideoRepo.Callback() {
      @Override public void onMoreVideosLoaded(List<TimelineItem.VideoItem> items) {
        if (adapter != null) {
          adapter.addAll(items);
        }
      }
    });
  }

  @CallSuper @Override public void onStart() {
    super.onStart();
    if (Build.VERSION.SDK_INT >= 24) {
      dispatchFragmentActivated();
    }
  }

  @CallSuper @Override public void onStop() {
    if (Build.VERSION.SDK_INT >= 24) {
      dispatchFragmentDeActivated();
    }
    super.onStop();
  }

  @CallSuper @Override public void onResume() {
    super.onResume();
    if (Build.VERSION.SDK_INT < 24) {
      dispatchFragmentActivated();
    }
  }

  @CallSuper @Override public void onPause() {
    if (Build.VERSION.SDK_INT < 24) {
      dispatchFragmentDeActivated();
    }
    super.onPause();
  }

  protected void dispatchFragmentActivated() {
    Toro.register(recyclerView);
    adapter.saveVideoState(Util.genVideoId(baseItem.getVideoUrl(), 0), basePosition, baseDuration);
  }

  protected void dispatchFragmentDeActivated() {
    Toro.unregister(recyclerView);
  }

  // Dialog cycle handling

  private static final String TAG = "Toro:FB:PLS";

  private Callback callback;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof Callback) {
      this.callback = (Callback) context;
    }

    Log.w(TAG, "onAttach() called with: callback = [" + callback + "]");
    if (callback != null) {
      callback.onPlaylistAttached();
    }
  }

  @Override public void onDetach() {
    if (callback != null) {
      PlaybackState state = adapter.getSavedState(Util.genVideoId(baseItem.getVideoUrl(), 0));
      callback.onPlaylistDetached(this.baseItem,
          // Get saved position of first Item in this list, pass it to origin item at "baseOrder"
          state != null ? state.getPosition() : 0, baseOrder);
    }
    Log.w(TAG, "onDetach() called. Callback: " + callback + " , Host: " + getContext());
    super.onDetach();
  }

  @Override public void show(FragmentManager manager, String tag) {
    super.show(manager, tag);
    Log.d(TAG, "show() called with: manager = [" + manager + "], tag = [" + tag + "]");
  }

  @Override public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    Log.d(TAG, "onDismiss() called with: dialog = [" + dialog + "]");
  }

  @Override public void onCancel(DialogInterface dialog) {
    super.onCancel(dialog);
    Log.d(TAG, "onCancel() called with: dialog = [" + dialog + "]");
  }

  public interface Callback {

    /**
     * Used to tell the caller to unregister its RecyclerView. We will restore it after.
     */
    void onPlaylistAttached();

    /**
     * Pass the playback information back to timeline
     *
     * @param baseItem the Item which was passed here at first
     * @param position latest playback position in this playlist
     * @param order original order of the baseItem
     */
    void onPlaylistDetached(TimelineItem.VideoItem baseItem, Long position, int order);
  }
}
