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
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.helper.LegacyVideoViewHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.sample.R;
import im.ene.toro.sample.data.MediaItem;
import im.ene.toro.widget.Container;
import im.ene.toro.widget.ToroVideoView;
import java.util.List;

/**
 * @author eneim | 6/6/17.
 */

public class VideoViewHolder extends BaseViewHolder implements ToroPlayer {

  private static final String TAG = "Toro:VH:ExoPlayer";

  static final int LAYOUT_RES = R.layout.vh_videoview_basic;

  private EventListener eventListener = new EventListener() {
    @Override public void onBuffering() {
      state.setText("BUFFERING");
    }

    @Override public void onPlaying() {
      state.setText("PLAYING");
    }

    @Override public void onPaused() {
      state.setText("PAUSED");
    }

    @Override public void onCompleted(Container container, ToroPlayer player) {
      state.setText("COMPLETED");
    }
  };

  @SuppressWarnings("WeakerAccess") @Nullable LegacyVideoViewHelper helper;
  @Nullable private Uri mediaUri;

  @BindView(R.id.player) ToroVideoView playerView;
  public @BindView(R.id.text_content) Button content;
  @BindView(R.id.player_state) TextView state;
  @BindView(R.id.player_format) TextView format;

  VideoViewHolder(View itemView) {
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

  @Override public void prepare(@NonNull Container container, @NonNull PlaybackInfo playbackInfo) {
    if (helper == null) {
      if (mediaUri != null) {
        helper = new LegacyVideoViewHelper(container, this, mediaUri);
        helper.setOnPreparedListener(__ -> state.setText("PREPARED"));
        helper.addPlayerEventListener(eventListener);
        try {
          helper.initialize(playbackInfo);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override public void release() {
    if (helper != null) {
      helper.setOnPreparedListener(null);
      helper.removePlayerEventListener(eventListener);
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
}
