/*
 * Copyright 2017 eneim@Eneim Labs, nam@ene.im
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

package im.ene.toro.extended;

import android.app.Application;
import android.view.View;
import android.view.ViewParent;
import com.google.android.exoplayer2.ExoPlayer;
import im.ene.toro.PlayerManager;
import im.ene.toro.Toro;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author eneim.
 * @since 4/6/17.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(Toro.class)
public class ExtPlayerViewHelperTest {

  @Mock View itemView;
  @Mock ExtToroPlayer player;
  @Mock PlayerManager manager;
  @Mock ViewParent viewParent;

  // mock app for Toro test
  @Mock Application application;

  private ExtPlayerViewHelper helper;

  @Before
  public void setUp() {
    Toro.init(application);

    MockitoAnnotations.initMocks(this);
    PowerMockito.mockStatic(Toro.class);

    when(player.getNextTarget()).thenReturn(ExtToroPlayer.Target.THIS_PLAYER);
    helper = new ExtPlayerViewHelper(player, itemView);

    when(itemView.getParent()).thenReturn(viewParent);
    PowerMockito.when(Toro.getManager(viewParent)).thenReturn(manager);
  }

  @Test
  public void testLoopPlayback() {
    // finish playback then return to idle state
    helper.onPlayerStateChanged(false, ExoPlayer.STATE_IDLE);
    verify(player).getNextTarget();

    // restart playback
    verify(manager).setPlayer(player);
    verify(manager).restorePlaybackState(player.getMediaId());
    verify(manager).startPlayback();
  }

  // TODO move this to super class (should test ExoPlayerViewHelper instead)
  @Test
  public void testPlaybackEnds() {
    helper.onPlayerStateChanged(true, ExoPlayer.STATE_ENDED);
    // after playback finishes
    verify(manager).savePlaybackState(player.getMediaId(), 0L, player.getDuration());
    verify(manager).setPlayer(null);
    verify(player).onPlaybackCompleted();
  }

  @Test
  public void testPlaybackEndsFalse() {
    helper.onPlayerStateChanged(false, ExoPlayer.STATE_ENDED);
    verify(manager, never()).setPlayer(null);
    verify(player, never()).onPlaybackCompleted();
  }

  // TODO test other statuses
}
