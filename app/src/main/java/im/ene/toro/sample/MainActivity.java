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

package im.ene.toro.sample;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import im.ene.toro.DefaultPlayerManager;
import im.ene.toro.ExoPlayerHelper;
import im.ene.toro.Player;
import im.ene.toro.Selector;
import im.ene.toro.ToroHelper;
import im.ene.toro.ToroUtil;
import im.ene.toro.sample.common.MediaUrls;
import im.ene.toro.widget.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;

public class MainActivity extends AppCompatActivity {

  @SuppressWarnings("unused") private static final String TAG = "ToroLib:Sample";

  ToroHelper helper;
  SimpleAdapter adapter;
  @BindView(R.id.recycler_view) Container container;

  @OnClick(R.id.button_change_all) void changeAll() {
    adapter.changeAll();
  }

  @OnClick(R.id.button_change) void change() {
    adapter.change();
  }

  @OnClick(R.id.button_move) void move() {
    adapter.move();
  }

  @OnClick(R.id.button_swap) void swap() {
    adapter.swap();
  }

  @OnClick(R.id.button_insert) void insert() {
    adapter.insert();
  }

  @OnClick(R.id.button_remove) void remove() {
    adapter.remove();
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    adapter = new SimpleAdapter();
    container.setAdapter(adapter);
    GridLayoutManager layoutManager = new GridLayoutManager(this, 1 /* change this for testing */);
    // container.setItemAnimator(null); // un-comment to test non-animator case
    container.setLayoutManager(layoutManager);
    helper = new ToroHelper(new DefaultPlayerManager(1), Selector.DEFAULT);
  }

  @Override protected void onStart() {
    super.onStart();
    helper.registerContainer(container);
  }

  @Override protected void onStop() {
    super.onStop();
    helper.registerContainer(null);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    Log.w(TAG, "onDestroy() called");
    container.setAdapter(null);
  }

  static class PlayerViewHolder extends BaseViewHolder implements Player {

    private ExoPlayerHelper helper;

    PlayerViewHolder(View itemView) {
      super(itemView);
      playerView.setVisibility(View.VISIBLE);
      indicator.setVisibility(View.GONE);
    }

    @NonNull @Override public View getPlayerView() {
      return playerView;
    }

    @Override public boolean prepare() {
      if (helper == null) {
        helper = new ExoPlayerHelper(this.playerView);
      }

      try {
        helper.prepare(Uri.parse(MediaUrls.MP4_HEVC_AVSep_3840x2160_8Mbps));
      } catch (ParserException e) {
        e.printStackTrace();
      }

      indicator.setText("PREPARED");
      return true;
    }

    @Override public void release() {
      indicator.setText("RELEASED");
      if (helper != null) {
        helper.release();
        helper = null;
      }
    }

    @Override public void play() {
      indicator.setText("PLAY");
      if (helper != null) {
        helper.play();
      }
    }

    @Override public void pause() {
      indicator.setText("PAUSE");
      if (helper != null) helper.pause();
    }

    @Override public boolean isPlaying() {
      return playerView.getPlayer() != null && playerView.getPlayer().getPlayWhenReady();
      // return "PLAY".equals(indicator.getText());
    }

    @Override public boolean wantsToPlay() {
      ViewParent parent = itemView.getParent();
      float visible = parent != null && parent instanceof Container ? //
          ToroUtil.visibleAreaOffset(playerView, (Container) parent) : 0;
      return visible >= 0.85;
    }

    @Override public int getPlayOrder() {
      return getAdapterPosition();
    }

    @Override public String toString() {
      return format(Locale.getDefault(), "Player{%d}{%d}", getAdapterPosition(), hashCode())
          + isPlaying();
    }
  }

  static class BaseViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.item_container) LinearLayout container;
    @BindView(R.id.text_content) TextView content;
    @BindView(R.id.text_indicator) TextView indicator;
    @BindView(R.id.player) SimpleExoPlayerView playerView;

    BaseViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
      playerView.setVisibility(View.GONE);
      indicator.setVisibility(View.VISIBLE);
    }
  }

  private static class SimpleAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    final int TYPE_BASE = 1;
    final int TYPE_PLAYER = 2;

    private final List<String> items = new ArrayList<>();

    SimpleAdapter() {
      for (int i = 0; i < 6; i++) {
        items.add("INIT:" + i);
      }
    }

    @Override public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      if (viewType != TYPE_BASE && viewType != TYPE_PLAYER) {
        throw new IllegalArgumentException("Un-supported viewType: " + viewType);
      }

      View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.vh_simple_player, parent, false);
      return viewType == TYPE_BASE ? new BaseViewHolder(view) : new PlayerViewHolder(view);
    }

    @Override public void onBindViewHolder(BaseViewHolder holder, int position) {
      holder.content.setText(items.get(position));
      holder.indicator.setText("^＿^!");
    }

    @Override public int getItemViewType(int position) {
      return TYPE_PLAYER;
    }

    @Override public int getItemCount() {
      // return Integer.MAX_VALUE;
      return items.size();
    }

    void changeAll() {
      notifyDataSetChanged();
    }

    void change() {
      int pos = (int) (Math.random() * items.size());
      items.set(pos, "CHANGED:" + pos);
      Log.d(TAG, format(Locale.getDefault(), "change: %d", pos));
      notifyItemChanged(pos);
    }

    void move() {
      int pos = (int) (Math.random() * items.size());
      int type = getItemViewType(pos);
      String item = items.remove(pos);
      int pos2 = pos;
      while (pos2 == pos) {
        pos2 = (int) (Math.random() * items.size());
      }
      items.add(pos2, item);
      Log.d(TAG, format(Locale.getDefault(), "move: %d --> %d, type = %d", pos, pos2, type));
      notifyItemMoved(pos, pos2);
    }

    void swap() {
      int pos1 = (int) (Math.random() * items.size());
      int pos2 = pos1;
      while (pos2 == pos1) {
        pos2 = (int) (Math.random() * items.size());
      }
      Collections.swap(items, pos1, pos2);
      Log.d(TAG, format(Locale.getDefault(), "swap: %d <--> %d", pos1, pos2));
      notifyItemChanged(pos1);
      notifyItemChanged(pos2);
    }

    void remove() {
      int pos1 = (int) (Math.random() * items.size());
      items.remove(pos1);
      Log.d(TAG, format(Locale.getDefault(), "remove: %d", pos1));
      notifyItemRemoved(pos1);
    }

    void insert() {
      items.add("INSERTED: " + items.size());
      notifyItemInserted(items.size());
    }

    @Override public void onViewDetachedFromWindow(BaseViewHolder holder) {
      super.onViewDetachedFromWindow(holder);
      if (holder instanceof PlayerViewHolder) {
        ((PlayerViewHolder) holder).release();
      }
    }
  }
}
