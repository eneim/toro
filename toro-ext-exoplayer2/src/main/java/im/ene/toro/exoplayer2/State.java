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

package im.ene.toro.exoplayer2;

import android.support.annotation.IntDef;
import com.google.android.exoplayer2.ExoPlayer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by eneim on 6/9/16.
 */
@IntDef({
    /**
     * The player is neither prepared or being prepared.
     */
    ExoPlayer.STATE_IDLE,
    /**
     * The player is prepared but not able to immediately play from the current position. The cause
     * is {@link TrackRenderer} specific, but this state typically occurs when more data needs
     * to be buffered for playback to start.
     */
    ExoPlayer.STATE_BUFFERING,
    /**
     * The player is prepared and able to immediately play from the current position. The player will
     * be playing if {@link #getPlayWhenReady()} returns true, and paused otherwise.
     */
    ExoPlayer.STATE_READY,
    /**
     * The player has finished playing the media.
     */
    ExoPlayer.STATE_ENDED
}) @Retention(RetentionPolicy.SOURCE) public @interface State {
}
