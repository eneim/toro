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

package im.ene.lab.toro.ext.layeredvideo;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.accessibility.CaptioningManager;
import android.widget.FrameLayout;
import com.google.android.exoplayer.text.CaptionStyleCompat;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.SubtitleLayout;
import com.google.android.exoplayer.util.Util;
import im.ene.lab.toro.ext.R;
import im.ene.lab.toro.player.internal.ExoMediaPlayer;
import java.util.List;

/**
 * Creates a view which displays subtitles.
 */
public class SubtitleLayer implements Layer, ExoMediaPlayer.CaptionListener {

  /**
   * The text view that displays the subtitles.
   */
  private SubtitleLayout subtitles;

  /**
   * The view that is created by this layer (it contains SubtitleLayer#subtitles).
   */
  private FrameLayout view;

  @Override public FrameLayout createView(LayerManager manager) {
    view = (FrameLayout) LayoutInflater.from(manager.getContainer().getContext())
        .inflate(R.layout.tr_ext_subtitle_layer, manager.getContainer(), false);
    subtitles = (SubtitleLayout) view.findViewById(R.id.subtitles);
    configureSubtitleView();
    manager.getExoPlayer().setCaptionListener(this);
    return view;
  }

  @Override public void onLayerDisplayed(LayerManager layerManager) {
    // Do nothing.
  }

  /**
   * Show or hide the subtitles.
   *
   * @param visibility One of {@link android.view.View#INVISIBLE},
   * {@link android.view.View#VISIBLE}, {@link android.view.View#GONE}.
   */
  public void setVisibility(int visibility) {
    view.setVisibility(visibility);
  }

  /**
   * When subtitles arrive, display them in the text view.
   *
   * @param cues The subtitles that must be displayed.
   */
  @Override public void onCues(List<Cue> cues) {
    this.subtitles.setCues(cues);
  }

  private void configureSubtitleView() {
    CaptionStyleCompat style;
    float fontScale;
    if (Util.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      style = getUserCaptionStyleV19();
      fontScale = getUserCaptionFontScaleV19();
    } else {
      style = CaptionStyleCompat.DEFAULT;
      fontScale = 1.0f;
    }
    subtitles.setStyle(style);
    subtitles.setFractionalTextSize(SubtitleLayout.DEFAULT_TEXT_SIZE_FRACTION * fontScale);
  }

  // Target API 19
  @TargetApi(Build.VERSION_CODES.KITKAT) private float getUserCaptionFontScaleV19() {
    CaptioningManager captioningManager =
        (CaptioningManager) subtitles.getContext().getSystemService(Context.CAPTIONING_SERVICE);
    return captioningManager.getFontScale();
  }

  // Target API 19
  @TargetApi(Build.VERSION_CODES.KITKAT) private CaptionStyleCompat getUserCaptionStyleV19() {
    CaptioningManager captioningManager =
        (CaptioningManager) subtitles.getContext().getSystemService(Context.CAPTIONING_SERVICE);
    return CaptionStyleCompat.createFromCaptionStyle(captioningManager.getUserStyle());
  }
}
