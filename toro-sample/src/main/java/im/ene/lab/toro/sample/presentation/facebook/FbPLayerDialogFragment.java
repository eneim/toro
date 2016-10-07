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

package im.ene.lab.toro.sample.presentation.facebook;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import butterknife.Bind;
import butterknife.ButterKnife;
import im.ene.lab.toro.Toro;
import im.ene.lab.toro.ToroAdapter;
import im.ene.lab.toro.ToroPlayer;
import im.ene.lab.toro.ToroStrategy;
import im.ene.lab.toro.sample.BuildConfig;
import im.ene.lab.toro.sample.R;
import im.ene.lab.toro.sample.data.SimpleVideoObject;
import im.ene.lab.toro.sample.data.VideoSource;
import im.ene.lab.toro.sample.widget.DividerItemDecoration;
import im.ene.lab.toro.sample.widget.LargeDialogFragment;
import im.ene.toro.extended.ExtToroAdapter;
import im.ene.toro.extended.SnapToTopLinearLayoutManager;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eneim on 5/13/16.
 */
public class FbPLayerDialogFragment extends LargeDialogFragment {

  public static final String TAG = "FbPLayer:Dialog";

  public static final String ARGS_INIT_VIDEO = "fb_player_init_video";
  public static final String ARGS_INIT_POSITION = "fb_player_init_position";
  public static final String ARGS_INIT_DURATION = "fb_player_init_duration";
  public static final String ARGS_LATEST_TIMESTAMP = "player_latest_timestamp";

  public static FbPLayerDialogFragment newInstance(SimpleVideoObject initItem, long initPos,
      long initDuration) {
    FbPLayerDialogFragment fragment = new FbPLayerDialogFragment();
    Bundle args = new Bundle();
    args.putParcelable(ARGS_INIT_VIDEO, initItem);
    args.putLong(ARGS_INIT_POSITION, initPos);
    args.putLong(ARGS_INIT_DURATION, initDuration);
    fragment.setArguments(args);
    return fragment;
  }

  private ToroStrategy strategyToRestore;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    strategyToRestore = Toro.getStrategy();

    Toro.setStrategy(new ToroStrategy() {
      boolean isFirstPlayerDone = false;

      @Override public String getDescription() {
        return "First video plays first";
      }

      @Override public ToroPlayer findBestPlayer(List<ToroPlayer> candidates) {
        return strategyToRestore.findBestPlayer(candidates);
      }

      @Override public boolean allowsToPlay(ToroPlayer player, ViewParent parent) {
        boolean allowToPlay = (isFirstPlayerDone || player.getPlayOrder() == 0)  //
            && strategyToRestore.allowsToPlay(player, parent);

        // A work-around to keep track of first video on top.
        if (player.getPlayOrder() == 0) {
          isFirstPlayerDone = true;
        }
        return allowToPlay;
      }
    });
  }

  @Override public void onDetach() {
    super.onDetach();
    Toro.setStrategy(strategyToRestore);
  }

  @Bind(R.id.recycler_view) RecyclerView recyclerView;
  private SimpleVideoObject initItem;
  private long initPosition;
  private long initDuration;
  private Adapter adapter;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      initItem = getArguments().getParcelable(ARGS_INIT_VIDEO);
      initPosition = getArguments().getLong(ARGS_INIT_POSITION);
      initDuration = getArguments().getLong(ARGS_INIT_DURATION);
    }

    if (initItem == null) {
      if (BuildConfig.DEBUG) {
        throw new IllegalStateException("Unexpected state");
      }
      getActivity().finish();
    }
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.generic_recycler_view, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    RecyclerView.LayoutManager layoutManager =
        new SnapToTopLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
        ((LinearLayoutManager) layoutManager).getOrientation()));

    adapter = new Adapter(initItem);
    recyclerView.setHasFixedSize(false);
    recyclerView.setAdapter(adapter);
    recyclerView.smoothScrollToPosition(0);
  }

  @Override public void onResume() {
    super.onResume();
    Toro.register(recyclerView);
    adapter.saveVideoState(initItem.toString() + "@0", initPosition, initDuration);
  }

  @Override public void onPause() {
    super.onPause();
  }

  @Override public void onDismiss(DialogInterface dialog) {
    Toro.unregister(recyclerView);
    Long latestPosition = adapter.getSavedPosition(initItem.toString() + "@0"); // first item
    if (getTargetFragment() != null && latestPosition != null) {
      Intent result = new Intent();
      result.putExtra(ARGS_LATEST_TIMESTAMP, latestPosition);
      getTargetFragment().onActivityResult(FbFeedFragment.RESUME_REQUEST_CODE, Activity.RESULT_OK,
          result);
    }
    super.onDismiss(dialog);
  }

  private static class Adapter extends ExtToroAdapter<ToroAdapter.ViewHolder>
      implements OrderedPlayList {

    public static final int VIEW_TYPE_NO_VIDEO = 1;

    public static final int VIEW_TYPE_VIDEO = 1 << 1;

    // public static final int VIEW_TYPE_VIDEO_MIXED = 1 << 2;

    protected List<SimpleVideoObject> mVideos = new ArrayList<>();

    @IntDef({
        VIEW_TYPE_NO_VIDEO, VIEW_TYPE_VIDEO /*, VIEW_TYPE_VIDEO_MIXED */
    }) @Retention(RetentionPolicy.SOURCE) public @interface Type {
    }

    private final SimpleVideoObject initItem;

    public Adapter(SimpleVideoObject initItem) {
      super();
      setHasStableIds(true);
      for (String item : VideoSource.SOURCES) {
        mVideos.add(new SimpleVideoObject(item));
      }

      this.initItem = initItem;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      final ToroAdapter.ViewHolder viewHolder;
      final View view = LayoutInflater.from(parent.getContext())
          .inflate(SimpleVideoViewHolder.LAYOUT_RES, parent, false);
      viewHolder = new SimpleVideoViewHolder(view);

      viewHolder.setOnItemClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          Toro.rest(true);
          new AlertDialog.Builder(v.getContext()).setTitle("Sample Action")
              .setMessage("Sample Content")
              .setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override public void onDismiss(DialogInterface dialog) {
                  Toro.rest(false);
                }
              })
              .create()
              .show();
        }
      });
      return viewHolder;
    }

    @Type @Override public int getItemViewType(int position) {
      return VIEW_TYPE_VIDEO;
    }

    @Override public long getItemId(int position) {
      Object item = getItem(position);
      if (item != null) {
        return item.hashCode();
      } else {
        return 0;
      }
    }

    @Override public int getItemCount() {
      return 512; // Magic number :trollface:
    }

    @Override public int firstVideoPosition() {
      return 0;
    }

    @Nullable @Override protected Object getItem(int position) {
      return mVideos.get(position % mVideos.size());
    }

  }
}
