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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import im.ene.toro.sample.R;
import im.ene.toro.sample.common.BaseFragment;

/**
 * @author eneim (7/27/17).
 */

public class MainPageFragment extends BaseFragment {

  public static MainPageFragment newInstance() {
    Bundle args = new Bundle();
    MainPageFragment fragment = new MainPageFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @BindView(R.id.tab_layout) TabLayout tabLayout;
  @BindView(R.id.pager) ViewPager viewPager;
  SubPagerAdapter pagerAdapter;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle bundle) {
    return inflater.inflate(R.layout.fragment_viewpager, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    pagerAdapter = new SubPagerAdapter(getChildFragmentManager());
    viewPager.setAdapter(pagerAdapter);
    tabLayout.setupWithViewPager(viewPager);
  }

  Boolean userFlag = null;

  @Override public void onViewStateRestored(@Nullable Bundle bundle) {
    super.onViewStateRestored(bundle);
    // Only called on first View creation/recreation.
    if (userFlag != null) {
      pagerAdapter.setUserVisibleHint(userFlag);
      userFlag = null;
    }
  }

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (pagerAdapter != null) {
      pagerAdapter.setUserVisibleHint(isVisibleToUser);
      userFlag = null;
    } else {
      userFlag = isVisibleToUser;
    }
  }
}
