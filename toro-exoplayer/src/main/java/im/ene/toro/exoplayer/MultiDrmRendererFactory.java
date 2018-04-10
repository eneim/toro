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

package im.ene.toro.exoplayer;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link RenderersFactory} that supports multiple {@link DrmSessionManager}s.
 *
 * @author eneim (2018/03/05).
 */

final class MultiDrmRendererFactory extends DefaultRenderersFactory {

  private final ArrayList<DrmSessionManager<FrameworkMediaCrypto>> drmSessionManagers;

  MultiDrmRendererFactory(Context context,
      @Nullable DrmSessionManager<FrameworkMediaCrypto>[] managers, int extensionRendererMode) {
    super(context, managers != null && managers.length > 0 ? managers[0] : null,
        extensionRendererMode);
    this.drmSessionManagers = managers != null && managers.length > 1 // at least one more.
        ? new ArrayList<DrmSessionManager<FrameworkMediaCrypto>>() : null;
    if (this.drmSessionManagers != null) {
      //noinspection ManualArrayToCollectionCopy
      for (int i = 0; i < managers.length - 1; i++) {
        this.drmSessionManagers.add(managers[i + 1]);
      }
    }
  }

  @Override protected void buildVideoRenderers(Context context,
      @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
      long allowedVideoJoiningTimeMs, Handler eventHandler,
      VideoRendererEventListener eventListener, int extensionRendererMode,
      ArrayList<Renderer> out) {
    ArrayList<Renderer> localOut = new ArrayList<>();
    super.buildVideoRenderers(context, drmSessionManager, allowedVideoJoiningTimeMs,  //
        eventHandler, eventListener, extensionRendererMode, localOut);
    Set<Renderer> outSet = new HashSet<>(localOut);

    if (this.drmSessionManagers != null) {
      localOut.clear();
      for (DrmSessionManager<FrameworkMediaCrypto> manager : this.drmSessionManagers) {
        super.buildVideoRenderers(context, manager, allowedVideoJoiningTimeMs,  //
            eventHandler, eventListener, EXTENSION_RENDERER_MODE_OFF, localOut);
      }
      outSet.addAll(localOut);
    }

    out.addAll(outSet);
  }

  @Override protected void buildAudioRenderers(Context context,
      @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
      AudioProcessor[] audioProcessors, Handler eventHandler,
      AudioRendererEventListener eventListener, int extensionRendererMode,
      ArrayList<Renderer> out) {
    ArrayList<Renderer> localOut = new ArrayList<>();
    super.buildAudioRenderers(context, drmSessionManager, audioProcessors, eventHandler,
        eventListener, extensionRendererMode, localOut);
    Set<Renderer> outSet = new HashSet<>(localOut);

    if (this.drmSessionManagers != null) {
      localOut.clear();
      for (DrmSessionManager<FrameworkMediaCrypto> manager : this.drmSessionManagers) {
        super.buildAudioRenderers(context, manager, audioProcessors, eventHandler, eventListener,
            EXTENSION_RENDERER_MODE_OFF, localOut);
      }
      outSet.addAll(localOut);
    }

    out.addAll(outSet);
  }
}
