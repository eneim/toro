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
package toro.v4.exo;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.offline.ActionFile;
import com.google.android.exoplayer2.offline.DownloadAction;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadManager.TaskState;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.offline.ProgressiveDownloadHelper;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.offline.TrackKey;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.offline.DashDownloadHelper;
import com.google.android.exoplayer2.source.hls.offline.HlsDownloadHelper;
import com.google.android.exoplayer2.source.smoothstreaming.offline.SsDownloadHelper;
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider;
import com.google.android.exoplayer2.ui.TrackNameProvider;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Util;
import im.ene.toro.annotations.Beta;
import im.ene.toro.exoplayer.R;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import toro.v4.Media;
import toro.v4.exo.factory.Downloader;

import static com.google.android.exoplayer2.offline.DownloadAction.getDefaultDeserializers;
import static com.google.android.exoplayer2.util.Util.getUtf8Bytes;

/**
 * Tracks media that has been downloaded.
 *
 * <p>Tracked downloads are persisted using an {@link ActionFile}, however in a real application
 * it's expected that state will be stored directly in the application's media database, so that it
 * can be queried efficiently together with other information about the media.
 */
@Beta class DownloaderImpl implements Downloader {

  /** Listens for changes in the tracked downloads. */
  public interface Listener {

    /** Called when the tracked downloads changed. */
    void onDownloadsChanged(Uri uri);
  }

  private static final String TAG = "Toro:Lib:Tracker";

  @SuppressWarnings("WeakerAccess") //
  final ActionFile actionFile;
  @SuppressWarnings("WeakerAccess") //
  final Context context;
  @SuppressWarnings({ "FieldCanBeLocal", "unused" }) //
  private final TrackNameProvider trackNameProvider;
  private final Handler actionFileWriteHandler;
  private final DataSource.Factory manifestDataSourceFactory;
  private final HashMap<Uri, DownloadAction> trackedDownloadStates;
  private final CopyOnWriteArraySet<Listener> listeners;

  DownloaderImpl(Context context, DataSource.Factory manifestDataSourceFactory, File actionFile,
      DownloadAction.Deserializer... deserializers) {
    this.context = context.getApplicationContext();
    this.manifestDataSourceFactory = manifestDataSourceFactory;
    this.actionFile = new ActionFile(actionFile);
    trackNameProvider = new DefaultTrackNameProvider(context.getResources());
    listeners = new CopyOnWriteArraySet<>();
    trackedDownloadStates = new HashMap<>();
    HandlerThread actionFileWriteThread = new HandlerThread("Toro:DownloadTracker");
    actionFileWriteThread.start();
    actionFileWriteHandler = new Handler(actionFileWriteThread.getLooper());
    loadTrackedActions(deserializers.length > 0 ? deserializers : getDefaultDeserializers());
  }

  void addListener(Listener listener) {
    listeners.add(listener);
  }

  void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  private boolean isDownloaded(Uri uri) {
    return trackedDownloadStates.containsKey(uri);
  }

  // This method is used to create MediaSource for downloaded Media/Uri.
  @Override public List<StreamKey> getOfflineStreamKeys(Uri uri) {
    if (!trackedDownloadStates.containsKey(uri)) {
      return Collections.emptyList();
    }
    //noinspection ConstantConditions
    return trackedDownloadStates.get(uri).getKeys();
  }

  @Override public void download(Media media) {
    if (!isDownloaded(media.getUri())) {
      HelperCallbackImpl helper =
          new HelperCallbackImpl(getDownloadHelper(media.getUri(), media.getExtension()),
              media.toString());
      helper.prepare();
    } else {
      // Do nothing
    }
  }

  @Override public void unload(Media media) {
    if (isDownloaded(media.getUri())) {
      DownloadAction removeAction =
          getDownloadHelper(media.getUri(), media.getExtension()).getRemoveAction(
              getUtf8Bytes(media.toString()));
      startServiceWithAction(removeAction);
    }
  }

  // DownloadManager.Listener
  @Override public void onInitialized(DownloadManager downloadManager) {
    // Do nothing.
  }

  @Override public void onTaskStateChanged(DownloadManager downloadManager, TaskState taskState) {
    DownloadAction action = taskState.action;
    Uri uri = action.uri;
    if ((action.isRemoveAction && taskState.state == TaskState.STATE_COMPLETED)
        || (!action.isRemoveAction && taskState.state == TaskState.STATE_FAILED)) {
      // A download has been removed, or has failed. Stop tracking it.
      if (trackedDownloadStates.remove(uri) != null) {
        handleTrackedDownloadStatesChanged(uri);
      }
    }
  }

  @Override public void onIdle(DownloadManager downloadManager) {
    // Do nothing.
  }

  // Internal methods

  private void loadTrackedActions(DownloadAction.Deserializer[] deserializers) {
    try {
      DownloadAction[] allActions = actionFile.load(deserializers);
      for (DownloadAction action : allActions) {
        trackedDownloadStates.put(action.uri, action);
      }
    } catch (IOException e) {
      Log.e(TAG, "Failed to load tracked actions", e);
    }
  }

  private void handleTrackedDownloadStatesChanged(Uri uri) {
    for (Listener listener : listeners) {
      listener.onDownloadsChanged(uri);
    }

    final DownloadAction[] actions = trackedDownloadStates.values().toArray(new DownloadAction[0]);
    actionFileWriteHandler.post(new Runnable() {
      @Override public void run() {
        try {
          actionFile.store(actions);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @SuppressWarnings("WeakerAccess") //
  void startDownload(DownloadAction action) {
    if (trackedDownloadStates.containsKey(action.uri)) {
      // This content is already being downloaded. Do nothing.
      return;
    }
    trackedDownloadStates.put(action.uri, action);
    handleTrackedDownloadStatesChanged(action.uri);
    startServiceWithAction(action);
  }

  private void startServiceWithAction(DownloadAction action) {
    DownloadService.startWithAction(context, MediaDownloadService.class, action, false);
  }

  private DownloadHelper getDownloadHelper(Uri uri, String extension) {
    int type = Util.inferContentType(uri, extension);
    switch (type) {
      case C.TYPE_DASH:
        return new DashDownloadHelper(uri, manifestDataSourceFactory);
      case C.TYPE_SS:
        return new SsDownloadHelper(uri, manifestDataSourceFactory);
      case C.TYPE_HLS:
        return new HlsDownloadHelper(uri, manifestDataSourceFactory);
      case C.TYPE_OTHER:
        return new ProgressiveDownloadHelper(uri);
      default:
        throw new IllegalStateException("Unsupported type: " + type);
    }
  }

  private final class HelperCallbackImpl implements DownloadHelper.Callback {

    private final DownloadHelper downloadHelper;
    private final String name;

    HelperCallbackImpl(DownloadHelper downloadHelper, String name) {
      this.downloadHelper = downloadHelper;
      this.name = name;
    }

    public void prepare() {
      downloadHelper.prepare(this);
    }

    @Override public void onPrepared(DownloadHelper helper) {
      // Preparation completes. Now other DownloadHelper methods can be called.
      List<TrackKey> trackKeys = new ArrayList<>();
      for (int i = 0; i < helper.getPeriodCount(); i++) {
        TrackGroupArray trackGroups = helper.getTrackGroups(i);
        for (int j = 0; j < trackGroups.length; j++) {
          TrackGroup trackGroup = trackGroups.get(j);
          for (int k = 0; k < trackGroup.length; k++) {
            Format track = trackGroup.getFormat(k);
            if (shouldDownload(track)) {
              trackKeys.add(new TrackKey(i, j, k));
            }
          }
        }
      }

      DownloadAction action = this.downloadHelper.getDownloadAction(getUtf8Bytes(name), trackKeys);
      startDownload(action);
    }

    private boolean shouldDownload(Format track) {
      return true;
    }

    @Override public void onPrepareError(DownloadHelper helper, IOException e) {
      Toast.makeText(context.getApplicationContext(), R.string.download_start_error,
          Toast.LENGTH_LONG).show();
      Log.e(TAG, "Failed to start download", e);
    }
  }
}
