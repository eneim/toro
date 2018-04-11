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

import com.google.android.youtube.player.YouTubePlayerSupportFragment;

/**
 * @author eneim (2017/12/07).
 */

public class ToroYouTubePlayerFragment extends YouTubePlayerSupportFragment {

  // This is used by the Adapter's FragmentManager to know which Helper request the Fragment.
  private YouTubePlayerHelper helperKey;

  /* package */ YouTubePlayerHelper getHelperKey() {
    return helperKey;
  }

  /* package */ void setHelperKey(YouTubePlayerHelper helperKey) {
    this.helperKey = helperKey;
  }

  public static ToroYouTubePlayerFragment newInstance() {
    return new ToroYouTubePlayerFragment();
  }

  @Override public String toString() {
    return "Toro:Yt:Fragment{" + "helper=" + helperKey + '}';
  }
}
