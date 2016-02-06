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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.fragment.MultiVideoComplicatedGridFragment;
import im.ene.lab.toro.sample.fragment.MultiVideoStaggeredGridFragment;
import im.ene.lab.toro.sample.fragment.SingleVideoSimpleListFragment;

public class HomeActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    ButterKnife.bind(this);
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
}
