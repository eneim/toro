# Toro

[ ![Download](https://api.bintray.com/packages/eneim/Toro/toro/images/download.svg) ](https://bintray.com/eneim/Toro/toro/_latestVersion)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Toro-green.svg?style=true)](https://android-arsenal.com/details/1/3106)

[![Join the chat at https://gitter.im/eneim/Toro](https://badges.gitter.im/eneim/Toro.svg)](https://gitter.im/eneim/Toro?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) <a href="http://www.methodscount.com/?lib=im.ene.toro2%3Atoro%3A2.1.0"><img src="https://img.shields.io/badge/Methods and size-core: 250 | deps: 9441 | 28 KB-e91e63.svg"/></a>


<a href="https://www.iconfinder.com/icons/1105270/brand_connect_shape_square_icon#size=512""><img src="/art/web_hi_res_512.png" width="256"></a>



> ***Video list auto playback made simple, specially built for RecyclerView***

## 0. Latest Version:  [ ![Download](https://api.bintray.com/packages/eneim/Toro/toro/images/download.svg) ](https://bintray.com/eneim/Toro/toro/_latestVersion)

See [CHANGELOG.md](CHANGELOG.md) for more information.

## 1. Main features:

- Auto start/pause/resume video while scrolling your RecyclerView. Well handling playback lifecycle and callback states. Rich feature extensions and easy to start Demo App.

- Playback state cache: Toro remembers last playback position and resume from where you left (*note: in Android default Media Player API, depend on Video's format and codec, the resume timestamp may varies*).

- Playback extensible behavior:
  - Customizable playback Strategy to decide the best component to start playback. Built-in Strategies included.
  - UI-based logic, straight-forward approach: you see the Video, then it should play. **Default**: you see 75% of the Video then it should start playing. **Advance**: you decide how much the visible Video should trigger the playback. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.
  - Straight-forward decision making strategy: UI tells Toro API if it wants to play a Media or not, then Toro's strategy will decide if it allows the Media to play or not (well, when many of them want to play, we need to choose only one). **Default**: built-in implementation takes care of everything. **Advance**: control to both sides, extensible UI Widgets and Core APIs. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.

- Built-in rich feature Extensions: ExoPlayer (v2, v1) based Playback widget as well as fine tuned combination with RecyclerView APIs. Fallback to legacy with Extension for Android MediaPlayer API.

- Powerful, flexible and highly customizable API. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.

- Selective playback: find the best playable item, defined by smart, flexible [Strategies](/toro/src/main/java/im/ene/toro/ToroStrategy.java) to decide when and how a player should start playing. Optimized built-in [Strategies](/toro/src/main/java/im/ene/toro/Toro.java#L339) is enough to begin, but user could always create their own. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.

- Toro Extended: an extension based on ExoPlayer 2 extension, provides the concept of "Next Target Player to trigger", and long click/press handling. See Wiki for more information.

- **I create lots of built-in components, but keep Toro highly customizable.** You are free to decide how you want to start your player, but if you don't know, just let Toro help you to decide.

**See [Wiki](https://github.com/eneim/Toro/wiki) for more details.**

## 2. Toro in Action

| | |
|---|------|
| <img src="/art/sample_6.gif" width="648"> | <img src="/art/sample_1.gif" width="162"> <img src="/art/sample_2.gif" width="162"> <img src="/art/sample_3.gif" width="162"> <img src="/art/sample_4.gif" width="162"> <img src="/art/sample_5.gif" width="162"> <img src="/art/sample_7.gif" width="162"> |

## 3. How to use

##### 0. Prerequirement

- Min support version: Android SDK level 16 (4.1.x)

##### 1. Add Toro to your project

- Make sure to have this in Project's top level ```build.gradle```

```groovy
allprojects {
	repositories {
    // Android Studio use this by default.
		jcenter()
	}
}
```

- Add this to dependencies

```groovy
ext {
	toroVersion = '2.1.0'
}

dependencies {

  // include in your module (app or library)
  // include only core Toro library
  compile "im.ene.toro2:toro:${toroVersion}"

  // include extension for ExoPlayer v2 (Toro is included already)
  compile "im.ene.toro2:toro-ext-exoplayer2:${toroVersion}"
  // include extension for ExoPlayer v1 (Toro is included already)
  compile "im.ene.toro2:toro-ext-exoplayer:${toroVersion}"
  // include extension for Legacy MediaPlayer (Toro is included already)
  compile "im.ene.toro2:oro-ext-mediaplayer:${toroVersion}"

  // include Toro extended helper (Toro and ExoPlayer v2 extension is included already)
  compile "im.ene.toro2:toro-extended:${toroVersion}"
}
```

Latest version is always visible here: [![Download](https://api.bintray.com/packages/eneim/Toro/toro/images/download.svg) ](https://bintray.com/eneim/Toro/toro/_latestVersion)

##### **TL,DR**: **toro-sample** module from this library comes with several good practice of this library. Please take a look.

##### 2. Integrate **Toro** into your Application: see [Toro starting guide](https://github.com/eneim/Toro/wiki/0.-Toro-starting-guide)

##### 3. Register/Unregister a RecyclerView to get support from **Toro**: see [Register/Unregister RecyclerView to Toro](https://github.com/eneim/Toro/wiki/0.-Toro-starting-guide#registerunregister-recyclerview-to-toro)

##### 4. Create ViewHolder to use with **Toro**: see [This Wiki](https://github.com/eneim/Toro/wiki/1.-Toro-in-Practice---A-Beginner-Guide) to see how to start implementing.

##### 5. Core concepts and components of **Toro**: see [Wiki](https://github.com/eneim/Toro/wiki)

### Contribute to Toro

- Issue report and PRs are welcome.

### Hall of Fames

*Use Toro in your App? <a href="mailto:nam@ene.im?subject=Hi Nam">Email me</a> to get promoted here.*

### License

> Copyright 2016 eneim@Eneim Labs, nam@ene.im

> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at

>        http://www.apache.org/licenses/LICENSE-2.0

> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.
