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

import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import im.ene.lab.toro.Toro;
import im.ene.lab.toro.ToroPlayer;
import im.ene.lab.toro.ToroStrategy;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.ToroApp;
import im.ene.lab.toro.sample.presentation.facebook.OrderedPlayList;
import im.ene.lab.toro.sample.widget.DividerItemDecoration;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by eneim on 4/8/16.
 */
public class YouTubeVideosFragment extends Fragment {

  private static final String TAG = "Toro:Youtube";

  public static YouTubeVideosFragment newInstance() {
    return new YouTubeVideosFragment();
  }

  protected RecyclerView mRecyclerView;
  protected RecyclerView.Adapter mAdapter;

  private int firstVideoPosition;
  ToroStrategy strategyToRestore;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    strategyToRestore = Toro.getStrategy();

    Toro.setStrategy(new ToroStrategy() {
      boolean isFirstPlayerDone = false;

      @Override public String getDescription() {
        return "First video plays first";
      }

      @Override public ToroPlayer findBestPlayer(List<ToroPlayer> candidates) {
        return strategyToRestore.findBestPlayer(candidates);
      }

      @Override public boolean allowsToPlay(ToroPlayer player, ViewParent parent) {
        boolean allowToPlay = (isFirstPlayerDone || player.getPlayOrder() == firstVideoPosition)  //
            && strategyToRestore.allowsToPlay(player, parent);

        // A work-around to keep track of first video on top.
        if (player.getPlayOrder() == firstVideoPosition) {
          isFirstPlayerDone = true;
        }
        return allowToPlay;
      }
    });
  }

  @Override public void onDetach() {
    super.onDetach();
    Toro.setStrategy(strategyToRestore);
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ytAccountCredential = GoogleAccountCredential.usingOAuth2(getContext(),
        Collections.singleton(YouTubeScopes.YOUTUBE));
    ytAccountCredential.setSelectedAccountName(
        ToroApp.pref().getString(ToroApp.PREF_ACCOUNT_NAME, null));
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.generic_recycler_view, container, false);
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2) @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
    RecyclerView.LayoutManager layoutManager = getLayoutManager();
    mRecyclerView.setLayoutManager(layoutManager);
    if (layoutManager instanceof LinearLayoutManager) {
      mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
          ((LinearLayoutManager) layoutManager).getOrientation()));
    }

    mAdapter = getAdapter();
    mRecyclerView.setHasFixedSize(false);
    mRecyclerView.setAdapter(mAdapter);

    if (mAdapter instanceof OrderedPlayList) {
      firstVideoPosition = ((OrderedPlayList) mAdapter).firstVideoPosition();
    }
  }

  @Override public void onResume() {
    super.onResume();
    Toro.register(mRecyclerView);
    if (checkGooglePlayServicesAvailable()) {
      haveGooglePlayServices();
    }
  }

  @Override public void onPause() {
    super.onPause();
    Toro.unregister(mRecyclerView);
  }

  @NonNull protected RecyclerView.LayoutManager getLayoutManager() {
    return new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
  }

  @NonNull protected RecyclerView.Adapter getAdapter() {
    return new MyYouTubeVideosAdapter(getChildFragmentManager(), null);
  }

  // Youtube Api AUTH Stuff
  static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;

  static final int REQUEST_AUTHORIZATION = 1;

  static final int REQUEST_ACCOUNT_PICKER = 2;

  GoogleAccountCredential ytAccountCredential;

  /** Check that Google Play services APK is installed and up to date. */
  private boolean checkGooglePlayServicesAvailable() {
    // GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
    final int connectionStatusCode =
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

    if (GoogleApiAvailability.getInstance().isUserResolvableError(connectionStatusCode)) {
      getActivity().runOnUiThread(new Runnable() {
        public void run() {
          GoogleApiAvailability.getInstance()
              .showErrorDialogFragment(getActivity(), connectionStatusCode,
                  REQUEST_GOOGLE_PLAY_SERVICES, null);
        }
      });
      return false;
    }

    return true;
  }

  private void haveGooglePlayServices() {
    // check if there is already an account selected
    if (ytAccountCredential.getSelectedAccountName() == null) {
      // ask user to choose account
      chooseAccount();
    } else {
      // load calendars
      // AsyncLoadTasks.run(this);
      loadYoutubeData();
    }
  }

  private void chooseAccount() {
    startActivityForResult(ytAccountCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case REQUEST_GOOGLE_PLAY_SERVICES:
        if (resultCode == Activity.RESULT_OK) {
          haveGooglePlayServices();
        } else {
          checkGooglePlayServicesAvailable();
        }
        break;
      case REQUEST_AUTHORIZATION:
        if (resultCode == Activity.RESULT_OK) {
          loadYoutubeData();
        } else {
          chooseAccount();
        }
        break;
      case REQUEST_ACCOUNT_PICKER:
        if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
          String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
          if (accountName != null) {
            ytAccountCredential.setSelectedAccountName(accountName);
            ToroApp.pref().edit().putString(ToroApp.PREF_ACCOUNT_NAME, accountName).apply();
            loadYoutubeData();
          }
        }
        break;
    }
  }

  // Data loading
  private void loadYoutubeData() {
    final YouTube youtube = new YouTube.Builder(YouTubeDataHelper.HTTP_TRANSPORT, YouTubeDataHelper.JSON_FACTORY,
        ytAccountCredential).setApplicationName("ToroApp/2.0").build();

    Observable.create(new Observable.OnSubscribe<PlaylistItemListResponse>() {
      @Override public void call(Subscriber<? super PlaylistItemListResponse> subscriber) {
        try {
          subscriber.onNext(youtube.playlistItems()
              .list("snippet")
              .setPlaylistId(YouTubeDataHelper.LIST_GOOGLE_IO_ANDROID)
              .setMaxResults(50L)
              .execute());
          subscriber.onCompleted();
        } catch (IOException e) {
          e.printStackTrace();
          subscriber.onError(e);
        }
      }
    })
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<PlaylistItemListResponse>() {
          @Override public void call(PlaylistItemListResponse response) {
            mRecyclerView.setAdapter(
                new MyYouTubeVideosAdapter(getChildFragmentManager(), response));
          }
        }, new Action1<Throwable>() {
          @Override public void call(Throwable throwable) {
            Log.e(TAG, "call() called with: " + "throwable = [" + throwable + "]");
            if (throwable instanceof UserRecoverableAuthIOException) {
              startActivityForResult(((UserRecoverableAuthIOException) throwable).getIntent(),
                  REQUEST_AUTHORIZATION);
            }
          }
        });
  }
}
