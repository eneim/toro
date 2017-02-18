/*
 * Copyright 2017 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro.sample.v3;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.util.Util;
import im.ene.toro.exoplayer2.ExoPlayerView;
import im.ene.toro.exoplayer2.Media;
import im.ene.toro.sample.BaseActivity;
import im.ene.toro.sample.R;
import im.ene.toro.sample.data.OrderedVideoObject;

/**
 * Created by eneim on 2/14/17.
 */

public class MediaPlayerActivity extends BaseActivity {

  private static final String TAG = "MediaPlayerActivity";

  static final String EXTRA_INIT_POSITION = "extra_init_position";

  static final String EXTRA_INIT_VIDEO = "extra_init_video";

  public static Intent createIntent(Context context, OrderedVideoObject video, Long position) {
    Intent intent = new Intent(context, MediaPlayerActivity.class);
    Bundle extras = new Bundle();
    if (video != null) {
      extras.putParcelable(EXTRA_INIT_VIDEO, video);
      if (position != null) {
        extras.putLong(EXTRA_INIT_POSITION, position);
      }
    }
    intent.putExtras(extras);
    return intent;
  }

  ExoPlayerView playerView;

  private OrderedVideoObject video;
  private long position;

  private Media media;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_player);
    playerView = (ExoPlayerView) findViewById(R.id.player_view);

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      this.video = extras.getParcelable(EXTRA_INIT_VIDEO);
      this.position = extras.getLong(EXTRA_INIT_POSITION, 0);
    }

    if (this.video == null) {
      finish();
    }

    media = new Media(Uri.parse(video.video));
    playerView.setResumePosition(position);
  }

  @Override protected void onStart() {
    super.onStart();
    if (Util.SDK_INT > 23) {
      try {
        playerView.setMedia(media, true);
      } catch (ParserException e) {
        e.printStackTrace();
      }
    }
  }

  @Override protected void onResume() {
    super.onResume();
    if ((Util.SDK_INT <= 23 || playerView.getPlayer() == null)) {
      try {
        playerView.setMedia(media, true);
      } catch (ParserException e) {
        e.printStackTrace();
      }
    }
  }

  @Override protected void onPause() {
    super.onPause();
    if (Util.SDK_INT <= 23) {
      playerView.releasePlayer();
    }
  }

  @Override protected void onStop() {
    super.onStop();
    if (Util.SDK_INT > 23) {
      playerView.releasePlayer();
    }
  }

  @Override public void finish() {
    if (playerView != null && playerView.getPlayer() != null) {
      long position = playerView.getCurrentPosition();
      Intent intent = new Intent();
      intent.putExtra(EXTRA_INIT_POSITION, position);
      setResult(RESULT_OK, intent);
    }
    super.finish();
  }

}
