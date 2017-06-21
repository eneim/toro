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

package im.ene.toro.sample.features.facebook.player;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.extra.ExoPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;
import im.ene.toro.sample.features.facebook.core.BlackBoardDialogFragment;
import im.ene.toro.sample.features.facebook.core.ScreenHelper;
import im.ene.toro.sample.features.facebook.data.FbVideo;

/**
 * @author eneim | 6/21/17.
 */

// TODO: from multi window mode to normal mode --> how the "state saving" flow goes?
public class BigPlayerFragment extends BlackBoardDialogFragment {

  public interface Callback {

    void onPlayerViewCreated();

    void onPlayerViewDestroyed(int order, FbVideo baseItem, PlaybackInfo latestInfo);
  }

  public static final String TAG = "Toro:Fb:BigPlayer";

  private static final String ARG_KEY_VIDEO_ITEM = "fb:player:video_item";
  private static final String ARG_KEY_VIDEO_ORDER = "fb:player:video:order";
  private static final String ARG_KEY_INIT_INFO = "fb:player:init_info";

  public static BigPlayerFragment newInstance(int order, @NonNull FbVideo video, PlaybackInfo info) {
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

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    // on orientation change, by default Android system will try to retain the view hierarchy.
    // so, it will try to destroy this dialog-fragment, and recreate it on new orientation with a
    // saved state.

    // here, we dispatch the orientation check, if we found that it is in portrait mode, we immediately
    // dismiss the dialog to prevent it from showing unexpectedly.
    windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    if (!ScreenHelper.shouldUseBigPlayer(windowManager.getDefaultDisplay())) {
      dismissAllowingStateLoss();
      return;
    }

    if (getParentFragment() != null && getParentFragment() instanceof Callback) {
      this.callback = (Callback) getParentFragment();
    }
  }

  @Override public void onDetach() {
    super.onDetach();
    callback = null;
    windowManager = null;
  }

  private int videoOrder;
  private FbVideo videoItem;
  private PlaybackInfo playbackInfo;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      videoItem = getArguments().getParcelable(ARG_KEY_VIDEO_ITEM);
      playbackInfo = getArguments().getParcelable(ARG_KEY_INIT_INFO);
      videoOrder = getArguments().getInt(ARG_KEY_VIDEO_ORDER);
    }

    if (videoItem == null) throw new IllegalArgumentException("Require a Video item.");
    if (playbackInfo == null) playbackInfo = new PlaybackInfo();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.layout_facebook_player, container, false);
  }

  Unbinder unbinder;
  @BindView(R.id.big_player) SimpleExoPlayerView playerView;
  ExoPlayerHelper playerHelper;

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    unbinder = ButterKnife.bind(this, view);
    if (callback != null) {
      callback.onPlayerViewCreated();
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
    if (windowManager.getDefaultDisplay().getRotation() % 180 != 0) {
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

        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | immersiveStickyFrag;
        decorView.setSystemUiVisibility(uiOptions);
      }
    }
  }

  @Override public void onStart() {
    super.onStart();
    if (playerHelper == null) {
      playerHelper = new ExoPlayerHelper(playerView,  //
          DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF, false);
      try {
        playerHelper.prepare(videoItem.getMediaUrl().getUri());
      } catch (ParserException e) {
        e.printStackTrace();
      }
    }
    playerHelper.setPlaybackInfo(playbackInfo);
    playerHelper.play();
  }

  @Override public void onStop() {
    super.onStop();
    if (playerHelper != null) playerHelper.pause();
  }

  @Override public void onDestroyView() {
    if (callback != null && playerHelper != null) {
      callback.onPlayerViewDestroyed(videoOrder, videoItem, playerHelper.getPlaybackInfo());
    }

    if (playerHelper != null) {
      playerHelper.release();
    }
    unbinder.unbind();
    super.onDestroyView();
  }
}
