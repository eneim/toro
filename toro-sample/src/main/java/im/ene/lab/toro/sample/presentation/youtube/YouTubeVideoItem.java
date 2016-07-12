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

package im.ene.lab.toro.sample.presentation.youtube;

import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;

/**
 * Created by eneim on 7/6/16.
 */
public class YouTubeVideoItem {

  final String title;
  final String id;
  final String description;
  final Thumbnail thumbnail;

  public YouTubeVideoItem(PlaylistItem origin) {
    PlaylistItemSnippet snippet = origin.getSnippet();
    this.title = snippet.getTitle();
    this.id = snippet.getResourceId().getVideoId();
    this.description = snippet.getDescription();
    this.thumbnail = new Thumbnail(snippet.getThumbnails().getMaxres());
  }

  public static class Thumbnail {

    final long width;
    final long height;
    final String url;

    public Thumbnail(com.google.api.services.youtube.model.Thumbnail origin) {
      this.width = origin.getWidth();
      this.height = origin.getHeight();
      this.url = origin.getUrl();
    }
  }
}
