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
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.ene.lab.toro.Toro;
import im.ene.lab.toro.ToroStrategy;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.fragment.DeadlySimpleListFragment;
import im.ene.lab.toro.sample.fragment.DualVideoListFragment;
import im.ene.lab.toro.sample.fragment.MultiVideoComplicatedGridFragment;
import im.ene.lab.toro.sample.fragment.MultiVideoStaggeredGridFragment;
import im.ene.lab.toro.sample.fragment.SingleVideoSimpleListFragment;

public class HomeActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

  @Bind(R.id.strategies_container) RadioGroup mStrategies;

  private final ToroStrategy[] STRATEGIES = {
      Toro.Strategies.FIRST_PLAYABLE_TOP_DOWN, Toro.Strategies.FIRST_PLAYABLE_TOP_DOWN_KEEP_LAST,
      Toro.Strategies.MOST_VISIBLE_TOP_DOWN, Toro.Strategies.MOST_VISIBLE_TOP_DOWN_KEEP_LAST
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    ButterKnife.bind(this);

    mStrategies.setOnCheckedChangeListener(this);
    mStrategies.removeAllViews();

    for (ToroStrategy strategy : STRATEGIES) {
      RadioButton checkBox = (RadioButton) LayoutInflater.from(mStrategies.getContext())
          .inflate(R.layout.strategy_checkbox, mStrategies, false);
      checkBox.setText(strategy.getDescription());
      mStrategies.addView(checkBox);
      checkBox.setChecked(Toro.getStrategy() == strategy);
    }
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

  @OnClick(R.id.btn_deadly_simple_list) void deadlySimpleVideoList() {
    startActivity(ShowCaseActivity.createIntent(this, DeadlySimpleListFragment.TAG));
  }

  @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
    RadioButton buttonView = (RadioButton) group.findViewById(checkedId);
    if (buttonView != null) {
      String policyName = buttonView.getText() + "";
      for (ToroStrategy policy : STRATEGIES) {
        if (policy.getDescription().equals(policyName)) {
          Toro.setStrategy(policy);
          break;
        }
      }
    }
  }
}
