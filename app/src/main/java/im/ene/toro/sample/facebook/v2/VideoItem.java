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

package im.ene.toro.sample.facebook.v2;

import android.net.Uri;
import android.support.annotation.NonNull;

public class VideoItem extends BaseItem {

  private final Uri videoUri;
  private final String customType;
  private final int videoWidth;
  private final int videoHeight;

  private VideoItem(Uri videoUri, int videoWidth, int videoHeight) {
    this.videoUri = videoUri;
    this.customType = null;
    this.videoWidth = videoWidth;
    this.videoHeight = videoHeight;
  }

  public VideoItem(Uri videoUri, String customType, int videoWidth, int videoHeight) {
    this.videoUri = videoUri;
    this.customType = customType;
    this.videoWidth = videoWidth;
    this.videoHeight = videoHeight;
  }

  public Uri getVideoUri() {
    return videoUri;
  }

  public String getCustomType() {
    return customType;
  }

  public int getVideoWidth() {
    return videoWidth;
  }

  public int getVideoHeight() {
    return videoHeight;
  }

  public VideoItem getCopy() {
    return new VideoItem(this.videoUri, this.videoWidth, this.videoHeight);
  }

  @NonNull @Override public String toString() {
    return "VideoItem{" +
        "videoUri=" + videoUri +
        ", videoWidth=" + videoWidth +
        ", videoHeight=" + videoHeight +
        '}';
  }

  static final VideoItem[] VIDEOS = new VideoItem[] {
      new VideoItem(
          Uri.parse("https://video-dev.github.io/streams/x36xhzz/x36xhzz.m3u8"),
          1920, 1080
      ),
      new VideoItem(
          Uri.parse("file:///android_asset/cosmos.mp4"), 768, 768
      ),
      new VideoItem(
          Uri.parse(
              "https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8"),
          // Uri.parse("file:///android_asset/test_vid_1.mov"),
          1920, 1080
          // 320, 720
      ),
      new VideoItem(
          Uri.parse("https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"),
          4096, 1744
      ),
      new VideoItem(
          Uri.parse("file:///android_asset/bbb.mp4"), 640, 360
      )
  };
}
