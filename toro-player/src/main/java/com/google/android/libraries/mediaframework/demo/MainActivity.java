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

package com.google.android.libraries.mediaframework.demo;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.libraries.mediaframework.demo.adplayer.ImaPlayer;
import com.google.android.libraries.mediaframework.exoplayerextensions.Video;
import com.google.android.libraries.mediaframework.layeredvideo.PlaybackControlLayer;
import im.ene.lab.toro.player.R;

/**
 * Displays a list of videos and plays them when they are selected.
 */
public class MainActivity extends Activity implements PlaybackControlLayer.FullscreenCallback {

  /**
   * The player which will be used to play the content videos and the ads.
   */
  private ImaPlayer imaPlayer;

  /**
   * The {@link FrameLayout} that will contain the video player.
   */
  private FrameLayout videoPlayerContainer;

  /**
   * The list of the videos.
   */
  private ListView videoListView;

  /**
   * Set up the view and populate the list of the videos.
   * @param savedInstanceState The bundle which contains saved state - it is ignored.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View view = getLayoutInflater().inflate(R.layout.ads_activity_main, null);

    // Get rid of the action bar.
    ActionBar actionBar = getActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    // This container will be the video player.
    videoPlayerContainer = (FrameLayout) view.findViewById(R.id.video_frame);

    // Lists the available videos.
    videoListView = (ListView) view.findViewById(R.id.video_list_view);

    // Retrieve the list of available videos.
    final VideoListItem[] videoListItems = getVideoListItems();

    // Extract the titles of the videos and put them into this array.
    final String[] videoTitles = new String[videoListItems.length];

    for (int i = 0; i < videoListItems.length; i++) {
      videoTitles[i] = videoListItems[i].title;
    }

    // Create a ListView of the available videos titles.
    videoListView.setAdapter(new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1,
        videoTitles));

    // When a video is selected, create the ImaPlayer and play the video.
    videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        createImaPlayer(videoListItems[i]);
      }
    });

    setContentView(view);
  }

  /**
   * Release the video player when the activity is destroyed.
   */
  @Override
  protected void onDestroy() {
    if (imaPlayer != null) {
      imaPlayer.release();
    }
    super.onDestroy();
  }

  /**
   * When a video has been selected, create an {@link ImaPlayer} and play the video.
   */
  public void createImaPlayer(VideoListItem videoListItem) {
    if (imaPlayer != null) {
      imaPlayer.release();
    }

    // If there was previously a video player in the container, remove it.
    videoPlayerContainer.removeAllViews();

    String adTagUrl = videoListItem.adUrl;
    String videoTitle = videoListItem.title;

    imaPlayer = new ImaPlayer(this,
        videoPlayerContainer,
        videoListItem.video,
        videoTitle,
        adTagUrl);
    imaPlayer.setFullscreenCallback(this);

    Resources res = getResources();

    // Customize the UI of the video player.

    // Set a logo (an Android icon will be displayed in the top left)
    Drawable logo = res.getDrawable(R.drawable.gmf_icon);
    imaPlayer.setLogoImage(logo);

    // Uncomment the following lines to set the color of the player's top chrome, bottom chrome, and
    // background to be a blue color.

    // int sampleChromeColor = res.getColor(R.color.sample_chrome_color);
    // imaPlayer.setChromeColor(sampleChromeColor);

    // Uncomment the following lines to set the color of the buttons and seekbar in the player
    // to be a green color.

    // int samplePlaybackControlColor = res.getColor(R.color.sample_playback_control_color);
    // imaPlayer.setPlaybackControlColor(samplePlaybackControlColor);

    // Add three buttons to the video player's set of action buttons.
    //
    // When the player is not fullscreen, there will be an overflow button in the top right of the
    // video player's playback control UI. When the overflow button is clicked, a dialog box
    // will appear listing the possible actions (in this case, "Option 1", "Option 2",
    // and "Option 3").
    //
    // When the player is in fullscreen, each of the buttons' icons (in this case, share, discard,
    // and favorite) will be displayed in the top right of the video player's playback control UI.
    //
    // When an action is triggered (either by clicking it in the dialog box when the video player is
    // not in fullscreen or by clicking its corresponding button when the video player is in
    // fullscreen), it will display a toast message.
    imaPlayer.addActionButton(
        res.getDrawable(R.drawable.ic_action_share),
        getString(R.string.option1),
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            Toast.makeText(MainActivity.this,
                getString(R.string.clicked_option_1),
                Toast.LENGTH_SHORT)
                .show();
          }
        }
    );
    imaPlayer.addActionButton(
        getResources().getDrawable(R.drawable.ic_action_discard),
        getString(R.string.option2),
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            Toast.makeText(MainActivity.this,
                getString(R.string.clicked_option_2),
                Toast.LENGTH_SHORT)
                .show();
          }
        }
    );
    imaPlayer.addActionButton(
        getResources().getDrawable(R.drawable.ic_action_favorite),
        getString(R.string.option3),
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            Toast.makeText(MainActivity.this,
                getString(R.string.clicked_option_3),
                Toast.LENGTH_SHORT)
                .show();
          }
        });

    // Now that the player is set up, let's start playing.
    imaPlayer.play();
  }

  /**
   * Create a list of videos and their associated metadata.
   * @return A list of videos (and their titles, content URLs, media types, content ID, and ad tag).
   */
  public VideoListItem[] getVideoListItems() {
    return new VideoListItem[] {
        new VideoListItem("No ads (DASH)",
            new Video("http://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtub" +
                "e?as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=" +
                "0.0.0.0&ipbits=0&expire=19000000000&signature=51AF5F39AB0CEC3E5497CD9C900EBFEAEC" +
                "CCB5C7.8506521BFC350652163895D4C26DEE124209AA9E&key=ik0",
                Video.VideoType.DASH,
                "bf5bb2419360daf1"),
            null),
        new VideoListItem("Skippable preroll (DASH)",
            new Video("http://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtub" +
                "e?as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=" +
                "0.0.0.0&ipbits=0&expire=19000000000&signature=51AF5F39AB0CEC3E5497CD9C900EBFEAEC" +
                "CCB5C7.8506521BFC350652163895D4C26DEE124209AA9E&key=ik0",
                Video.VideoType.DASH,
                "bf5bb2419360daf1"),
            "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/" +
            "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast" +
            "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct" +
            "%3Dskippablelinear&correlator="),
        new VideoListItem("Unskippable preroll (DASH)",
            new Video("http://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtub" +
                "e?as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=" +
                "0.0.0.0&ipbits=0&expire=19000000000&signature=51AF5F39AB0CEC3E5497CD9C900EBFEAEC" +
                "CCB5C7.8506521BFC350652163895D4C26DEE124209AA9E&key=ik0",
                Video.VideoType.DASH,
                "bf5bb2419360daf1"),
            "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/" +
            "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast" +
            "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct" +
            "%3Dlinear&correlator="),
        new VideoListItem("Ad rules - Pre-, Mid-, and Post-rolls (DASH)",
            new Video("http://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtub" +
                "e?as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=" +
                "0.0.0.0&ipbits=0&expire=19000000000&signature=51AF5F39AB0CEC3E5497CD9C900EBFEAEC" +
                "CCB5C7.8506521BFC350652163895D4C26DEE124209AA9E&key=ik0",
                Video.VideoType.DASH,
                "bf5bb2419360daf1"),
            "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/" +
            "ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp" +
            "&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite" +
            "%26sample_ar%3Dpremidpostpod&cmsid=496&vid=short_onecue&correlator="),
        new VideoListItem("No ads (mp4)",
            new Video("http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4",
                Video.VideoType.MP4),
            null),
        new VideoListItem("No ads - BBB (HLS)",
            new Video("http://googleimadev-vh.akamaihd.net/i/big_buck_bunny/bbb-,480p,720p,1080p" +
                ",.mov.csmil/master.m3u8",
                Video.VideoType.HLS),
            null),
        new VideoListItem("Ad rules - Apple test (HLS)",
            new Video("https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/" +
                "bipbop_4x3_variant.m3u8 ",
                Video.VideoType.HLS),
            "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/" +
            "ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp" +
            "&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite" +
            "%26sample_ar%3Dpremidpostpod&cmsid=496&vid=short_onecue&correlator="),
    };
  }

  /**
   * When the video player goes into fullscreen, hide the video list so that the video player can
   * occupy the entire screen.
   */
  @Override
  public void onGoToFullscreen() {
    videoListView.setVisibility(View.INVISIBLE);
  }

  /**
   * When the player returns from fullscreen, show the video list again.
   */
  @Override
  public void onReturnFromFullscreen() {
    videoListView.setVisibility(View.VISIBLE);
  }

  /**
   * Simple class to bundle together the title, content video, and ad tag associated with a video.
   */
  public static class VideoListItem {

    /**
     * The title of the video.
     */
    public final String title;

    /**
     * The actual content video (contains its URL, media type - either DASH or mp4,
     * and an optional media type).
     */
    public final Video video;

    /**
     * The URL of the VAST document which represents the ad.
     */
    public final String adUrl;

    /**
     * @param title The title of the video.
     * @param video The actual content video (contains its URL, media type - either DASH or mp4,
     *                  and an optional media type).
     * @param adUrl The URL of the VAST document which represents the ad.
     */
    public VideoListItem(String title, Video video, String adUrl) {
      this.title = title;
      this.video = video;
      this.adUrl = adUrl;
    }
  }


}
