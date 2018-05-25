
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

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import com.squareup.moshi.Json;

@Entity(tableName = "photo_item")
public class PhotoItem {

  @Json(name = "largeImageURL") private String largeImageURL;
  @Json(name = "webformatHeight") private Integer webformatHeight;
  @Json(name = "webformatWidth") private Integer webformatWidth;
  @Json(name = "likes") private Integer likes;
  @Json(name = "imageWidth") private Integer imageWidth;
  @Json(name = "id") private Integer id;
  @Json(name = "user_id") private Integer userId;
  @Json(name = "views") private Integer views;
  @Json(name = "comments") private Integer comments;
  @PrimaryKey @NonNull @Json(name = "pageURL") private String pageURL;
  @Json(name = "imageHeight") private Integer imageHeight;
  @Json(name = "webformatURL") private String webformatURL;
  @Json(name = "type") private String type;
  @Json(name = "previewHeight") private Integer previewHeight;
  @Json(name = "tags") private String tags;
  @Json(name = "downloads") private Integer downloads;
  @Json(name = "user") private String user;
  @Json(name = "favorites") private Integer favorites;
  @Json(name = "imageSize") private Integer imageSize;
  @Json(name = "previewWidth") private Integer previewWidth;
  @Json(name = "userImageURL") private String userImageURL;
  @Json(name = "previewURL") private String previewURL;

  public String getLargeImageURL() {
    return largeImageURL;
  }

  public void setLargeImageURL(String largeImageURL) {
    this.largeImageURL = largeImageURL;
  }

  public Integer getWebformatHeight() {
    return webformatHeight;
  }

  public void setWebformatHeight(Integer webformatHeight) {
    this.webformatHeight = webformatHeight;
  }

  public Integer getWebformatWidth() {
    return webformatWidth;
  }

  public void setWebformatWidth(Integer webformatWidth) {
    this.webformatWidth = webformatWidth;
  }

  public Integer getLikes() {
    return likes;
  }

  public void setLikes(Integer likes) {
    this.likes = likes;
  }

  public Integer getImageWidth() {
    return imageWidth;
  }

  public void setImageWidth(Integer imageWidth) {
    this.imageWidth = imageWidth;
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

  public String getPageURL() {
    return pageURL;
  }

  public void setPageURL(String pageURL) {
    this.pageURL = pageURL;
  }

  public Integer getImageHeight() {
    return imageHeight;
  }

  public void setImageHeight(Integer imageHeight) {
    this.imageHeight = imageHeight;
  }

  public String getWebformatURL() {
    return webformatURL;
  }

  public void setWebformatURL(String webformatURL) {
    this.webformatURL = webformatURL;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Integer getPreviewHeight() {
    return previewHeight;
  }

  public void setPreviewHeight(Integer previewHeight) {
    this.previewHeight = previewHeight;
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

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public Integer getFavorites() {
    return favorites;
  }

  public void setFavorites(Integer favorites) {
    this.favorites = favorites;
  }

  public Integer getImageSize() {
    return imageSize;
  }

  public void setImageSize(Integer imageSize) {
    this.imageSize = imageSize;
  }

  public Integer getPreviewWidth() {
    return previewWidth;
  }

  public void setPreviewWidth(Integer previewWidth) {
    this.previewWidth = previewWidth;
  }

  public String getUserImageURL() {
    return userImageURL;
  }

  public void setUserImageURL(String userImageURL) {
    this.userImageURL = userImageURL;
  }

  public String getPreviewURL() {
    return previewURL;
  }

  public void setPreviewURL(String previewURL) {
    this.previewURL = previewURL;
  }
}
