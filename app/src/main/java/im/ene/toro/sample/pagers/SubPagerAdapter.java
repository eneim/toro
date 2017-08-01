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

package im.ene.toro.sample.pagers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;
import im.ene.toro.sample.Deck;
import im.ene.toro.sample.common.BaseFragment;
import im.ene.toro.sample.intro.IntroFragment;

/**
 * @author eneim (7/27/17).
 */

public class SubPagerAdapter extends FragmentStatePagerAdapter {

  SubPagerAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override public Fragment getItem(int position) {
    Fragment fragment = IntroFragment.newInstance();
    try {
      fragment = Deck.createFragment(Deck.Slide.values()[position + 1].getFragmentClass());
    } catch (Deck.ToroDemoException e) {
      e.printStackTrace();
    }

    if (fragment instanceof BaseFragment) ((BaseFragment) fragment).setViewPagerMode(true);
    return fragment;
  }

  @Override public int getCount() {
    return Deck.Slide.values().length - 1 /* minus the first Slide */;
  }

  private Fragment primaryItem;

  @Override public void setPrimaryItem(ViewGroup container, int position, Object object) {
    super.setPrimaryItem(container, position, object);
    if (object instanceof Fragment) primaryItem = (Fragment) object;
    if (primaryItem != null) {
      if (visible != null) {
        primaryItem.setUserVisibleHint(visible);
        visible = null;
      }
    }
  }

  private Boolean visible = null;

  void setUserVisibleHint(boolean visible) {
    if (primaryItem != null) {
      primaryItem.setUserVisibleHint(visible);
      this.visible = null;
    } else {
      this.visible = visible;
    }
  }
}
