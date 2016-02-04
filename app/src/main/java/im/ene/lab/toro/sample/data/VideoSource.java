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

package im.ene.lab.toro.sample.data;

/**
 * Created by eneim on 1/30/16.
 */
public class VideoSource {

  private static final String MP4 = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";

  private static final String HLS =
      "http://csm.cds1.yospace.com/csm/restart/41728753/666021318435.m3u8";

  private static final String MKV =
      "http://www.sample-videos.com/video/mkv/480/big_buck_bunny_480p_2mb.mkv";

  public static final String[] SOURCES = {
      MP4, HLS, MKV
  };
}
