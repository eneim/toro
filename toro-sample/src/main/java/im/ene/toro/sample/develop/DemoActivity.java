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

package im.ene.toro.sample.develop;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import im.ene.toro.sample.R;
import im.ene.toro.exoplayer2.ExoVideoView;
import im.ene.toro.exoplayer2.PlayerCallback;
import im.ene.toro.exoplayer2.State;

public class DemoActivity extends AppCompatActivity {

  private static final String TAG = "DemoActivity";

  ExoVideoView videoView;
  Button buttonStart;
  Button buttonClose;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_demo);

    videoView = (ExoVideoView) findViewById(R.id.demo_video_view);

    buttonStart = (Button) findViewById(R.id.start);
    if (videoView.isPlaying()) {
      buttonStart.setText("Pause");
    } else {
      buttonStart.setText("Start");
    }

    buttonStart.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (!videoView.isPlaying()) {
          videoView.start();
          buttonStart.setText("Pause");
        } else {
          videoView.pause();
          buttonStart.setText("Start");
        }
      }
    });

    buttonClose = (Button) findViewById(R.id.close);
    buttonClose.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        videoView.stop();
        buttonStart.setText("Start");
      }
    });

    videoView.setPlayerCallback(new PlayerCallback() {
      @Override public void onPlayerStateChanged(boolean playWhenReady, @State int playbackState) {
        Log.d(TAG, "onPlayerStateChanged() called with: playWhenReady = ["
            + playWhenReady
            + "], playbackState = ["
            + playbackState
            + "]");
      }

      @Override public boolean onPlayerError(Exception error) {
        return false;
      }
    });

    videoView.setMedia(Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"));
  }

  @Override protected void onStart() {
    super.onStart();
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      if (!videoView.isPlaying()) {
        videoView.start();
      }
    }
  }

  @Override protected void onStop() {
    super.onStop();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      videoView.pause();
    }
  }
}
