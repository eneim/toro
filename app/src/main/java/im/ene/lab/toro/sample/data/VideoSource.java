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

  private static final String MP4 =
      "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";

  private static final String M4V =
      "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_640x360.m4v";

  private static final String MKV =
      "http://www.sample-videos.com/video/mkv/720/big_buck_bunny_720p_5mb.mkv";

  private static final String MP4_2 =
      "http://www.gomplayer.jp/img/sample/mp4_mpeg4_aac.mp4";

  private static final String HLS =
      "http://walterebert.com/playground/video/hls/sintel-trailer.m3u8";

  // May not support
  private static final String MOV =
      "http://download.blender.org/peach/bigbuckbunny_movies/big_buck_bunny_480p_h264.mov";

  // 3GP, but Java, why ...
  private static final String THREEGP =
      "http://www.sample-videos.com/video/3gp/240/big_buck_bunny_240p_5mb.3gp";

  private static final String FLV =
      "http://www.sample-videos.com/video/flv/720/big_buck_bunny_720p_5mb.flv";  // Old school

  public static final String[] SOURCES = {
      MP4, MP4_2
  };
}
