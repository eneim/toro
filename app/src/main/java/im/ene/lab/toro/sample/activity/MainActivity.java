package im.ene.lab.toro.sample.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.fragment.SampleVideoListFragment;
import im.ene.lab.toro.sample.fragment.SingleVideoListFragment;
import im.ene.lab.toro.sample.fragment.StaggeredVideoListFragment;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private ViewPager mPager;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mPager = (ViewPager) findViewById(R.id.view_pager);

    ArrayList<Fragment> items = new ArrayList<>();
    items.add(StaggeredVideoListFragment.newInstance());
    items.add(SampleVideoListFragment.newInstance());
    items.add(SingleVideoListFragment.newInstance());

    ArrayList<String> titles = new ArrayList<>();
    titles.add(StaggeredVideoListFragment.class.getSimpleName());
    titles.add(SampleVideoListFragment.class.getSimpleName());
    titles.add(SingleVideoListFragment.class.getSimpleName());

    ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, items);
    mPager.setAdapter(adapter);
  }

  private static class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> mItems;
    private final List<String> mTitles;

    public ViewPagerAdapter(FragmentManager fm,
        ArrayList<String> titles,
        ArrayList<Fragment> items) {
      super(fm);
      this.mTitles = titles;
      this.mItems = items;
    }

    @Override public Fragment getItem(int position) {
      return mItems.get(position);
    }

    @Override public int getCount() {
      return Math.min(mItems.size(), mTitles.size());
    }

    @Override public CharSequence getPageTitle(int position) {
      return mTitles.get(position);
    }
  }
}
