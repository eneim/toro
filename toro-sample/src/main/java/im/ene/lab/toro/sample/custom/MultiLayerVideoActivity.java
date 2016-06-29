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

package im.ene.lab.toro.sample.custom;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import im.ene.lab.toro.ext.layeredvideo.MultiLayerVideoPlayerView;
import im.ene.lab.toro.player.Video;
import im.ene.lab.toro.sample.util.Util;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by eneim on 6/28/16.
 */
public class MultiLayerVideoActivity extends AppCompatActivity {

  MultiLayerVideoPlayerView videoPlayerView;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    videoPlayerView = new MultiLayerVideoPlayerView(this);
    setContentView(videoPlayerView);
  }

  @Override protected void onResume() {
    super.onResume();
    try {
      File[] local = Util.loadMovieFolder();
      Video video = new Video(
          local != null && local.length > 0 ? Uri.fromFile(Util.loadMovieFolder()[0])
              : Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"), "Garden of Words");

      videoPlayerView.setMedia(video);
      videoPlayerView.start();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}
