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

package im.ene.toro.sample.data;

import android.net.Uri;

/**
 * @author eneim | 6/6/17.
 */

public enum MediaUrl {
  DASH_H265_TEARS("https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd"),
  MP4_BUNNY_432("http://dash.edgesuite.net/akamai/bbb_30fps/bbb_30fps_768x432_1500k.mp4"), //
  HLS_BUNNY_240("http://docs.evostream.com/sample_content/assets/hls-bunny-166/playlist.m3u8"), //
  DASH_BUNNY_FULL("http://dash.edgesuite.net/akamai/bbb_30fps/bbb_30fps.mpd"),  //
  WEBM_BUNNY("http://clips.vorwaerts-gmbh.de/big_buck_bunny.webm")  //
  ;

  private final String url;

  MediaUrl(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public Uri getUri() {
    return Uri.parse(this.url);
  }

  @Override public String toString() {
    return name() + "{" + url + "}";
  }
}
