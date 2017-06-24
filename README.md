# Toro 

> ***Video list auto playback made simple, specially built for RecyclerView***

<img src="/extra/web_hi_res_512.png" width="256">

<a href='https://ko-fi.com/A342OWW' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://az743702.vo.msecnd.net/cdn/kofi2.png?v=0' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Toro-green.svg?style=true)](https://android-arsenal.com/details/1/3106)
[![Join the chat at https://gitter.im/eneim/Toro](https://badges.gitter.im/eneim/Toro.svg)](https://gitter.im/eneim/Toro?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) 

### Menu

* [Features](#features)
* [Demo](#demo)
* [Getting start](#getting-start)

### Features

- Auto start/pause Media playback on Attach/Detach/Scroll/... events.
- Optional playback position save/restore (no position save/restore by default).
  - If enabled, Toro will also save/restore them on Configuration change (orientation change, multi-window mode ...).
- Customizable playback component: either MediaPlayer or ExoPlayer will work. Toro comes with default helper classes to support these 2.
- Customizable player selector: custom the selection of the player to start, among many other players.
  - Which in turn support single/multiple players.

### Demo

> Youtube link comes here.

### Getting start

Update module build.gradle.

```groovy
ext {
  toroVersion = '3.0.0-alpha1'
  // below: other dependencies' versions maybe
}

dependencies {
   compile "im.ene.toro3:toro:${toroVersion}" // deprecated in Android Studio 3.0
   
   // TODO: uncomment if using Android Studio 3.+ only
   // implementation "im.ene.toro3:toro:${toroVersion}"
}
```

Using ```Container``` in place of Video list. Below: a simple Container with default max simultaneous players count to 1.

```xml
<im.ene.toro.widget.Container
      android:id="@+id/recycler_view"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      app:max_player_number="1"
      />
```

Implement ```ToroPlayer``` to ViewHolder that should be a Video player.

```kotlin
// Better naming after import
import android.view.LayoutInflater.from as inflater

/**
 * @author eneim (6/24/17).
 */

class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ToroPlayer {

  companion object {
    // Used by Adapter to create layout
    internal val LAYOUT_RES = R.layout.vh_skeleton_exoplayer

    // Static call from Adapter to create this ViewHolder
    fun createNew(parent: ViewGroup) = PlayerViewHolder(inflater(parent.context).inflate(
        LAYOUT_RES, parent, false))
  }

  internal var playerView = itemView.findViewById<SimpleExoPlayerView>(R.id.player)
  internal var playerViewHelper: SimpleExoPlayerViewHelper? = null
  internal lateinit var mediaUri: Uri

  // Called by Adapter to pass a valid media uri here.
  fun bind(uri: Uri) {
    this.mediaUri = uri
  }

  override fun getPlayerView(): View {
    return playerView
  }

  override fun getCurrentPlaybackInfo(): PlaybackInfo {
    return playerViewHelper?.updatePlaybackInfo() ?: PlaybackInfo()
  }

  override fun initialize(container: Container, playbackInfo: PlaybackInfo) {
    if (playerViewHelper == null) {
      playerViewHelper = SimpleExoPlayerViewHelper(container, this, mediaUri)
    }
    playerViewHelper!!.initialize(playbackInfo)
  }

  override fun play() {
    playerViewHelper?.play()
  }

  override fun pause() {
    playerViewHelper?.pause()
  }

  override fun isPlaying(): Boolean {
    return playerViewHelper != null && playerViewHelper!!.isPlaying
  }

  override fun release() {
    try {
      playerViewHelper?.cancel()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    playerViewHelper = null
  }

  override fun wantsToPlay(): Boolean {
    val parent = itemView.parent
    var offset = 0f
    if (parent is View) {
      offset = ToroUtil.visibleAreaOffset(playerView, parent)
    }
    return offset >= 0.85
  }

  override fun getPlayerOrder(): Int {
    return adapterPosition
  }
}
```

Setup Adapter to use the ViewHolder above, and setup Container to use that Adapter.

That's all.

### License

> Copyright 2017 eneim@Eneim Labs, nam@ene.im

> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at

>        http://www.apache.org/licenses/LICENSE-2.0

> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.