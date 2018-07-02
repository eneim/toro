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

package im.ene.toro.sample.facebook.playlist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.State;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroPlayer;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.BuildConfig;
import im.ene.toro.sample.R;
import im.ene.toro.sample.facebook.core.BlackBoardDialogFragment;
import im.ene.toro.sample.facebook.core.ScreenHelper;
import im.ene.toro.sample.facebook.data.FbVideo;
import im.ene.toro.sample.facebook.player.BigPlayerFragment;
import im.ene.toro.widget.Container;
import io.reactivex.Observable;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author eneim | 6/19/17.
 */

public class MoreVideosFragment extends BlackBoardDialogFragment
    implements BigPlayerFragment.Callback {

  public interface Callback {

    void onPlaylistCreated();

    void onPlaylistDestroyed(int basePosition, FbVideo baseItem, PlaybackInfo latestInfo);
  }

  public static final String TAG = "Toro:Fb:MoreVideos";

  // Arguments to start this fragment
  private static final String ARG_EXTRA_PLAYBACK_INFO = "fb:more_videos:playback_info";
  private static final String ARG_EXTRA_BASE_FB_VIDEO = "fb:more_videos:base_video";
  private static final String ARG_EXTRA_BASE_ORDER = "fb:more_videos:base_order";

  // Save stuff for big player
  private static final String STATE_KEY_FB_VIDEO = "fb:timeline:list:state:video";
  private static final String STATE_KEY_ACTIVE_ORDER = "fb:timeline:list:state:order";
  private static final String STATE_KEY_PLAYBACK_STATE = "fb:timeline:list:state:playback_info";

  // Save state from "Big player"
  private static final String STATE_KEY_BIG_PLAYER_BUNDLE = "fb:timeline:list:state:player:bundle";

  // We use FbVideo as Parcelable, in real life, it should be retrieved from Database.
  public static MoreVideosFragment newInstance(int position, @NonNull FbVideo video,
      @NonNull PlaybackInfo info) {
    MoreVideosFragment fragment = new MoreVideosFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_EXTRA_BASE_ORDER, position);
    args.putParcelable(ARG_EXTRA_BASE_FB_VIDEO, video);
    args.putParcelable(ARG_EXTRA_PLAYBACK_INFO, info);
    fragment.setArguments(args);
    return fragment;
  }

  private FbVideo baseVideo;
  private PlaybackInfo baseInfo;
  private int baseOrder;

  private Callback callback;

  // Orientation helper stuff
  private WindowManager windowManager;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    if (getParentFragment() != null && getParentFragment() instanceof Callback) {
      this.callback = (Callback) getParentFragment();
    }
  }

  @Override public void onDetach() {
    super.onDetach();
    windowManager = null;
    callback = null;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      baseVideo = getArguments().getParcelable(ARG_EXTRA_BASE_FB_VIDEO);
      baseInfo = getArguments().getParcelable(ARG_EXTRA_PLAYBACK_INFO);
      baseOrder = getArguments().getInt(ARG_EXTRA_BASE_ORDER);
    }

    if (baseVideo == null || baseInfo == null) {
      throw new IllegalArgumentException(
          "Invalid arguments for MoreVideosFragment. Require non-null base Video and PlaybackInfo.");
    }
  }

  Unbinder unbinder;
  @BindView(R.id.recycler_view) Container container;
  RecyclerView.LayoutManager layoutManager;
  MoreVideosAdapter adapter;

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_facebook_morevideos, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    unbinder = ButterKnife.bind(this, view);

    if (callback != null) {
      callback.onPlaylistCreated();
    }

    layoutManager = new LinearLayoutManager(getContext()) {
      @Override public void smoothScrollToPosition(RecyclerView view, State state, int position) {
        LinearSmoothScroller linearSmoothScroller =
            new SnapTopLinearSmoothScroller(view.getContext());
        linearSmoothScroller.setTargetPosition(position);
        super.startSmoothScroll(linearSmoothScroller);
      }
    };

    container.setLayoutManager(layoutManager);
    adapter = new MoreVideosAdapter(baseVideo, baseVideo.timeStamp);
    container.setAdapter(adapter);
    container.setCacheManager(adapter);
    container.savePlaybackInfo(0, baseInfo);

    adapter.setOnCompleteCallback(new MoreVideosAdapter.OnCompleteCallback() {
      @SuppressLint("CheckResult") @Override void onCompleted(ToroPlayer player) {
        int position = adapter.findNextPlayerPosition(player.getPlayerOrder());
        //noinspection Convert2MethodRef,ResultOfMethodCallIgnored
        Observable.just(container)
            .delay(250, TimeUnit.MILLISECONDS)
            .filter(c -> c != null)
            .subscribe(rv -> rv.smoothScrollToPosition(position));
      }
    });

    // Backup active selector.
    selector = container.getPlayerSelector();
  }

  @Override public void onViewStateRestored(@Nullable Bundle bundle) {
    super.onViewStateRestored(bundle);
    if (bundle == null) {
      // User comes here from first place, don't need to do anything.
      return;
    }

    Bundle playerBundle = bundle.getBundle(STATE_KEY_BIG_PLAYER_BUNDLE);
    if (playerBundle != null) { // Not null = just back after a "Big player" dismissed.
      int order = playerBundle.getInt(BigPlayerFragment.BUNDLE_KEY_ORDER);
      PlaybackInfo info = playerBundle.getParcelable(BigPlayerFragment.BUNDLE_KEY_INFO);
      if (info == null) info = new PlaybackInfo();
      this.container.savePlaybackInfo(order, info);
    }
    // Bundle != null, which is a hint that we come here from a config change, maybe orientation.
    if (ScreenHelper.shouldUseBigPlayer(windowManager.getDefaultDisplay())) {
      // Device in landscape mode. Here we use PlayerSelector.NONE to disable the current playback.
      container.setPlayerSelector(PlayerSelector.NONE);
      // Since we come here from a orientation change, if previous state (portrait mode),
      // there was an on-playing Player, there would have a saved state of that playback.
      // Let's retrieve it and then do stuff.

      // 1. Obtain the saved Video object and its order.
      FbVideo video = bundle.getParcelable(STATE_KEY_FB_VIDEO); // !can be null.
      int order = bundle.getInt(STATE_KEY_ACTIVE_ORDER);
      if (video != null) {
        // 2. Get saved playback info. We know the adapter is also a state manager though.
        PlaybackInfo info = bundle.getParcelable(STATE_KEY_PLAYBACK_STATE);
        // 3. Prepare video Uri, open a full screen playback dialog on top of this one.
        BigPlayerFragment playerFragment = BigPlayerFragment.newInstance(order, video, info);
        playerFragment.show(getChildFragmentManager(), BigPlayerFragment.FRAGMENT_TAG);
      }
    }
  }

  // Memo: This method is called before child Fragment's onSaveInstanceState.
  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    // If there is DialogFragment showing, we save stuff from it.
    Fragment playerFragment =
        getChildFragmentManager().findFragmentByTag(BigPlayerFragment.FRAGMENT_TAG);
    if (playerFragment != null && playerFragment instanceof BigPlayerFragment) {
      Bundle playerBundle = ((BigPlayerFragment) playerFragment).getCurrentState();
      outState.putBundle(STATE_KEY_BIG_PLAYER_BUNDLE, playerBundle);
    }

    // Save this fragment's stuff here.
    List<ToroPlayer> activePlayers = container.filterBy(Container.Filter.PLAYING);
    if (activePlayers.isEmpty()) return;
    ToroPlayer firstPlayer = activePlayers.get(0);  // get the first one only.
    // We will store the Media object, playback state.
    FbVideo item = adapter.getItem(firstPlayer.getPlayerOrder());
    if (item == null) {
      if (BuildConfig.DEBUG) {  // debug only.
        throw new IllegalStateException("Video is null for active Player: " + firstPlayer);
      }
    }

    outState.putInt(STATE_KEY_ACTIVE_ORDER, firstPlayer.getPlayerOrder());
    outState.putParcelable(STATE_KEY_FB_VIDEO, item);
    outState.putParcelable(STATE_KEY_PLAYBACK_STATE, firstPlayer.getCurrentPlaybackInfo());
  }

  @Override public void onDestroyView() {
    if (callback != null && adapter != null) {
      callback.onPlaylistDestroyed(baseOrder, baseVideo, container.getPlaybackInfo(0));
    }
    unbinder.unbind();
    adapter = null;
    layoutManager = null;
    selector = null;
    super.onDestroyView();
  }

  // Implement BigPlayerFragment callback

  PlayerSelector selector;  // backup current selector.

  @Override public void onBigPlayerCreated() {
    container.setPlayerSelector(PlayerSelector.NONE);
  }

  @Override
  public void onBigPlayerDestroyed(int videoOrder, FbVideo baseItem, PlaybackInfo latestInfo) {
    if (latestInfo != null) container.savePlaybackInfo(videoOrder, latestInfo);
    container.setPlayerSelector(selector);
  }
}
