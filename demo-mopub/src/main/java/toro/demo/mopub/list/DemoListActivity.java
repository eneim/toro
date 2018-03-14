/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
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

package toro.demo.mopub.list;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.CoordinatorLayout.LayoutParams;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import im.ene.toro.widget.Container;
import toro.demo.mopub.R;

public class DemoListActivity extends AppCompatActivity {

  Container container;
  DemoListAdapter adapter;
  RecyclerView.LayoutManager layoutManager;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_demo_list);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    container = findViewById(R.id.container);
    layoutManager = new LinearLayoutManager(this);
    adapter = new DemoListAdapter();

    container.setLayoutManager(layoutManager);
    container.setAdapter(adapter);

    // Only when you use Container inside a CoordinatorLayout and depends on Behavior.
    ViewGroup.LayoutParams params = container.getLayoutParams();
    if (params != null && params instanceof LayoutParams) {
      CoordinatorLayout.Behavior behavior = ((LayoutParams) params).getBehavior();
      if (behavior != null) {
        //noinspection unchecked
        ((LayoutParams) params).setBehavior(new Container.Behavior(behavior,  //
            () -> container.onScrollStateChanged(RecyclerView.SCROLL_STATE_IDLE)));
      }
    }
  }
}
