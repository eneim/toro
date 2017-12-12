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

package im.ene.toro.youtube;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.text.TextUtils;
import android.util.Log;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;

import static im.ene.toro.youtube.BuildConfig.API_KEY;

/**
 * @author eneim (2017/11/23).
 */

public class PlaylistViewModel extends ViewModel {

  private static final String TAG = "YouT:ViewModel";

  // droidconNYC 2017 playlist
  private static final String YOUTUBE_PLAYLIST_ID = "PLdb5m83JnoaATBUkWTVxT_kGyhYBozZ4F";
  private static final Long YOUTUBE_PLAYLIST_MAX_RESULTS = 20L;

  //see: https://developers.google.com/youtube/v3/docs/playlistItems/list
  private static final String YOUTUBE_PLAYLIST_PART = "snippet";
  private static final String YOUTUBE_PLAYLIST_FIELDS =
      "pageInfo,nextPageToken,items(id,snippet(resourceId/videoId))";
  //see: https://developers.google.com/youtube/v3/docs/videos/list
  private static final String YOUTUBE_VIDEOS_PART = "snippet,contentDetails,statistics";
  // video resource properties that the response will include.
  private static final String YOUTUBE_VIDEOS_FIELDS =
      "items(id,snippet(title,description,thumbnails/high,channelTitle),contentDetails/duration,statistics)";
  // selector specifying which fields to include in a partial response.

  private final YouTube ytApi;
  private final MutableLiveData<VideoListResponse> liveData = new MutableLiveData<>();
  private final CompositeDisposable disposables;

  @Override protected void onCleared() {
    super.onCleared();
    disposables.clear();
  }

  public PlaylistViewModel() {
    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    ytApi = new YouTube.Builder(httpTransport, jsonFactory, null).setApplicationName(
        "Toro Youtube Demo").build();
    disposables = new CompositeDisposable();
  }

  LiveData<VideoListResponse> getPlaylist() throws IOException {
    return liveData;
  }

  void refresh() throws IOException {
    Disposable disposable = Observable.just(ytApi.playlistItems()
        .list(YOUTUBE_PLAYLIST_PART)
        .setPlaylistId(YOUTUBE_PLAYLIST_ID)
        .setPageToken(null)
        .setFields(YOUTUBE_PLAYLIST_FIELDS)
        .setMaxResults(YOUTUBE_PLAYLIST_MAX_RESULTS)
        .setKey(API_KEY))
        .map(AbstractGoogleClientRequest::execute)
        .map(PlaylistItemListResponse::getItems)
        .flatMap(playlistItems -> Observable.fromIterable(playlistItems)
            .map(item -> item.getSnippet().getResourceId().getVideoId()))
        .toList()
        .map(ids -> ytApi.videos().list(YOUTUBE_VIDEOS_PART).setFields(YOUTUBE_VIDEOS_FIELDS) //
            .setKey(API_KEY).setId(TextUtils.join(",", ids)).execute())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError(throwable -> Log.e(TAG, "accept() called with: throwable = [" + throwable + "]"))
        .doOnSuccess(response -> Log.d(TAG, "accept() called with: response = [" + response + "]"))
        .onErrorReturnItem(new VideoListResponse()) // Bad work around
        .doOnSuccess(liveData::setValue)
        .subscribe();
    disposables.add(disposable);
  }
}
