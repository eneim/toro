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

package im.ene.toro.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.text.Html;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.exoplayer.ExoPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.common.BaseActivity;
import im.ene.toro.sample.common.LoopingMediaSourceBuilder;

import static com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH;
import static im.ene.toro.sample.common.BaseFragment.RESULT_EXTRA_PLAYBACK_INFO;
import static im.ene.toro.sample.common.BaseFragment.RESULT_EXTRA_PLAYER_ORDER;

/**
 * @author eneim (9/20/17).
 */

public class SinglePlayerActivity extends BaseActivity {

  static final String EXTRA_MEDIA_URI = "toro:demo:custom:player:uri";  // Uri
  static final String EXTRA_MEDIA_ORDER = "toro:demo:custom:player:order";  // int
  static final String EXTRA_MEDIA_DESCRIPTION = "toro:demo:custom:player:description"; // String
  static final String EXTRA_MEDIA_PLAYBACK_INFO = "toro:demo:custom:player:info"; // PlaybackInfo
  static final String EXTRA_MEDIA_PLAYER_SIZE = "toro:demo:custom:player:player_size";  // Point
  static final String EXTRA_MEDIA_VIDEO_SIZE = "toro:demo:custom:player:video_size";  // Point
  static final String EXTRA_DEFAULT_FULLSCREEN = "toro:demo:custom:player:fullscreen";  // boolean

  static final String STATE_MEDIA_PLAYBACK_INFO = "todo:demo:player:state:playback";

  public static Intent createIntent(Context base, int order, Uri media, String desc,
      PlaybackInfo playbackInfo, Point playerSize, Point videoSize, boolean fullscreen) {
    Intent intent = new Intent(base, SinglePlayerActivity.class);
    Bundle extras = new Bundle();
    extras.putInt(EXTRA_MEDIA_ORDER, order);
    extras.putString(EXTRA_MEDIA_DESCRIPTION, desc);
    extras.putParcelable(EXTRA_MEDIA_URI, media);
    extras.putParcelable(EXTRA_MEDIA_PLAYBACK_INFO, playbackInfo);
    extras.putParcelable(EXTRA_MEDIA_PLAYER_SIZE, playerSize);
    extras.putParcelable(EXTRA_MEDIA_VIDEO_SIZE, videoSize);
    extras.putBoolean(EXTRA_DEFAULT_FULLSCREEN, fullscreen);
    intent.putExtras(extras);
    return intent;
  }

  private int order;
  private Uri mediaUri;
  private PlaybackInfo playbackInfo;
  private String content;
  Point playerSize;
  Point videoSize;
  // ONLY start with fullscreen on landscape mode or not.
  // If true: this Activity starts in landscape mode, no changeable.
  // If false: this Activity starts in current screen mode, changeable by user (eg: rotate device).
  private boolean fullscreen;

  private ExoPlayerHelper playerHelper;

  @BindView(R.id.player_view) SimpleExoPlayerView playerView;
  // Views below are not available in landscape mode.
  @Nullable @BindView(R.id.media_description) TextView mediaDescription;

  @Override protected void onCreate(@Nullable Bundle state) {
    super.onCreate(state);
    // Only request transition if this Activity start from zero (not re-created).
    if (state == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
    }

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      order = extras.getInt(EXTRA_MEDIA_ORDER);
      mediaUri = extras.getParcelable(EXTRA_MEDIA_URI);
      playbackInfo = extras.getParcelable(EXTRA_MEDIA_PLAYBACK_INFO);
      content = extras.getString(EXTRA_MEDIA_DESCRIPTION);
      playerSize = extras.getParcelable(EXTRA_MEDIA_PLAYER_SIZE);
      videoSize = extras.getParcelable(EXTRA_MEDIA_VIDEO_SIZE);
      fullscreen = extras.getBoolean(EXTRA_DEFAULT_FULLSCREEN, false);
    }

    Point windowSize = new Point();
    getWindow().getWindowManager().getDefaultDisplay().getSize(windowSize);
    boolean landscape = windowSize.y < windowSize.x;

    if (fullscreen) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      setContentView(R.layout.activity_single_player_landscape);
      landscape = true;
    } else {
      // Dynamic layout based on Window size, not the actual device orientation.
      if (landscape) {
        setContentView(R.layout.activity_single_player_landscape);
      } else {
        setContentView(R.layout.activity_single_player);
      }
    }

    ButterKnife.bind(this);
    if (state == null) {
      ViewCompat.setTransitionName(playerView, getString(R.string.transition_name_single_player));
    } else {
      ViewCompat.setTransitionName(playerView, getString(R.string.app_name));
    }

    // Optimize player view UI and resize mode.
    Point size = usableSize(videoSize) ? videoSize : (usableSize(playerSize) ? playerSize : null);
    if (size != null) {
      if (landscape) {
        // Landscape mode, we use resize_mode for Player view content.
        int resizeMode = size.x * windowSize.y > windowSize.x * size.y ?  //
            RESIZE_MODE_FIXED_WIDTH : RESIZE_MODE_FIXED_HEIGHT;
        playerView.setResizeMode(resizeMode);
      } else {
        float ratio = size.y / (float) size.x;
        size.x = Math.max(size.x, windowSize.x);  // max width
        size.y = (int) (size.x * ratio);
        playerView.setMinimumHeight(size.y);
        if (playerView.getLayoutParams() != null) {
          playerView.getLayoutParams().height = size.y;
        }
      }
    }

    if (state != null && state.containsKey(STATE_MEDIA_PLAYBACK_INFO)) {
      playbackInfo = state.getParcelable(STATE_MEDIA_PLAYBACK_INFO);
    }

    if (mediaDescription != null) mediaDescription.setText(Html.fromHtml(content));
    LoopingMediaSourceBuilder mediaSourceBuilder = new LoopingMediaSourceBuilder(this, mediaUri);
    playerHelper = new ExoPlayerHelper(playerView, EXTENSION_RENDERER_MODE_OFF, true);
    playerHelper.setPlaybackInfo(playbackInfo);

    ActivityCompat.postponeEnterTransition(this);
    playerView.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            playerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            ActivityCompat.startPostponedEnterTransition(SinglePlayerActivity.this);
          }
        });

    try {
      playerHelper.prepare(mediaSourceBuilder);
    } catch (ParserException e) {
      e.printStackTrace();
    }
  }

  @Override protected void onStart() {
    super.onStart();
    if (playerHelper != null && !playerHelper.isPlaying()) {
      playerHelper.play();
    }
  }

  @Override protected void onStop() {
    super.onStop();
    if (playerHelper != null) {
      playerHelper.pause();
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (playerHelper != null) {
      playerHelper.release();
      playerHelper = null;
    }
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (playerHelper != null) {
      PlaybackInfo info = playerHelper.getPlaybackInfo();
      outState.putParcelable(STATE_MEDIA_PLAYBACK_INFO, info);
    }
  }

  @Override public void finish() {
    if (playerHelper != null) {
      Intent intent = new Intent();
      intent.putExtra(RESULT_EXTRA_PLAYER_ORDER, order);
      intent.putExtra(RESULT_EXTRA_PLAYBACK_INFO, playerHelper.getPlaybackInfo());
      setResult(Activity.RESULT_OK, intent);
    }
    super.finish();
  }

  static boolean usableSize(Point size) {
    return size != null && size.x > 0 && size.y > 0;
  }
}
