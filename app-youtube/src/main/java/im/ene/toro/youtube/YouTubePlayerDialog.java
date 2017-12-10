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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

  static final String ARGS_VIDEO_ID = "youtube:dialog:video_id";
  static final String ARGS_PLAYBACK_INFO = "youtube:dialog:playback_info";
  static final String ARGS_ORIENTATION = "youtube:dialog:orientation";

  static class InitData {

    final String videoId;
    final PlaybackInfo playbackInfo;
    final int orientation;  // Original Orientation of requested Activity

    InitData(@NonNull String videoId, @NonNull PlaybackInfo playbackInfo, int orientation) {
      this.videoId = videoId;
      this.playbackInfo = playbackInfo;
      this.orientation = orientation;
    }
  }

  public static YouTubePlayerDialog newInstance(InitData initData) {
    YouTubePlayerDialog fragment = new YouTubePlayerDialog();
    Bundle args = new Bundle();
    args.putString(ARGS_VIDEO_ID, initData.videoId);
    args.putParcelable(ARGS_PLAYBACK_INFO, initData.playbackInfo);
    args.putInt(ARGS_ORIENTATION, initData.orientation);
    fragment.setArguments(args);
    return fragment;
  }

  InitData data;

  @Override public final void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    Bundle args = getArguments();
    if (args != null) {
      //noinspection ConstantConditions
      data = new InitData(args.getString(ARGS_VIDEO_ID), args.getParcelable(ARGS_PLAYBACK_INFO),
          args.getInt(ARGS_ORIENTATION));
    }
    super.setCancelable(false);
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle bundle) {
    return inflater.inflate(R.layout.fragment_dialog_player, container, false);
  }

  ToroYouTubePlayerFragment fragment = ToroYouTubePlayerFragment.newInstance();
  final FragmentManager.FragmentLifecycleCallbacks callbacks =
      new FragmentManager.FragmentLifecycleCallbacks() {
        @Override public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v,
            Bundle savedInstanceState) {
          fm.unregisterFragmentLifecycleCallbacks(this);
          if (fragment == f) initPlayer();
        }
      };

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    getChildFragmentManager().registerFragmentLifecycleCallbacks(callbacks, false);
    getChildFragmentManager().beginTransaction().replace(view.getId(), fragment).commitNow();
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    fragment = null;
    getChildFragmentManager().unregisterFragmentLifecycleCallbacks(callbacks);
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
        youTubePlayer.setOnFullscreenListener(fullscreenListener);
        youTubePlayer.loadVideo(data.videoId, (int) data.playbackInfo.getResumePosition());
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
}
