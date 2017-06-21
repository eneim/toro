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

package im.ene.toro.sample.features.facebook.playlist;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;
import im.ene.toro.sample.features.facebook.core.FullscreenDialogFragment;
import im.ene.toro.sample.features.facebook.data.FbVideo;
import im.ene.toro.widget.Container;

/**
 * @author eneim | 6/19/17.
 */

public class MoreVideosFragment extends FullscreenDialogFragment {

  public interface Callback {

    void onPlaylistViewCreated();

    void onPlaylistViewDestroyed(int basePosition, FbVideo baseItem, PlaybackInfo latestInfo);
  }

  public static final String TAG = "Toro:Fb:MoreVideos";

  private static final String ARG_EXTRA_PLAYBACK_INFO = "fb:more_videos:playback_info";
  private static final String ARG_EXTRA_BASE_FB_VIDEO = "fb:more_videos:base_video";
  private static final String ARG_EXTRA_BASE_ORDER = "fb:more_videos:base_order";

  // We use FbVideo as Parcelable, in real life, it should be retrieved from Database.
  public static MoreVideosFragment newInstance(int position, @NonNull FbVideo video,
      @NonNull PlaybackInfo info) {
    MoreVideosFragment fragment = new MoreVideosFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_EXTRA_BASE_ORDER, position);
    args.putParcelable(ARG_EXTRA_BASE_FB_VIDEO, video);
    args.putParcelable(ARG_EXTRA_PLAYBACK_INFO, info);
    fragment.setArguments(args);
    return fragment;
  }

  private FbVideo baseVideo;
  private PlaybackInfo baseInfo;
  private int baseOrder;

  private Callback callback;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (getParentFragment() != null && getParentFragment() instanceof Callback) {
      this.callback = (Callback) getParentFragment();
    }
  }

  @Override public void onDetach() {
    super.onDetach();
    callback = null;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      baseVideo = getArguments().getParcelable(ARG_EXTRA_BASE_FB_VIDEO);
      baseInfo = getArguments().getParcelable(ARG_EXTRA_PLAYBACK_INFO);
      baseOrder = getArguments().getInt(ARG_EXTRA_BASE_ORDER);
    }

    if (baseVideo == null || baseInfo == null) {
      throw new IllegalArgumentException(
          "Invalid arguments for MoreVideosFragment. Require non-null base Video and PlaybackInfo.");
    }
  }

  Unbinder unbinder;
  @BindView(R.id.recycler_view) Container container;
  RecyclerView.LayoutManager layoutManager;
  MoreVideosAdapter adapter;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.layout_container_facebook_morevideos, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    unbinder = ButterKnife.bind(this, view);

    if (callback != null) {
      callback.onPlaylistViewCreated();
    }

    layoutManager = new LinearLayoutManager(getContext());
    container.setLayoutManager(layoutManager);
    adapter = new MoreVideosAdapter(baseVideo, baseVideo.timeStamp);
    adapter.savePlaybackInfo(0, baseInfo);
    container.setAdapter(adapter);
    container.setPlayerStateManager(adapter);
  }

  @Override public void onDestroyView() {
    if (callback != null && adapter != null) {
      callback.onPlaylistViewDestroyed(baseOrder, baseVideo, adapter.getPlaybackInfo(0));
    }
    unbinder.unbind();
    adapter = null;
    layoutManager = null;
    super.onDestroyView();
  }
}
