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

package im.ene.lab.toro.sample.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.ene.lab.toro.Toro;
import im.ene.lab.toro.ToroStrategy;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.ToroSampleApp;
import im.ene.lab.toro.sample.facebook.FbFeedFragment;
import im.ene.lab.toro.sample.fragment.DeadlySimpleListFragment;
import im.ene.lab.toro.sample.fragment.DualVideoListFragment;
import im.ene.lab.toro.sample.fragment.MultiVideoComplicatedGridFragment;
import im.ene.lab.toro.sample.fragment.MultiVideoStaggeredGridFragment;
import im.ene.lab.toro.sample.fragment.SimpleToggleableListFragment;
import im.ene.lab.toro.sample.fragment.SimpleVideoListFragment;
import im.ene.lab.toro.sample.fragment.SingleVideoSimpleListFragment;
import im.ene.lab.toro.sample.fragment.ViewPagerFragment;
import im.ene.lab.toro.sample.fragment.YoutubeListFragment;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

  @Bind(R.id.content) View mContent;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRequestedOrientation(getResources().getBoolean(R.bool.is_large_screen) ? //
        ActivityInfo.SCREEN_ORIENTATION_USER : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle =
        new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    int lastSelected = ToroSampleApp.pref().getInt(PREF_KEY_STRATEGY, 0);
    Toro.setStrategy(mStrategies.get(lastSelected));
  }

  @Override public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @SuppressWarnings("StatementWithEmptyBody") @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    if (id == R.id.nav_strategy) {
      // Setup strategy
      setupStrategy();
    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @OnClick(R.id.btn_simple_video_list) void simpleVideoList() {
    startActivity(ShowCaseActivity.createIntent(this, SimpleVideoListFragment.TAG));
  }

  @OnClick(R.id.btn_single_video_simple_list) void singleVideoSimpleList() {
    startActivity(ShowCaseActivity.createIntent(this, SingleVideoSimpleListFragment.TAG));
  }

  @OnClick(R.id.btn_multi_video_staggered_grid) void multiVideoStaggeredGrid() {
    startActivity(ShowCaseActivity.createIntent(this, MultiVideoStaggeredGridFragment.TAG));
  }

  @OnClick(R.id.btn_multi_video_complicated_grid) void multiVideoComplicatedGrid() {
    startActivity(ShowCaseActivity.createIntent(this, MultiVideoComplicatedGridFragment.TAG));
  }

  @OnClick(R.id.btn_multi_video_dual_list) void multiVideoDualList() {
    startActivity(ShowCaseActivity.createIntent(this, DualVideoListFragment.TAG));
  }

  @OnClick(R.id.btn_toggleable_list) void toggleAbleList() {
    startActivity(ShowCaseActivity.createIntent(this, SimpleToggleableListFragment.TAG));
  }

  @OnClick(R.id.btn_deadly_simple_list) void deadlySimpleVideoList() {
    startActivity(ShowCaseActivity.createIntent(this, DeadlySimpleListFragment.TAG));
  }

  @OnClick(R.id.btn_youtube_video_list) void youtubeVideoList() {
    startActivity(ShowCaseActivity.createIntent(this, YoutubeListFragment.TAG));
  }

  @OnClick(R.id.btn_view_pager) void viewPager() {
    startActivity(ShowCaseActivity.createIntent(this, ViewPagerFragment.TAG));
  }

  @OnClick(R.id.btn_facebook_feed) void facebookFeed() {
    startActivity(ShowCaseActivity.createIntent(this, FbFeedFragment.TAG));
  }

  private static final List<ToroStrategy> mStrategies;

  static {
    mStrategies = new ArrayList<>();
    mStrategies.add(Toro.Strategies.FIRST_PLAYABLE_TOP_DOWN);
    mStrategies.add(Toro.Strategies.FIRST_PLAYABLE_TOP_DOWN_KEEP_LAST);
    mStrategies.add(Toro.Strategies.MOST_VISIBLE_TOP_DOWN);
    mStrategies.add(Toro.Strategies.MOST_VISIBLE_TOP_DOWN_KEEP_LAST);
  }

  private static final String TAG = "MainActivity";

  private static final String PREF_KEY_STRATEGY = "pref_playback_strategy_position";

  private void setupStrategy() {
    final int lastSelected = ToroSampleApp.pref().getInt(PREF_KEY_STRATEGY, 0);
    new AlertDialog.Builder(this).setSingleChoiceItems(
        new StrategyAdapter(this, android.R.layout.simple_list_item_single_choice), lastSelected,
        new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            ToroSampleApp.pref().edit().putInt(PREF_KEY_STRATEGY, which).apply();
            Toro.setStrategy(mStrategies.get(which));
            dialog.dismiss();
          }
        })
        .setTitle("Playback Strategies")
        .setOnDismissListener(new DialogInterface.OnDismissListener() {
          @Override public void onDismiss(DialogInterface dialog) {
            Snackbar.make(mContent, "Strategy: " + Toro.getStrategy().getDescription(),
                Snackbar.LENGTH_LONG).setAction(android.R.string.ok, new View.OnClickListener() {
              @Override public void onClick(View v) {
                // Do nothing
              }
            }).show();
          }
        })
        .create()
        .show();
  }

  private static class StrategyAdapter extends ArrayAdapter<ToroStrategy> {

    private LayoutInflater inflater;
    private int resource; // MUST be TextView layout resource

    public StrategyAdapter(Context context, int resource) {
      super(context, resource, mStrategies);
      this.resource = resource;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
      if (inflater == null) {
        inflater = LayoutInflater.from(parent.getContext());
      }

      View view;
      TextView text;

      if (convertView == null) {
        view = inflater.inflate(resource, parent, false);
      } else {
        view = convertView;
      }

      try {
        text = (TextView) view;
      } catch (ClassCastException e) {
        Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
        throw new IllegalStateException("Hmm", e);
      }

      ToroStrategy item = getItem(position);
      text.setText(item.getDescription());

      return view;
    }
  }
}
