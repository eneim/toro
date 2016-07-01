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

package im.ene.lab.toro.sample.develop.standalone;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.util.MimeTypes;
import im.ene.lab.toro.ext.layeredvideo.SimpleVideoPlayer;
import im.ene.lab.toro.player.Video;
import im.ene.lab.toro.player.internal.ExoMediaPlayer;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.util.Util;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

/**
 * Created by eneim on 6/3/16.
 */
public class StandalonePlayerActivity extends AppCompatActivity {

  // For use within demo app code.
  public static final String CONTENT_ID_EXTRA = "content_id";
  public static final String CONTENT_TYPE_EXTRA = "content_type";
  public static final String PROVIDER_EXTRA = "provider";

  // For use when launching the demo app using adb.
  private static final String CONTENT_EXT_EXTRA = "type";

  private static final String TAG = "PlayerActivity";
  private static final int MENU_GROUP_TRACKS = 1;
  private static final int ID_OFFSET = 2;

  private FrameLayout playerContainer;

  private boolean playRequested = true;

  SimpleVideoPlayer videoPlayer;

  private boolean targetShowUI = true;
  private static final int MSG_UI_SHOW_HIDE = -2;
  private static final int UI_SHOW_HIDE_DELAY_MS = 1500;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // If the Android version is lower than Jellybean, use this call to hide
    // the status bar.
    setContentView(R.layout.sample_videoplayer);
    playerContainer = (FrameLayout) findViewById(R.id.player_container);

    try {
      File[] local = Util.loadMovieFolder();
      videoPlayer = new SimpleVideoPlayer(this, playerContainer, new Video(
          local != null && local.length > 0 ? Uri.fromFile(Util.loadMovieFolder()[0])
              : Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"), "Garden of Words"),
          "Garden of Words", true);
      videoPlayer.setVideoTitle("Garden of Words");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    videoPlayer.setFullscreenCallback(null);
    videoPlayer.enableSeeking();

    Toolbar toolbar = videoPlayer.getActionToolbar();
    if (toolbar != null) {
      setSupportActionBar(videoPlayer.getActionToolbar());
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
  }

  @Override protected void onResume() {
    super.onResume();
    // DisplayHelper.setFullScreen(this, true);
    videoPlayer.play();
  }

  @Override public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    videoPlayer.notifyWindowFocusChanged(hasFocus);
  }

  @Override protected void onPause() {
    super.onPause();
    videoPlayer.pause();
    // uiShowHideHandler.removeMessages(MSG_UI_SHOW_HIDE);
  }

  // Addition methods

  public void showVideoPopup(View v) {
    PopupMenu popup = new PopupMenu(this, v);
    configurePopupWithTracks(popup, null, ExoMediaPlayer.TYPE_VIDEO);
    popup.show();
  }

  boolean enableBackgroundAudio = false;

  public void showAudioPopup(View v) {
    PopupMenu popup = new PopupMenu(this, v);
    Menu menu = popup.getMenu();
    menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.enable_background_audio);
    final MenuItem backgroundAudioItem = menu.findItem(0);
    backgroundAudioItem.setCheckable(true);
    backgroundAudioItem.setChecked(enableBackgroundAudio);
    PopupMenu.OnMenuItemClickListener clickListener = new PopupMenu.OnMenuItemClickListener() {
      @Override public boolean onMenuItemClick(MenuItem item) {
        if (item == backgroundAudioItem) {
          enableBackgroundAudio = !item.isChecked();
          return true;
        }
        return false;
      }
    };
    configurePopupWithTracks(popup, clickListener, ExoMediaPlayer.TYPE_AUDIO);
    popup.show();
  }

  public void showTextPopup(View v) {
    PopupMenu popup = new PopupMenu(this, v);
    configurePopupWithTracks(popup, new PopupMenu.OnMenuItemClickListener() {
      @Override public boolean onMenuItemClick(MenuItem item) {
        return false;
      }
    }, ExoMediaPlayer.TYPE_TEXT);
    popup.show();
  }

  private void configurePopupWithTracks(PopupMenu popup,
      final PopupMenu.OnMenuItemClickListener customActionClickListener, final int trackType) {
    if (videoPlayer == null || videoPlayer.getPlayer() == null) {
      return;
    }
    int trackCount = videoPlayer.getPlayer().getTrackCount(trackType);
    if (trackCount == 0) {
      return;
    }
    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override public boolean onMenuItemClick(MenuItem item) {
        return (customActionClickListener != null && customActionClickListener.onMenuItemClick(
            item)) || onTrackItemClick(item, trackType);
      }
    });
    Menu menu = popup.getMenu();
    // ID_OFFSET ensures we avoid clashing with Menu.NONE (which equals 0).
    menu.add(MENU_GROUP_TRACKS, ExoMediaPlayer.TRACK_DISABLED + ID_OFFSET, Menu.NONE, R.string.off);
    for (int i = 0; i < trackCount; i++) {
      menu.add(MENU_GROUP_TRACKS, i + ID_OFFSET, Menu.NONE,
          buildTrackName(videoPlayer.getPlayer().getTrackFormat(trackType, i)));
    }
    menu.setGroupCheckable(MENU_GROUP_TRACKS, true, true);
    menu.findItem(videoPlayer.getPlayer().getSelectedTrack(trackType) + ID_OFFSET).setChecked(true);
  }

  private static String buildTrackName(MediaFormat format) {
    if (format.adaptive) {
      return "auto";
    }
    String trackName;
    if (MimeTypes.isVideo(format.mimeType)) {
      trackName = joinWithSeparator(
          joinWithSeparator(buildResolutionString(format), buildBitrateString(format)),
          buildTrackIdString(format));
    } else if (MimeTypes.isAudio(format.mimeType)) {
      trackName = joinWithSeparator(joinWithSeparator(
          joinWithSeparator(buildLanguageString(format), buildAudioPropertyString(format)),
          buildBitrateString(format)), buildTrackIdString(format));
    } else {
      trackName = joinWithSeparator(
          joinWithSeparator(buildLanguageString(format), buildBitrateString(format)),
          buildTrackIdString(format));
    }
    return trackName.length() == 0 ? "unknown" : trackName;
  }

  private static String buildResolutionString(MediaFormat format) {
    return format.width == MediaFormat.NO_VALUE || format.height == MediaFormat.NO_VALUE ? ""
        : format.width + "x" + format.height;
  }

  private boolean onTrackItemClick(MenuItem item, int type) {
    if (videoPlayer.getPlayer() == null || item.getGroupId() != MENU_GROUP_TRACKS) {
      return false;
    }
    videoPlayer.getPlayer().setSelectedTrack(type, item.getItemId() - ID_OFFSET);
    return true;
  }

  private static String buildAudioPropertyString(MediaFormat format) {
    return format.channelCount == MediaFormat.NO_VALUE || format.sampleRate == MediaFormat.NO_VALUE
        ? "" : format.channelCount + "ch, " + format.sampleRate + "Hz";
  }

  private static String buildLanguageString(MediaFormat format) {
    return TextUtils.isEmpty(format.language) || "und".equals(format.language) ? ""
        : format.language;
  }

  private static String buildBitrateString(MediaFormat format) {
    return format.bitrate == MediaFormat.NO_VALUE ? ""
        : String.format(Locale.US, "%.2fMbit", format.bitrate / 1000000f);
  }

  private static String joinWithSeparator(String first, String second) {
    return first.length() == 0 ? second : (second.length() == 0 ? first : first + ", " + second);
  }

  private static String buildTrackIdString(MediaFormat format) {
    return format.trackId == null ? "" : " (" + format.trackId + ")";
  }

  // Internal/Develop/Experiment API
}
