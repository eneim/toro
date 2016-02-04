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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import im.ene.lab.toro.Toro;
import im.ene.lab.toro.ToroStrategy;
import im.ene.lab.toro.sample.R;

/**
 * Created by eneim on 2/1/16.
 */
public abstract class RecyclerViewFragment extends Fragment
    implements RadioGroup.OnCheckedChangeListener {

  protected RecyclerView mRecyclerView;

  private static final ToroStrategy[] mPolicies = {
      Toro.Strategies.MOST_VISIBLE_TOP_DOWN, Toro.Strategies.MOST_VISIBLE_TOP_DOWN_KEEP_LAST,
      Toro.Strategies.FIRST_PLAYABLE_TOP_DOWN, Toro.Strategies.FIRST_PLAYABLE_TOP_DOWN_KEEP_LAST
  };

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.generic_recycler_view, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
    mRecyclerView.setLayoutManager(getLayoutManager());
    mRecyclerView.setAdapter(getAdapter());

    RadioGroup policiesContainer = (RadioGroup) view.findViewById(R.id.policies_container);
    policiesContainer.setOnCheckedChangeListener(this);

    policiesContainer.removeAllViews();
    for (ToroStrategy policy : mPolicies) {
      RadioButton checkBox = (RadioButton) LayoutInflater.from(policiesContainer.getContext())
          .inflate(R.layout.policy_checkbox, policiesContainer, false);
      checkBox.setText(policy.getDescription());
      policiesContainer.addView(checkBox);
      checkBox.setChecked(Toro.getStrategy() == policy);
    }
  }

  @Override public void onResume() {
    super.onResume();
    Toro.register(mRecyclerView);
  }

  @Override public void onPause() {
    super.onPause();
    Toro.unregister(mRecyclerView);
  }

  @NonNull protected abstract RecyclerView.LayoutManager getLayoutManager();

  @NonNull protected abstract RecyclerView.Adapter getAdapter();

  @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
    RadioButton buttonView = (RadioButton) group.findViewById(checkedId);
    if (buttonView != null) {
      String policyName = buttonView.getText() + "";
      for (ToroStrategy policy : mPolicies) {
        if (policy.getDescription().equals(policyName)) {
          Toro.setStrategy(policy);
          mRecyclerView.getAdapter().notifyDataSetChanged();
          break;
        }
      }
    }
  }
}
