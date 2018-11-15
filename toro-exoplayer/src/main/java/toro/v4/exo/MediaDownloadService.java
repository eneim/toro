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

import android.support.annotation.RequiresPermission;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadManager.TaskState;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.PlatformScheduler;
import com.google.android.exoplayer2.util.Util;
import im.ene.toro.annotations.Beta;

/** A service for downloading media. */
@Beta public class MediaDownloadService extends DownloadService {

  private static final int JOB_ID = 1;

  public MediaDownloadService() {
    super(FOREGROUND_NOTIFICATION_ID_NONE);
  }

  @Override protected DownloadManager getDownloadManager() {
    return null;
  }

  @Override @RequiresPermission(android.Manifest.permission.RECEIVE_BOOT_COMPLETED)
  protected PlatformScheduler getScheduler() {
    return Util.SDK_INT >= 21 ? new PlatformScheduler(this, JOB_ID) : null;
  }

  @SuppressWarnings("StatementWithEmptyBody") //
  @Override protected void onTaskStateChanged(TaskState taskState) {
    if (taskState.action.isRemoveAction) {
      return;
    }
    if (taskState.state == TaskState.STATE_COMPLETED) {
      // TODO [20181008] handle this.
    } else if (taskState.state == TaskState.STATE_FAILED) {
      // TODO [20181008] handle this
    }
  }
}
