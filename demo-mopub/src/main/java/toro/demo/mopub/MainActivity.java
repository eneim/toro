/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
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

package toro.demo.mopub;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import im.ene.toro.exoplayer.Config;
import im.ene.toro.exoplayer.Playable;
import im.ene.toro.exoplayer.ToroExo;
import im.ene.toro.exoplayer.ui.PlayerView;

import static im.ene.toro.exoplayer.MediaSourceBuilder.LOOPING;

public class MainActivity extends AppCompatActivity {

  static final Config config = new Config.Builder().setMediaSourceBuilder(LOOPING).build();
  //static final Uri video = Uri.parse(
  //    "https://storage.googleapis.com/material-design/publish/material_v_12/assets/0B14F_FSUCc01SWc0N29QR3pZT2s/materialmotionhero-spec-0505.mp4");
  static final Uri video = Uri.parse("file:///android_asset/bbb/video.mp4");

  PlayerView playerView;
  Playable<PlayerView> playable;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    playerView = findViewById(R.id.playerView);
    // playerView.setControllerHideOnTouch(false);
    playable = ToroExo.with(this).getCreator(config).createPlayableCompat(video, null);
    playable.prepare(true);
    playable.setPlayerView(playerView);
  }

  @Override protected void onStart() {
    super.onStart();
    if (!playable.isPlaying()) playable.play();
  }

  @Override protected void onStop() {
    super.onStop();
    playable.pause();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    playable.release();
  }
}
