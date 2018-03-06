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

package im.ene.toro.sample.facebook.player;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import butterknife.BindView;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.exoplayer.Playable;
import im.ene.toro.exoplayer.ToroExo;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;
import im.ene.toro.sample.facebook.core.BlackBoardDialogFragment;
import im.ene.toro.sample.facebook.core.ScreenHelper;
import im.ene.toro.sample.facebook.data.FbVideo;

/**
 * @author eneim | 6/21/17.
 */

// TODO: from multi window mode to normal mode --> how the "state saving" flow goes?
public class BigPlayerFragment extends BlackBoardDialogFragment {

  public static final String FRAGMENT_TAG = "Toro:BigPlayerFragment";

  public interface Callback {

    void onBigPlayerCreated();

    void onBigPlayerDestroyed(int order, FbVideo baseItem, PlaybackInfo latestInfo);
  }

  private static final String ARG_KEY_VIDEO_ITEM = "fb:player:video_item";
  private static final String ARG_KEY_VIDEO_ORDER = "fb:player:video:order";
  private static final String ARG_KEY_INIT_INFO = "fb:player:init_info";

  public static BigPlayerFragment newInstance(int order, @NonNull FbVideo video,
      PlaybackInfo info) {
    BigPlayerFragment fragment = new BigPlayerFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_KEY_VIDEO_ORDER, order);
    args.putParcelable(ARG_KEY_VIDEO_ITEM, video);
    if (info != null) args.putParcelable(ARG_KEY_INIT_INFO, info);
    fragment.setArguments(args);
    return fragment;
  }

  // Normally, if user switch from Landscape to Portrait more, this Dialog is saved and restore.
  // We only allow this Dialog to show in Landscape mode, so we need to use WindowManager to see if
  // we are in landscape mode or portrait. If it is portrait we immediately dismiss this dialog.
  private WindowManager windowManager;
  private Callback callback;

  private int videoOrder;
  private FbVideo videoItem;
  private PlaybackInfo playbackInfo;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    this.TAG = "Toro:Fb:BigPlayer";
    Log.d(TAG, "onAttach() called with: context = [" + context + "]");
    if (getParentFragment() != null && getParentFragment() instanceof Callback) {
      this.callback = (Callback) getParentFragment();
    }
  }

  @Override public void onDetach() {
    super.onDetach();
    callback = null;
    windowManager = null;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle bundle = savedInstanceState;
    if (bundle == null) bundle = getArguments();
    if (bundle != null) {
      videoItem = bundle.getParcelable(ARG_KEY_VIDEO_ITEM);
      playbackInfo = bundle.getParcelable(ARG_KEY_INIT_INFO);
      videoOrder = bundle.getInt(ARG_KEY_VIDEO_ORDER);
    }
    if (videoItem == null) throw new IllegalArgumentException("Require a Video item.");
    if (playbackInfo == null) playbackInfo = new PlaybackInfo();
    // on orientation change, by default Android system will try to retain the view hierarchy.
    // so, it will try to destroy this dialog-fragment, and recreate it on new orientation with a
    // saved state.

    // here, we dispatch the orientation check, if we found that it is in portrait mode, we immediately
    // dismiss the dialog to prevent it from showing unexpectedly.
    windowManager = getContext() == null ? null
        : (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    if (windowManager == null || !ScreenHelper.shouldUseBigPlayer(
        windowManager.getDefaultDisplay())) {
      dismissAllowingStateLoss();
    }
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_dialog_facebook_bigplayer, container, false);
  }

  @BindView(R.id.big_player) PlayerView playerView;
  Playable playerHelper;

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (callback != null) {
      callback.onBigPlayerCreated();
    }
    playerView.setKeepScreenOn(true);
    Point windowSize = new Point();
    windowManager.getDefaultDisplay().getSize(windowSize);
    // Optimize playerView scale type
    // Just using AspectRatioFrameLayout.RESIZE_MODE_FILL will not work well for small Video.
    if (windowSize.y * videoItem.getMediaUrl().getRatio() >= windowSize.x) {
      playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
    } else {
      playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
    }
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    /* Surface.Rotation interface, values are 0, 1, 2, 3 */
    int rotation = windowManager.getDefaultDisplay().getRotation();
    if (rotation % 2 != 0) {
      // ROTATION_90 or ROTATION_270 --> not in its natural or reversed natural position.
      // Only do this in landscape mode.
      Window window = getDialog().getWindow();
      if (window != null) {
        View decorView = window.getDecorView();
        int immersiveStickyFrag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
          immersiveStickyFrag = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        } else {
          immersiveStickyFrag = 4096; // not affective, just to by pass the lint
        }

        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            // | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // un-comment for 100% full screen.
            // | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION  // un-comment for 100% full screen.
            | immersiveStickyFrag;
        decorView.setSystemUiVisibility(uiOptions);
      }
    }
  }

  @Override public void onStart() {
    super.onStart();
    if (playerHelper == null) {
      playerHelper = ToroExo.with(getContext()).getDefaultCreator() //
          .createPlayable(videoItem.getMediaUrl().getUri(), null);
      playerHelper.prepare(true);
    }
    playerHelper.setPlayerView(playerView);
    playerHelper.setPlaybackInfo(playbackInfo);
    playerHelper.play();
  }

  @Override public void onStop() {
    super.onStop();
    if (playerHelper != null) playerHelper.pause();
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(ARG_KEY_VIDEO_ORDER, videoOrder);
    outState.putParcelable(ARG_KEY_VIDEO_ITEM, videoItem);
    if (playerHelper != null) playbackInfo = playerHelper.getPlaybackInfo();
    outState.putParcelable(ARG_KEY_INIT_INFO, playbackInfo);
  }

  @Override public void onDestroyView() {
    if (playerHelper != null) {
      playbackInfo = playerHelper.getPlaybackInfo();
      playerHelper.setPlayerView(null);
      playerHelper.release();
      playerHelper = null;
    }
    if (callback != null) {
      callback.onBigPlayerDestroyed(videoOrder, videoItem, playbackInfo);
    }
    super.onDestroyView();
  }

  // Public API
  public static final String BUNDLE_KEY_VIDEO = "fb:player:bundle:video";
  public static final String BUNDLE_KEY_ORDER = "fb:player:bundle:order";
  public static final String BUNDLE_KEY_INFO = "fb:player:bundle:info";

  public Bundle getCurrentState() {
    PlaybackInfo info = playbackInfo;
    if (playerHelper != null) info = playerHelper.getPlaybackInfo();
    Bundle bundle = new Bundle();
    bundle.putParcelable(BUNDLE_KEY_VIDEO, videoItem);
    bundle.putInt(BUNDLE_KEY_ORDER, videoOrder);
    bundle.putParcelable(BUNDLE_KEY_INFO, info);
    return bundle;
  }
}
