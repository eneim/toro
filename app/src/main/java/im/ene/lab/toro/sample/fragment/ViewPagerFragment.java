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

package im.ene.lab.toro.sample.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.Bind;
import butterknife.ButterKnife;
import im.ene.lab.toro.sample.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eneim on 2/7/16.
 */
public class ViewPagerFragment extends Fragment {

  public static final String TAG = "ViewPagerFragment";

  public static ViewPagerFragment newInstance() {
    return new ViewPagerFragment();
  }

  @Bind(R.id.viewpager) ViewPager mViewPager;

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_viewpager, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);
    List<Fragment> fragments = new ArrayList<>();
    fragments.add(MultiVideoStaggeredGridFragment.newInstance());
    fragments.add(SingleVideoSimpleListFragment.newInstance());

    ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager(), fragments);
    mViewPager.setAdapter(adapter);
  }

  private static class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> mItems;

    public ViewPagerAdapter(FragmentManager fm, @NonNull List<Fragment> items) {
      super(fm);
      mItems = items;
    }

    @Override public Fragment getItem(int position) {
      return mItems.get(position);
    }

    @Override public int getCount() {
      return mItems.size();
    }
  }
}
