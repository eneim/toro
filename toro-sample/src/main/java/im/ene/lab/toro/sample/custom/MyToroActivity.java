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

package im.ene.lab.toro.sample.custom;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.data.SimpleVideoObject;
import im.ene.lab.toro.sample.data.VideoSource;
import im.ene.lab.toro.sample.fragment.RecyclerViewFragment;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eneim on 6/23/16.
 */
public class MyToroActivity extends AppCompatActivity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_show_case);
    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.container, MySampleFragment.newInstance())
          .commit();
    }
  }

  public static class MySampleFragment extends RecyclerViewFragment {

    public static MySampleFragment newInstance() {
      return new MySampleFragment();
    }

    @NonNull @Override protected RecyclerView.LayoutManager getLayoutManager() {
      return new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
    }

    @NonNull @Override protected RecyclerView.Adapter getAdapter() {
      return new Adapter();
    }
  }

  static class Adapter extends RecyclerView.Adapter<MyVideoViewHolder> {

    protected List<SimpleVideoObject> mVideos = new ArrayList<>();

    public Adapter() {
      super();
      setHasStableIds(true);
      for (String item : VideoSource.SOURCES) {
        mVideos.add(new SimpleVideoObject(item));
      }
    }

    @Override public MyVideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext())
          .inflate(MyVideoViewHolder.LAYOUT_RES, parent, false);
      return new MyVideoViewHolder(view);
    }

    @Override public void onBindViewHolder(MyVideoViewHolder holder, int position) {
      holder.bind(this, mVideos.get(position % mVideos.size()));
    }

    @Override public int getItemCount() {
      return 512;
    }
  }
}
