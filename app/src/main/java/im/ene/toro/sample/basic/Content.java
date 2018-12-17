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

package im.ene.toro.sample.basic;

import android.net.Uri;

/**
 * @author eneim (7/1/17).
 */

public class Content {

  // MPD: http://dash.akamaized.net/dash264/TestCasesHD/2b/qualcomm/1/MultiResMPEG2.mpd
  // HLS: http://docs.evostream.com/sample_content/assets/hls-sintel-abr3/playlist.m3u8
  // HLS: https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8
  // HLS: https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8
  // HLS: https://video-dev.github.io/streams/x36xhzz/x36xhzz.m3u8

  // private static final String MP4_BUNNY = "file:///android_asset/bbb.mp4";
  private static final String MP4_BUNNY = "https://video-dev.github.io/streams/x36xhzz/x36xhzz.m3u8";
  private static final String MP4_TOS = "file:///android_asset/tos.mp4";
  private static final String MP4_COSMOS = "file:///android_asset/cosmos.mp4";

  static final String[] ITEMS = {
      "https://video-dev.github.io/streams/x36xhzz/x36xhzz.m3u8",
      "file:///android_asset/cosmos.mp4",
      "http://dash.akamaized.net/dash264/TestCasesHD/2b/qualcomm/1/MultiResMPEG2.mpd",
      "file:///android_asset/tos.mp4",
      "https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8",
      "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
      "file:///android_asset/bbb.mp4",
      // "http://docs.evostream.com/sample_content/assets/hls-sintel-abr3/playlist.m3u8"
  };

  public static class Media {
    public final int index;
    public final Uri mediaUri;

    public Media(int index, Uri mediaUri) {
      this.index = index;
      this.mediaUri = mediaUri;
    }

    static Media getItem(int index) {
      return new Media(index, Uri.parse(ITEMS[index % ITEMS.length]));
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Media)) return false;

      Media media = (Media) o;

      if (index != media.index) return false;
      return mediaUri.equals(media.mediaUri);
    }

    @Override public int hashCode() {
      int result = index;
      result = 31 * result + mediaUri.hashCode();
      return result;
    }
  }
}
