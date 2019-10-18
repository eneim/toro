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

package im.ene.toro.sample.intro;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.ene.toro.sample.R;
import im.ene.toro.sample.basic.BasicListActivity;
import im.ene.toro.sample.common.BaseFragment;
import im.ene.toro.sample.complex.ComplexListActivity;
import im.ene.toro.sample.facebook.FacebookDemoActivity;
import im.ene.toro.sample.flexible.FlexibleListActivity;
import im.ene.toro.sample.legacy.LegacyDemoActivity;
import im.ene.toro.sample.nested.NestedListActivity;
import im.ene.toro.sample.pagers.ManyPagersActivity;

/**
 * @author eneim (7/2/17).
 */

public class IntroFragment extends BaseFragment {

  public static IntroFragment newInstance() {
    Bundle args = new Bundle();
    IntroFragment fragment = new IntroFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.recycler_view) RecyclerView recyclerView;

  Callback callback;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof Callback) {
      this.callback = (Callback) context;
    }
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle bundle) {
    return inflater.inflate(R.layout.fragment_intro, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    if (callback != null) callback.onToolbarCreated(toolbar);
    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);
    DemoAdapter adapter = new DemoAdapter();
    recyclerView.setAdapter(adapter);
    adapter.setOnDemoClick(new OnDemoClick() {
      @Override void onDemoClick(View view, Demo demo) {
        if (callback != null) {
          callback.onDemoClick(view, demo);
        }
      }
    });
  }

  @Override public void onDetach() {
    super.onDetach();
    callback = null;
  }

  static class DemoAdapter extends RecyclerView.Adapter<DemoItemViewHolder> {

    OnDemoClick onDemoClick;

    void setOnDemoClick(OnDemoClick onDemoClick) {
      this.onDemoClick = onDemoClick;
    }

    @Override public DemoItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.view_holder_intro_button, parent, false);
      DemoItemViewHolder viewHolder = new DemoItemViewHolder(view);
      viewHolder.itemView.setOnClickListener(v -> {
        int pos = viewHolder.getAdapterPosition();
        if (pos != RecyclerView.NO_POSITION && onDemoClick != null) {
          onDemoClick.onDemoClick(v, Demo.values()[pos]);
        }
      });

      return viewHolder;
    }

    @Override public void onBindViewHolder(DemoItemViewHolder holder, int position) {
      holder.bind(Demo.values()[position]);
    }

    @Override public int getItemCount() {
      return Demo.values().length;
    }
  }

  static abstract class OnDemoClick {

    abstract void onDemoClick(View view, Demo demo);
  }

  static class DemoItemViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.demo_title) TextView demoTitle;

    DemoItemViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    void bind(Demo demo) {
      demoTitle.setText(demo.getTitle());
    }
  }

  public enum Demo {
    BASIC("Basic List Demo", BasicListActivity.class),  //
    TIMELINE("Facebook Timeline Demo", FacebookDemoActivity.class), //
    NESTED("Nested Container Demo", NestedListActivity.class),  //
    COMPLEX("Complicated Grid Demo", ComplexListActivity.class),  //
    FLEXIBLE("Flexible Grid Demo", FlexibleListActivity.class),  //
    MANY_PAGERS("ViewPagers in ViewPager Demo", ManyPagersActivity.class),  //
    // CUSTOM("Custom Layout Demo", CustomLayoutActivity.class),  // This is the launch Activity
    LEGACY("Legacy VideoView Demo", LegacyDemoActivity.class) //
    ;
    private final String title;
    private final Class<?> activityClass;

    Demo(String title, Class<?> activityClass) {
      this.title = title;
      this.activityClass = activityClass;
    }

    public String getTitle() {
      return title;
    }

    public Class<?> getActivityClass() {
      return activityClass;
    }
  }

  public interface Callback {

    void onToolbarCreated(Toolbar toolbar);

    void onDemoClick(View view, Demo demo);
  }
}
