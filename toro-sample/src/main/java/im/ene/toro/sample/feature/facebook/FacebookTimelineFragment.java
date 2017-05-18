/*
 * Copyright 2017 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro.sample.feature.facebook;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.google.android.exoplayer2.C;
import im.ene.toro.PlaybackState;
import im.ene.toro.Toro;
import im.ene.toro.ToroPlayer;
import im.ene.toro.sample.BaseToroFragment;
import im.ene.toro.sample.R;
import im.ene.toro.sample.feature.facebook.bigplayer.BigPlayerFragment;
import im.ene.toro.sample.feature.facebook.playlist.FacebookPlaylistFragment;
import im.ene.toro.sample.feature.facebook.timeline.TimelineAdapter;
import im.ene.toro.sample.feature.facebook.timeline.TimelineItem;
import im.ene.toro.sample.feature.facebook.timeline.TimelineItem.VideoItem;
import im.ene.toro.sample.util.DemoUtil;
import java.util.ArrayList;

/**
 * @author eneim.
 * @since 4/13/17.
 */

public class FacebookTimelineFragment extends BaseToroFragment
    implements FacebookPlaylistFragment.Callback, BigPlayerFragment.Callback {

  private static final String TAG = "Toro:Fb:Timeline";

  static final String ARGS_PLAYBACK_STATES = "toro:fb:timeline:playback:states";
  static final String ARGS_PLAYBACK_LATEST = "toro:fb:timeline:playback:latest";

  public static FacebookTimelineFragment newInstance() {
    Bundle args = new Bundle();
    FacebookTimelineFragment fragment = new FacebookTimelineFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
  TimelineAdapter adapter;
  LinearLayoutManager layoutManager;
  boolean isActive = false;

  BigPlayerFragment bigPlayerFragment;
  private WindowManager windowManager;

  private Unbinder unbinder;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.generic_recycler_view, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    unbinder = ButterKnife.bind(this, view);

    adapter = new TimelineAdapter();
    layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
    mRecyclerView.setHasFixedSize(false);
    mRecyclerView.setLayoutManager(layoutManager);
    mRecyclerView.setAdapter(adapter);

    adapter.setOnItemClickListener(new TimelineAdapter.ItemClickListener() {
      @Override protected void onOgpItemClick(RecyclerView.ViewHolder viewHolder, View view,
          TimelineItem.OgpItem item) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getItemUrl()));
        startActivity(intent);
      }

      @Override protected void onPhotoClick(RecyclerView.ViewHolder viewHolder, View view,
          TimelineItem.PhotoItem item) {

      }

      @Override
      protected void onVideoClick(RecyclerView.ViewHolder viewHolder, View view, VideoItem item) {
        if (item == null) {
          return;
        }

        long duration = C.LENGTH_UNSET;
        long position = C.POSITION_UNSET;
        int order = viewHolder.getAdapterPosition();
        ToroPlayer player = adapter.getPlayer();
        if (player != null) {
          PlaybackState state =
              adapter.getPlaybackState(DemoUtil.genVideoId(item.getVideoUrl(), order));
          duration = player.getDuration();
          position = player.isPlaying() ? player.getCurrentPosition()
              : state != null ? state.getPosition() : 0; // safe
        }

        FacebookPlaylistFragment playlistFragment =
            FacebookPlaylistFragment.newInstance(item, position, duration, order);
        playlistFragment.setTargetFragment(FacebookTimelineFragment.this, 1000);
        playlistFragment.show(getChildFragmentManager(),
            FacebookPlaylistFragment.class.getSimpleName());
      }
    });
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    ToroPlayer player = adapter.getPlayer();
    if (player != null) {
      adapter.savePlaybackState(player.getMediaId(), player.getCurrentPosition(),
          player.getDuration());
      outState.putParcelable(ARGS_PLAYBACK_LATEST,  //
          new SavedPlayback(  //
              (VideoItem) adapter.getItem(player.getPlayOrder()).getEmbedItem(),
              new PlaybackState(player.getMediaId(), player.getDuration(),
                  player.getCurrentPosition())  //
          ) //
      );
    }

    outState.putParcelableArrayList(ARGS_PLAYBACK_STATES, adapter.getPlaybackStates());
    Log.d(TAG, "onSaveInstanceState() called with: outState = [" + outState + "]");
  }

  @Override public void onViewStateRestored(@Nullable Bundle state) {
    super.onViewStateRestored(state);
    Log.d(TAG, "onViewStateRestored() called with: state = [" + state + "]");
    ArrayList<PlaybackState> savedStates;
    if (state != null
        && state.containsKey(ARGS_PLAYBACK_STATES)
        && (savedStates = state.getParcelableArrayList(ARGS_PLAYBACK_STATES)) != null) {
      for (PlaybackState playbackState : savedStates) {
        adapter.savePlaybackState(playbackState.getMediaId(), playbackState.getPosition(),
            playbackState.getDuration());
      }
    }

    if (bigPlayerFragment != null) {
      bigPlayerFragment.dismissAllowingStateLoss();
    }

    if (windowManager.getDefaultDisplay().getRotation() % 180 == 0) {
      Toro.register(mRecyclerView);
    } else {
      // in landscape
      SavedPlayback latestState = state != null && state.containsKey(ARGS_PLAYBACK_LATEST)
          ? (SavedPlayback) state.getParcelable(ARGS_PLAYBACK_LATEST) : null;

      if (latestState == null) {
        Toro.register(mRecyclerView);
        return;
      }

      VideoItem videoItem = latestState.videoItem;
      bigPlayerFragment = BigPlayerFragment.newInstance(videoItem.getVideoUrl(),
          latestState.playbackState);
      bigPlayerFragment.setTargetFragment(this, 2000);
      bigPlayerFragment.show(getChildFragmentManager(), BigPlayerFragment.TAG);
    }
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    Toro.unregister(mRecyclerView);
    if (unbinder != null) {
      unbinder.unbind();
    }
  }

  @Override public void onDetach() {
    super.onDetach();
    windowManager = null;
  }

  @Override public void onPlaylistAttached() {
    Toro.unregister(mRecyclerView);
  }

  @Override
  public void onPlaylistDetached(VideoItem baseItem, @NonNull PlaybackState state, int order) {
    adapter.savePlaybackState(DemoUtil.genVideoId(baseItem.getVideoUrl(), order),
        state.getPosition(), state.getDuration());

    if (isActive) {
      Toro.register(mRecyclerView);
    }
  }

  @Override protected void dispatchFragmentActive() {
    isActive = true;
  }

  @Override protected void dispatchFragmentInactive() {
    isActive = false;
  }

  @Override public void onBigPlayerAttached() {
    Toro.unregister(mRecyclerView);
  }

  @Override public void onBigPlayerDetached(@NonNull PlaybackState state) {
    Log.d(TAG, "onBigPlayerDetached() called with: state = [" + state + "]");
    Log.i(TAG, "onBigPlayerDetached: " + mRecyclerView + " | " + adapter);
    if (adapter != null) {
      adapter.savePlaybackState(state.getMediaId(), state.getPosition(), state.getDuration());
    }
  }

  // save latest playback item if there is
  static class SavedPlayback implements Parcelable {

    final VideoItem videoItem;
    final PlaybackState playbackState;

    public SavedPlayback(VideoItem videoItem, PlaybackState playbackState) {
      this.videoItem = videoItem;
      this.playbackState = playbackState;
    }

    protected SavedPlayback(Parcel in) {
      videoItem = in.readParcelable(VideoItem.class.getClassLoader());
      playbackState = in.readParcelable(PlaybackState.class.getClassLoader());
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
      dest.writeParcelable(videoItem, flags);
      dest.writeParcelable(playbackState, flags);
    }

    @Override public int describeContents() {
      return 0;
    }

    public static final Creator<SavedPlayback> CREATOR = new Creator<SavedPlayback>() {
      @Override public SavedPlayback createFromParcel(Parcel in) {
        return new SavedPlayback(in);
      }

      @Override public SavedPlayback[] newArray(int size) {
        return new SavedPlayback[size];
      }
    };
  }
}
