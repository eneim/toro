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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.youtube.player.YouTubePlayerFragment;

/**
 * @author eneim (2017/12/07).
 */

public class ToroYouTubePlayerFragment extends YouTubePlayerFragment {

  // This instance lives out of the lifecycle callbacks, but must be released and cleared before
  // any destroying event (onDestroyView/onDestroy)
  private YouTubePlayerHelper playerHelper;

  /* package */ void setPlayerHelper(YouTubePlayerHelper playerHelper) {
    this.playerHelper = playerHelper;
  }

  public static ToroYouTubePlayerFragment newInstance() {
    return new ToroYouTubePlayerFragment();
  }

  @Override public String toString() {
    return "YouT:Fragment{" + "helper=" + playerHelper + '}';
  }

  @Override public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
    if (this.playerHelper != null) this.playerHelper.ytFragment = this;
    return super.onCreateView(layoutInflater, viewGroup, bundle);
  }

  @Override public void onStop() {
    super.onStop();
    // A stopped player need to be release or else it will throw error when resume. It is weird ...
    if (this.playerHelper != null) this.playerHelper.release();
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    if (this.playerHelper != null) {
      this.playerHelper.release();
      this.playerHelper.ytFragment = null;
      this.playerHelper = null;
    }
  }
}
