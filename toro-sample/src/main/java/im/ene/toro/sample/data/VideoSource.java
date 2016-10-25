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

package im.ene.toro.sample.data;

/**
 * Created by eneim on 1/30/16.
 */
public class VideoSource {

  private static final String MP4 = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";

  public static final String[] SOURCES = {
      MP4,
      "https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears.mpd",
      "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd",
      "http://vod.leasewebcdn.com/bbb.flv?ri=1024&rs=150&start=0"
      // SQUARE, HORIZONTAL, VERTICAL
  };

  public static final String[] YOUTUBES = {
      "J3IvOfvH1ys", "e7t3svG9PTk", "9m6MoBM-sFI", "7BR7Iee_mz8", "PZqzvs-AXYA", "Pms0pcyPbAM"
  };
}
