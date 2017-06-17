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

package im.ene.toro.sample.common;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.helper.ExoPlayerHelper;
import im.ene.toro.helper.SimpleExoPlayerViewHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;
import im.ene.toro.sample.data.MediaItem;
import im.ene.toro.widget.Container;
import java.util.Arrays;
import java.util.List;

/**
 * @author eneim | 6/6/17.
 */

public class SimpleExoPlayerViewHolder extends BaseViewHolder implements ToroPlayer {

  private static final String TAG = "Toro:VH:ExoPlayer";

  static final int LAYOUT_RES = R.layout.vh_exoplayer_basic;

  @SuppressWarnings("WeakerAccess") @Nullable SimpleExoPlayerViewHelper helper;
  @Nullable private Uri mediaUri;

  @BindView(R.id.player) SimpleExoPlayerView playerView;
  public @BindView(R.id.text_content) Button content;
  @BindView(R.id.player_state) TextView state;
  @BindView(R.id.player_format) TextView format;

  private ExoPlayerHelper.EventListener eventListener = new ExoPlayerHelper.EventListener() {
    @SuppressLint("SetTextI18n") @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      state.setText("STATE: " + playbackState + ", PWR: " + playWhenReady);
      if (playbackState >= 2) {
        if (helper != null && helper.getPlayer() != null) {
          Format[] formats = getCurrentFormats(helper.getPlayer());
          format.setText(formats != null ? Arrays.toString(formats) : "Format: NULL");
        }
      }

      boolean screenOn = playbackState >= 2 && playbackState <= 3 && playWhenReady;
      playerView.setKeepScreenOn(screenOn);
    }

    @SuppressLint("SetTextI18n") @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
      if (helper == null || helper.getPlayer() == null) return;
      int state = helper.getPlayer().getPlaybackState();
      if (state >= 2) {
        Format[] formats = getCurrentFormats(helper.getPlayer());
        format.setText(formats != null ? Arrays.toString(formats) : "Format: NULL");
      }
    }
  };

  SimpleExoPlayerViewHolder(View itemView) {
    super(itemView);
  }

  @Override
  public void bind(@NonNull RecyclerView.Adapter adapter, Object item, List<Object> payloads) {
    if (item != null && item instanceof MediaItem) {
      mediaUri = ((MediaItem) item).getMediaUrl().getUri();
      content.setText(item.toString());
    }
  }

  @NonNull @Override public View getPlayerView() {
    return playerView;
  }

  @NonNull @Override public PlaybackInfo getCurrentPlaybackInfo() {
    PlaybackInfo state = new PlaybackInfo();
    if (helper != null) state = helper.getPlaybackInfo();
    return state;
  }

  @Override
  public void initialize(@NonNull Container container, @NonNull PlaybackInfo playbackInfo) {
    if (helper == null) {
      if (mediaUri != null) {
        helper = new SimpleExoPlayerViewHelper(container, this, mediaUri);
        helper.setEventListener(eventListener);
        helper.initialize(playbackInfo);
      }
    }
  }

  @Override public void release() {
    if (helper != null) {
      helper.setEventListener(null);
      try {
        helper.cancel();
      } catch (Exception e) {
        e.printStackTrace();
      }
      helper = null;
    }
  }

  @Override public void play() {
    if (helper != null) helper.play();
  }

  @Override public void pause() {
    if (helper != null) helper.pause();
  }

  @Override public boolean isPlaying() {
    return helper != null && helper.isPlaying();
  }

  @Override public boolean wantsToPlay() {
    ViewParent parent = itemView.getParent();
    float visible = parent != null && parent instanceof Container ? //
        ToroUtil.visibleAreaOffset(playerView, (Container) parent) : 0;
    return visible >= 0.85;
  }

  @Override public int getPlayerOrder() {
    return getAdapterPosition();
  }

  @Nullable static Format[] getCurrentFormats(@NonNull ExoPlayer player) {
    Format[] formats = null;
    TrackSelectionArray selectionArray = player.getCurrentTrackSelections();
    if (selectionArray != null && selectionArray.length > 0) {
      formats = new Format[selectionArray.length];
      for (int i = 0; i < selectionArray.length; i++) {
        if (selectionArray.get(i) != null) formats[i] = selectionArray.get(i).getSelectedFormat();
      }
    }

    return formats;
  }

  @Override public String toString() {
    return "Player{" + hashCode() + " " + getAdapterPosition() + " " + isPlaying() + "}";
  }
}
