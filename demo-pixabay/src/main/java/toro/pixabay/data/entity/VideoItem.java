
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

package toro.pixabay.data.entity;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import com.squareup.moshi.Json;

@Entity public class VideoItem {

  @Json(name = "picture_id") private String pictureId;
  @Embedded @Json(name = "videos") private VideoSizes videos;
  @Json(name = "tags") private String tags;
  @Json(name = "downloads") private Integer downloads;
  @Json(name = "likes") private Integer likes;
  @Json(name = "favorites") private Integer favorites;
  @Json(name = "duration") private Integer duration;
  @Json(name = "id") private Integer id;
  @Json(name = "user_id") private Integer userId;
  @Json(name = "views") private Integer views;
  @Json(name = "comments") private Integer comments;
  @Json(name = "userImageURL") private String userImageURL;
  @PrimaryKey @NonNull @Json(name = "pageURL") private String pageURL;
  @Json(name = "type") private String type;
  @Json(name = "user") private String user;

  public String getPictureId() {
    return pictureId;
  }

  public void setPictureId(String pictureId) {
    this.pictureId = pictureId;
  }

  public VideoSizes getVideos() {
    return videos;
  }

  public void setVideos(VideoSizes videos) {
    this.videos = videos;
  }

  public String getTags() {
    return tags;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }

  public Integer getDownloads() {
    return downloads;
  }

  public void setDownloads(Integer downloads) {
    this.downloads = downloads;
  }

  public Integer getLikes() {
    return likes;
  }

  public void setLikes(Integer likes) {
    this.likes = likes;
  }

  public Integer getFavorites() {
    return favorites;
  }

  public void setFavorites(Integer favorites) {
    this.favorites = favorites;
  }

  public Integer getDuration() {
    return duration;
  }

  public void setDuration(Integer duration) {
    this.duration = duration;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public Integer getViews() {
    return views;
  }

  public void setViews(Integer views) {
    this.views = views;
  }

  public Integer getComments() {
    return comments;
  }

  public void setComments(Integer comments) {
    this.comments = comments;
  }

  public String getUserImageURL() {
    return userImageURL;
  }

  public void setUserImageURL(String userImageURL) {
    this.userImageURL = userImageURL;
  }

  public String getPageURL() {
    return pageURL;
  }

  public void setPageURL(String pageURL) {
    this.pageURL = pageURL;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }
}
