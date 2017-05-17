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

package im.ene.toro.sample.feature.facebook.bigplayer;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import im.ene.toro.exoplayer2.ExoPlayerHelper;
import im.ene.toro.exoplayer2.ExoPlayerView;
import im.ene.toro.sample.R;

/**
 * @author eneim.
 * @since 5/14/17.
 */

public class BigPlayerFragment extends AppCompatDialogFragment {

  public static final String TAG = "Toro:Fb:BigPlayer";

  static final String ARG_MEDIA_URL = "toro:fb:big:player:media:url";
  static final String ARG_MEDIA_POSITION = "toro:fb:big:player:media:position";

  public static BigPlayerFragment newInstance(String url, long position) {
    BigPlayerFragment fragment = new BigPlayerFragment();
    Bundle args = new Bundle();
    args.putString(ARG_MEDIA_URL, url);
    args.putLong(ARG_MEDIA_POSITION, position);
    fragment.setArguments(args);
    return fragment;
  }

  @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    return super.onCreateDialog(savedInstanceState);
  }

  @Override public int getTheme() {
    return R.style.Toro_Theme_Playlist;
  }

  private String mediaUrl;
  private long startPosition;
  ExoPlayerView playerView;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mediaUrl = getArguments().getString(ARG_MEDIA_URL, null);
      startPosition = getArguments().getLong(ARG_MEDIA_POSITION);
    }

    if (mediaUrl == null) {
      dismissAllowingStateLoss();
    }
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.facebook_big_player, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    playerView = (ExoPlayerView) view;
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    Window window = getDialog().getWindow();
    if (window != null) {
      View decorView = window.getDecorView();
      int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
      decorView.setSystemUiVisibility(uiOptions);
    }
  }

  @Override public void onStart() {
    super.onStart();

    MediaSource mediaSource = ExoPlayerHelper.buildMediaSource(getContext(), Uri.parse(mediaUrl),
        new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "Toro-Sample")),
        playerView.getHandler(), null);

    try {
      playerView.setResumePosition(startPosition);
      playerView.setMediaSource(mediaSource, true);
    } catch (ParserException e) {
      e.printStackTrace();
    }
  }

  Callback callback;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (getTargetFragment() instanceof Callback) {
      this.callback = (Callback) getTargetFragment();
    }

    if (this.callback != null) {
      callback.onBigPlayerAttach();
    }
  }

  @Override public void onDetach() {
    super.onDetach();
    if (this.callback != null) {
      callback.onBigPlayerDetach();
    }
  }

  public interface Callback {

    void onBigPlayerAttach();

    void onBigPlayerDetach();
  }
}
