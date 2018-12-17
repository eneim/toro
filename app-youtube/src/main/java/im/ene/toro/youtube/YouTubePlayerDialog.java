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
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.FragmentLifecycleCallbacks;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.youtube.common.BlackBoardDialogFragment;

/**
 * This {@link DialogFragment} is used to play a YouTube video in fullscreen.
 * Once shown for the first time, it will add a {@link ToroYouTubePlayerFragment} into it and start
 * playing the Video on demand.
 *
 * TODO: take care of the case this Dialog is recreated by system.
 *
 * @author eneim (2017/12/08).
 */

public final class YouTubePlayerDialog extends BlackBoardDialogFragment {

  public static final String TAG = "YouT:BigPlayer";

  static final String ARGS_INIT_DATA = "toro:youtube:init_data";

  public static YouTubePlayerDialog newInstance(@NonNull InitData initData) {
    YouTubePlayerDialog fragment = new YouTubePlayerDialog();
    Bundle args = new Bundle();
    args.putParcelable(ARGS_INIT_DATA, initData);
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof Callback) {
      this.callback = (Callback) context;
    } else if (getTargetFragment() instanceof Callback) {
      this.callback = (Callback) getTargetFragment();
    }
    if (this.callback == null) dismissAllowingStateLoss();
  }

  @Override public void onDetach() {
    super.onDetach();
    callback = null;
    initData = null;
  }

  InitData initData;
  YouTubePlayer player;
  Callback callback;
  Fragment fragment;

  final FragmentLifecycleCallbacks callbacks = new FragmentLifecycleCallbacks() {

    @Override public void onFragmentStarted(@NonNull FragmentManager fm, @NonNull Fragment f) {
      if (f == fragment && (f instanceof ToroYouTubePlayerFragment)) {
        maybeInitPlayer((ToroYouTubePlayerFragment) f);
      }
    }

    @Override public void onFragmentStopped(@NonNull FragmentManager fm, @NonNull Fragment f) {
      if (f == fragment && player != null) {
        player.release();
        player = null;
      }
    }
  };

  @Override public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    if (initData != null) requireActivity().setRequestedOrientation(initData.returnOrientation);
  }

  @Nullable InitData getDataFromArgs() {
    Bundle args = getArguments();
    return args != null ? args.getParcelable(ARGS_INIT_DATA) : null;
  }

  InitData getLatestData() {
    if (initData == null) return null;
    if (player != null) {
      try {
        initData.playbackInfo.setResumePosition(player.getCurrentTimeMillis());
      } catch (IllegalStateException er) {
        er.printStackTrace();
      }
    }

    PlaybackInfo info = new PlaybackInfo(initData.playbackInfo);
    return new InitData(initData.adapterOrder, initData.videoId, info, initData.returnOrientation);
  }

  @Override public final void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    super.setCancelable(false);
    this.initData = getDataFromArgs();
    if (initData != null) {
      getChildFragmentManager().registerFragmentLifecycleCallbacks(callbacks, false);
    }
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle bundle) {
    return inflater.inflate(R.layout.fragment_dialog_player, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    if (callback != null) callback.onBigPlayerCreated();
    initializedListener = new YouTubePlayer.OnInitializedListener() {
      @Override public void onInitializationSuccess(YouTubePlayer.Provider provider,
          YouTubePlayer youTubePlayer, boolean b) {
        player = youTubePlayer;
        if (initData != null) {
          player.setShowFullscreenButton(false);
          player.loadVideo(initData.videoId, (int) initData.playbackInfo.getResumePosition());
        }
      }

      @Override public void onInitializationFailure(YouTubePlayer.Provider provider,
          YouTubeInitializationResult youTubeInitializationResult) {

      }
    };

    fragment = getChildFragmentManager().findFragmentById(R.id.player_dialog);
    if (fragment == null) {
      fragment = ToroYouTubePlayerFragment.newInstance();
      getChildFragmentManager().beginTransaction().replace(R.id.player_dialog, fragment).commit();
    }
  }

  // YoutubePlayer will be released before this method.
  @Override public void onDestroyView() {
    super.onDestroyView();
    initializedListener = null;
    if (initData != null) {
      PlaybackInfo info = new PlaybackInfo(initData.playbackInfo);
      if (callback != null) {
        callback.onBigPlayerDestroyed(initData.adapterOrder, initData.videoId, info);
      }
    }
    fragment = null;
    initData = null;
  }

  // Guard this, disallow this customization
  @Override public final void setCancelable(boolean cancelable) {
    throw new RuntimeException("Not Supported.");
  }

  final void maybeInitPlayer(@NonNull final ToroYouTubePlayerFragment fragment) {
    final View view = fragment.getView();
    if (view == null) return;
    view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        fragment.initialize(BuildConfig.API_KEY, initializedListener);
      }
    });
  }

  YouTubePlayer.OnInitializedListener initializedListener;

  public interface Callback {

    void onBigPlayerCreated();

    void onBigPlayerDestroyed(int videoOrder, String baseItem, PlaybackInfo latestInfo);
  }

  static class InitData implements Parcelable {

    final int adapterOrder;
    @NonNull final String videoId;
    @NonNull final PlaybackInfo playbackInfo;
    // Original orientation of requested Activity, used for restoring the orientation.
    final int returnOrientation;

    InitData(int adapterOrder, @NonNull String videoId, @NonNull PlaybackInfo playbackInfo,
        int returnOrientation) {
      this.adapterOrder = adapterOrder;
      this.videoId = videoId;
      this.playbackInfo = playbackInfo;
      this.returnOrientation = returnOrientation;
    }

    @Override public int describeContents() {
      return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(this.adapterOrder);
      dest.writeString(this.videoId);
      dest.writeParcelable(this.playbackInfo, flags);
      dest.writeInt(this.returnOrientation);
    }

    InitData(Parcel in) {
      this.adapterOrder = in.readInt();
      this.videoId = in.readString();
      this.playbackInfo = in.readParcelable(PlaybackInfo.class.getClassLoader());
      this.returnOrientation = in.readInt();
    }

    public static final Creator<InitData> CREATOR = new Creator<InitData>() {
      @Override public InitData createFromParcel(Parcel source) {
        return new InitData(source);
      }

      @Override public InitData[] newArray(int size) {
        return new InitData[size];
      }
    };
  }
}
