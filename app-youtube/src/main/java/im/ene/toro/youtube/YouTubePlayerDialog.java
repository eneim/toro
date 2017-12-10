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

package im.ene.toro.youtube;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.FragmentLifecycleCallbacks;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.youtube.common.BlackBoardDialogFragment;

/**
 * @author eneim (2017/12/08).
 */

public class YouTubePlayerDialog extends BlackBoardDialogFragment {

  public static final String TAG = "YouT:BigPlayer";

  static final String ARGS_ADAPTER_ORDER = "youtube:dialog:adapter_order";
  static final String ARGS_VIDEO_ID = "youtube:dialog:video_id";
  static final String ARGS_PLAYBACK_INFO = "youtube:dialog:playback_info";
  static final String ARGS_ORIENTATION = "youtube:dialog:orientation";

  static class InitData {

    final int adapterOrder;
    final String videoId;
    final PlaybackInfo playbackInfo;
    final int orientation;  // Original Orientation of requested Activity

    InitData(int adapterOrder, @NonNull String videoId, @NonNull PlaybackInfo playbackInfo,
        int orientation) {
      this.adapterOrder = adapterOrder;
      this.videoId = videoId;
      this.playbackInfo = playbackInfo;
      this.orientation = orientation;
    }
  }

  public static YouTubePlayerDialog newInstance(InitData initData) {
    YouTubePlayerDialog fragment = new YouTubePlayerDialog();
    Bundle args = new Bundle();
    args.putInt(ARGS_ADAPTER_ORDER, initData.adapterOrder);
    args.putString(ARGS_VIDEO_ID, initData.videoId);
    args.putParcelable(ARGS_PLAYBACK_INFO, initData.playbackInfo);
    args.putInt(ARGS_ORIENTATION, initData.orientation);
    fragment.setArguments(args);
    return fragment;
  }

  Callback callback;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    // TODO user must properly custom this.
    if (context instanceof Callback) {
      this.callback = (Callback) context;
    } else if (getTargetFragment() instanceof Callback) {
      this.callback = (Callback) getTargetFragment();
    }
  }

  InitData data;
  YouTubePlayer player;
  ToroYouTubePlayerFragment fragment = ToroYouTubePlayerFragment.newInstance();

  final FragmentLifecycleCallbacks callbacks = new FragmentLifecycleCallbacks() {
    @Override public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v,
        Bundle savedInstanceState) {
      // fm.unregisterFragmentLifecycleCallbacks(this);
      if (fragment == f) initPlayer();
    }

    @Override public void onFragmentStopped(FragmentManager fm, Fragment f) {
      if (fragment == f && player != null) {
        // YouTubePlayer instance is still available, but we need safety here.
        try {
          data.playbackInfo.setResumePosition(player.getCurrentTimeMillis());
        } catch (IllegalStateException er) {
          er.printStackTrace();
        }
      }
    }
  };

  @Override public final void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    Bundle args = getArguments();
    if (args != null) {
      //noinspection ConstantConditions
      data = new InitData(args.getInt(ARGS_ADAPTER_ORDER, -1), args.getString(ARGS_VIDEO_ID),
          args.getParcelable(ARGS_PLAYBACK_INFO), args.getInt(ARGS_ORIENTATION));
    }
    super.setCancelable(false);
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle bundle) {
    return inflater.inflate(R.layout.fragment_dialog_player, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    if (callback != null) callback.onBigPlayerCreated();
    getChildFragmentManager().registerFragmentLifecycleCallbacks(callbacks, false);
    getChildFragmentManager().beginTransaction().replace(view.getId(), fragment).commitNow();
  }

  // YoutubePlayer will be released before this method.
  @Override public void onDestroyView() {
    super.onDestroyView();
    PlaybackInfo info = new PlaybackInfo(data.playbackInfo);
    fragment = null;
    getChildFragmentManager().unregisterFragmentLifecycleCallbacks(callbacks);
    if (callback != null) callback.onBigPlayerDestroyed(data.adapterOrder, data.videoId, info);
  }

  // Guard this, disallow this customization
  @Override public final void setCancelable(boolean cancelable) {
    throw new RuntimeException("Not Supported.");
  }

  void initPlayer() {
    if (this.fragment == null) dismissAllowingStateLoss();
    this.fragment.initialize(BuildConfig.API_KEY, new YouTubePlayer.OnInitializedListener() {
      @Override public void onInitializationSuccess(YouTubePlayer.Provider provider,
          YouTubePlayer youTubePlayer, boolean b) {
        player = youTubePlayer;
        player.setOnFullscreenListener(fullscreenListener);
        player.setFullscreen(true);
        player.loadVideo(data.videoId, (int) data.playbackInfo.getResumePosition());
      }

      @Override public void onInitializationFailure(YouTubePlayer.Provider provider,
          YouTubeInitializationResult youTubeInitializationResult) {

      }
    });
  }

  final YouTubePlayer.OnFullscreenListener fullscreenListener = fullscreen -> {
    if (!fullscreen) {
      if (getActivity() != null) {
        getActivity().setRequestedOrientation(data.orientation);
      }
      dismissAllowingStateLoss();
    }
  };

  public interface Callback {

    void onBigPlayerCreated();

    void onBigPlayerDestroyed(int videoOrder, String baseItem, PlaybackInfo latestInfo);
  }
}
