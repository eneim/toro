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

package im.ene.lab.toro.sample.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import im.ene.lab.toro.player.widget.TextureVideoView;
import im.ene.lab.toro.sample.R;

/**
 * Created by eneim on 6/3/16.
 */
public class StandalonePlayerActivity extends AppCompatActivity {

  private TextureVideoView mVideoPlayerView;

  private Button mPlayPause;
  private boolean playRequested = true;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sample_videoplayer);
    mVideoPlayerView = (TextureVideoView) findViewById(R.id.player_view);
    mVideoPlayerView.setMediaUri(Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"));

    mPlayPause = (Button) findViewById(R.id.btn_play_pause);
    mPlayPause.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        playRequested = !playRequested;
        if (playRequested) {
          mVideoPlayerView.start();
        } else {
          mVideoPlayerView.pause();
        }
        updateButtonText();
      }
    });
    updateButtonText();
  }

  private void updateButtonText() {
    if (playRequested) {
      mPlayPause.setText("PAUSE");
    } else {
      mPlayPause.setText("PLAY");
    }
  }

  @Override protected void onResume() {
    super.onResume();
    if (playRequested) {
      mVideoPlayerView.start();
    }
  }

  @Override protected void onPause() {
    super.onPause();
    mVideoPlayerView.pause();
  }
}
