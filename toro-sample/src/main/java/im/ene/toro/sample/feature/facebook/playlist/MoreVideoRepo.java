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

package im.ene.toro.sample.feature.facebook.playlist;

import im.ene.toro.sample.feature.facebook.timeline.TimelineItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eneim on 10/13/16.
 */

public class MoreVideoRepo {

  private final TimelineItem.VideoItem baseItem;

  public MoreVideoRepo(TimelineItem.VideoItem baseItem) {
    this.baseItem = baseItem;
  }

  public void getMoreVideos(Callback callback) {
    if (callback == null) {
      throw new IllegalArgumentException("Required a non null Callback");
    }
    List<TimelineItem.VideoItem> moreVideos = new ArrayList<>();
    for (int i = 0; i < 15; i++) {
      moreVideos.add(Videos.getVideo(i));
    }
    callback.onMoreVideosLoaded(moreVideos);
  }

  public interface Callback {

    void onMoreVideosLoaded(List<TimelineItem.VideoItem> items);
  }

  enum Videos {
    HORIZONTAL("file:///android_asset/horizontal.mp4"),
    SQUARE("file:///android_asset/square.mp4"),
    VERTICAL("file:///android_asset/vertical.mp4");

    private final String videoUrl;

    Videos(String videoUrl) {
      this.videoUrl = videoUrl;
    }

    private String getVideoUrl() {
      return videoUrl;
    }

    static TimelineItem.VideoItem getVideo(int origin) {
      return new TimelineItem.VideoItem(
          Videos.values()[origin % Videos.values().length].getVideoUrl());
    }
  }
}
