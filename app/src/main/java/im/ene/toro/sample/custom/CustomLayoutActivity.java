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

package im.ene.toro.sample.custom;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.ene.toro.PlayerSelector;
import im.ene.toro.sample.MainActivity;
import im.ene.toro.sample.R;
import im.ene.toro.sample.common.BaseActivity;
import im.ene.toro.widget.Container;

/**
 * @author eneim (7/2/17).
 */

public class CustomLayoutActivity extends BaseActivity implements CustomLayoutFragment.Callback {

  @BindView(R.id.drawer_layout) DrawerLayout drawer;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.nav_view) NavigationView navView;

  ActionBarDrawerToggle drawerToggle;

  DrawerLayout.DrawerListener containerToggle;
  Container container;
  PlayerSelector selector;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    ButterKnife.bind(this);
    setSupportActionBar(toolbar);
    setTitle(R.string.app_name);

    drawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, //
        R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(drawerToggle);

    containerToggle = new DrawerLayout.SimpleDrawerListener() {
      @Override public void onDrawerOpened(View drawerView) {
        if (container != null) container.setPlayerSelector(PlayerSelector.NONE);
      }

      @Override public void onDrawerClosed(View drawerView) {
        if (container != null && selector != null) container.setPlayerSelector(selector);
      }
    };

    drawer.addDrawerListener(containerToggle);

    navView.inflateHeaderView(R.layout.widget_space_4dp);
    navView.inflateHeaderView(R.layout.widget_hom_nav_header_1);
    navView.inflateHeaderView(R.layout.widget_hom_nav_header_2);
    navView.getHeaderView(3).<TextView>findViewById(R.id.text_content).setText(
        Html.fromHtml(getString(R.string.lib_info_license)));

    setupDemoButton();

    if (savedInstanceState == null) {
      CustomLayoutFragment fragment = CustomLayoutFragment.newInstance();
      getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
    }
  }

  // It is said that this method should not be used at Application.
  // In fact, only here we can ensure that Activity's Views have finished restoring its state.
  // In this case, the drawer will be re-open after recreation IF it was opened before. But before
  // this method is called (eg. onStart), state of navView is still 'closed'
  // (drawer.isDrawerOpen(navView) returns false), which is unexpected.
  // With that in mind, the "syncState" call should also be called here as well.
  @Override protected void onPostCreate(@Nullable Bundle bundle) {
    super.onPostCreate(bundle);
    drawerToggle.syncState();
    //noinspection ConstantConditions
    // check drawer state after recreation, and pause playback if need.
    if (this.container != null && drawer.isDrawerOpen(navView)) {
      this.container.setPlayerSelector(PlayerSelector.NONE);
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    drawer.removeDrawerListener(drawerToggle);
    drawer.removeDrawerListener(containerToggle);
    container = null;
    selector = null;
    drawerToggle = null;
    containerToggle = null;
  }

  private void setupDemoButton() {
    View buttonContainer = toolbar.findViewById(R.id.home_toolbar_button);
    if (buttonContainer == null) {
      buttonContainer = LayoutInflater.from(toolbar.getContext())
          .inflate(R.layout.widget_toolbar_button, toolbar, false);
      buttonContainer.setId(R.id.home_toolbar_button);

      ActionBar.LayoutParams params = (ActionBar.LayoutParams) buttonContainer.getLayoutParams();
      params.gravity = GravityCompat.END;
      toolbar.addView(buttonContainer, params);
    }

    buttonContainer.findViewById(R.id.button_open_demos)
        .setOnClickListener(__ -> startActivity(new Intent(this, MainActivity.class)));
  }

  @Override public void onContainerAvailable(@NonNull Container container) {
    this.container = container;
    this.selector = container.getPlayerSelector();
  }
}
