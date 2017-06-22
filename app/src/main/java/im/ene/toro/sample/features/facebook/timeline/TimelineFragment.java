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

package im.ene.toro.sample.features.facebook.timeline;

import android.content.Context;
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
import android.view.WindowManager;
import butterknife.BindView;
import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroPlayer;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.BuildConfig;
import im.ene.toro.sample.R;
import im.ene.toro.sample.common.BaseFragment;
import im.ene.toro.sample.features.facebook.core.ScreenHelper;
import im.ene.toro.sample.features.facebook.data.FbItem;
import im.ene.toro.sample.features.facebook.data.FbVideo;
import im.ene.toro.sample.features.facebook.player.BigPlayerFragment;
import im.ene.toro.sample.features.facebook.playlist.MoreVideosFragment;
import im.ene.toro.widget.Container;
import java.util.List;

/**
 * @author eneim | 6/18/17.
 */

public class TimelineFragment extends BaseFragment
    implements MoreVideosFragment.Callback, BigPlayerFragment.Callback {

  private static final String STATE_KEY_FB_VIDEO = "fb:timeline:state:video";
  private static final String STATE_KEY_ACTIVE_ORDER = "fb:timeline:state:order";
  private static final String STATE_KEY_PLAYBACK_STATE = "fb:timeline:state:playback_info";

  private static final String STATE_KEY_BIG_PLAYER_BUNDLE = "fb:timeline:state:player:bundle";

  @SuppressWarnings("unused") public static TimelineFragment newInstance() {
    Bundle args = new Bundle();
    TimelineFragment fragment = new TimelineFragment();
    fragment.setArguments(args);
    return fragment;
  }

  // View setup
  @BindView(R.id.recycler_view) Container container;
  TimelineAdapter adapter;
  RecyclerView.LayoutManager layoutManager;
  TimelineAdapter.Callback adapterCallback;

  // Orientation helper stuff
  private WindowManager windowManager;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    // !IMPORTANT: don't remove these lines.
    this.TAG = "Toro:Fb:Timeline";
    Log.wtf(TAG, "onAttach() called with: context = [" + context + "]");
    windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle bundle) {
    return inflater.inflate(R.layout.layout_container_facebook, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    adapter = new TimelineAdapter(System.currentTimeMillis());
    layoutManager = new LinearLayoutManager(getContext());
    container.setAdapter(adapter);
    container.setLayoutManager(layoutManager);
    container.setPlayerStateManager(adapter);
    adapterCallback = new TimelineAdapter.Callback() {
      @Override void onItemClick(@NonNull TimelineViewHolder viewHolder, @NonNull View view,
          @NonNull FbItem item, int position) {
        if (viewHolder instanceof ToroPlayer && item instanceof FbVideo) {
          PlaybackInfo info = ((ToroPlayer) viewHolder).getCurrentPlaybackInfo();
          MoreVideosFragment moreVideos =
              MoreVideosFragment.newInstance(position, (FbVideo) item, info);
          moreVideos.show(getChildFragmentManager(), MoreVideosFragment.TAG);
        }
      }
    };
    adapter.setCallback(adapterCallback);
    selector = container.getPlayerSelector(); // save for later use.
  }

  @Override public void onViewStateRestored(@Nullable Bundle bundle) {
    super.onViewStateRestored(bundle);
    if (bundle == null) {
      // User come here from first place, we keep current behaviour.
      return;
    }
    // Bundle != null, which is a hint that we come here from a orientation change.
    // There is a chance that this Fragment is recreated after a config change. In that case,
    // if in previous 'life', there was a BigPlayerFragment created, it will store a bundle
    // containing its latest playback state that was provided by this Fragment.
    // Here we restore it to continue the playback.
    Bundle playerBundle = bundle.getBundle(STATE_KEY_BIG_PLAYER_BUNDLE);
    if (playerBundle != null) {
      int order = playerBundle.getInt(BigPlayerFragment.BUNDLE_KEY_ORDER);
      PlaybackInfo info = playerBundle.getParcelable(BigPlayerFragment.BUNDLE_KEY_INFO);
      if (info == null) info = new PlaybackInfo();
      this.adapter.savePlaybackInfo(order, info);
    }
    if (ScreenHelper.shouldUseBigPlayer(windowManager.getDefaultDisplay())) {
      // Since we come here from a orientation change, if previous state (portrait mode),
      // there was a on-playing Player, we should have a saved state of latest playback.
      // Let's retrieve it and then do stuff.

      // 1. Obtain the Video object and its order.
      FbVideo video = bundle.getParcelable(STATE_KEY_FB_VIDEO); // can be null.
      if (video != null) {
        // Device in landscape mode, and there is on-going Video saved from previous state.
        // Here we use PlayerSelector.NONE to disable the auto playback and bring the big player in.
        container.setPlayerSelector(PlayerSelector.NONE);
        int order = bundle.getInt(STATE_KEY_ACTIVE_ORDER);
        // 2. Get saved playback info. We know the adapter is also a state manager though.
        PlaybackInfo info = bundle.getParcelable(STATE_KEY_PLAYBACK_STATE);
        // 3. Prepare video Uri, open a full screen playback dialog.
        BigPlayerFragment playerFragment = BigPlayerFragment.newInstance(order, video, info);
        playerFragment.show(getChildFragmentManager(), BigPlayerFragment.FRAGMENT_TAG);
      }
    }
  }

  // Memo: This method is called before child Fragment's onSaveInstanceState.
  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    // If there is DialogFragment showing, we save stuff from it here.
    Fragment playerFragment =
        getChildFragmentManager().findFragmentByTag(BigPlayerFragment.FRAGMENT_TAG);
    if (playerFragment != null && playerFragment instanceof BigPlayerFragment) {
      Bundle playerBundle = ((BigPlayerFragment) playerFragment).getCurrentState();
      outState.putBundle(STATE_KEY_BIG_PLAYER_BUNDLE, playerBundle);
    }

    // Save stuff here.
    List<ToroPlayer> activePlayers = container.getActivePlayers();
    if (activePlayers.isEmpty()) return;
    ToroPlayer firstPlayer = activePlayers.get(0);  // get the first one only.
    // We will store the Media object, playback state.
    FbItem item = adapter.getItem(firstPlayer.getPlayerOrder());
    if (item == null) {
      if (BuildConfig.DEBUG) {  // debug only.
        throw new IllegalStateException("Video is null for active Player: " + firstPlayer);
      }
    }

    // Save this and restore again to open on a BigPlayer if need.
    if (item instanceof FbVideo) {
      outState.putInt(STATE_KEY_ACTIVE_ORDER, firstPlayer.getPlayerOrder());
      outState.putParcelable(STATE_KEY_FB_VIDEO, (FbVideo) item);
      outState.putParcelable(STATE_KEY_PLAYBACK_STATE, firstPlayer.getCurrentPlaybackInfo());
    } else {
      // Real practice should not face the following issue, only for debugging.
      if (BuildConfig.DEBUG) {
        throw new IllegalStateException("Found wrong type of FbItem for ToroPlayer: " + item);
      }
    }
  }

  @Override public void onDestroyView() {
    adapter.setCallback(null);
    adapterCallback = null;
    adapter = null;
    layoutManager = null;
    selector = null;
    super.onDestroyView();
  }

  // Implement MoreVideosFragment callback

  PlayerSelector selector;  // backup current selector.

  @Override public void onPlaylistCreated() {
    container.setPlayerSelector(PlayerSelector.NONE);
  }

  @Override
  public void onPlaylistDestroyed(int basePosition, FbVideo baseItem, PlaybackInfo latestInfo) {
    adapter.savePlaybackInfo(basePosition, latestInfo);
    container.setPlayerSelector(selector);
  }

  // Implement BigPlayerFragment callback

  @Override public void onBigPlayerCreated() {
    container.setPlayerSelector(PlayerSelector.NONE);
  }

  @Override
  public void onBigPlayerDestroyed(int videoOrder, FbVideo baseItem, PlaybackInfo latestInfo) {
    adapter.savePlaybackInfo(videoOrder, latestInfo);
    container.setPlayerSelector(selector);
  }
}
