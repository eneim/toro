# Toro 
<a href='https://ko-fi.com/A342OWW' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://az743702.vo.msecnd.net/cdn/kofi2.png?v=0' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

[ ![Download](https://api.bintray.com/packages/eneim/Toro/toro/images/download.svg) ](https://bintray.com/eneim/Toro/toro/_latestVersion)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Toro-green.svg?style=true)](https://android-arsenal.com/details/1/3106)

[![Join the chat at https://gitter.im/eneim/Toro](https://badges.gitter.im/eneim/Toro.svg)](https://gitter.im/eneim/Toro?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) <a href="http://www.methodscount.com/?lib=im.ene.toro2%3Atoro%3A%2B"><img src="https://img.shields.io/badge/Methods and size-core: 326 | deps: 10993 | 32 KB-e91e63.svg"/></a>


<a href="https://www.iconfinder.com/icons/1105270/brand_connect_shape_square_icon#size=512""><img src="/art/web_hi_res_512.png" width="256"></a>



> ***Video list auto playback made simple, specially built for RecyclerView***

## 0. Latest Version:  [ ![Download](https://api.bintray.com/packages/eneim/Toro/toro/images/download.svg) ](https://bintray.com/eneim/Toro/toro/_latestVersion)

See [CHANGELOG.md](CHANGELOG.md) for more information.

## 1. Main features:

- Auto start media playback after RecyclerView layout has been settled down.
- Auto start/pause/resume media base on RecyclerView scroll state.
- Auto save latest media last playback position and resume from there later (*note: in Android default Media Player API, depend on Video's format and codec, the resume timestamp may varies*).
- Customizable playback behavior:
  - Customizable playback Strategy to decide the best component to start playback.
  - UI-based approach: you see the Video, then it can play. **Default**: you see 75% of the Video then it can start playing. **Advance**: you decide how much the visible Video should trigger the playback. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.
  
- Powerful, flexible and highly customizable API. See [Wiki](https://github.com/eneim/Toro/wiki) for more details.

- Selective playback: among many playable items, Strategy helps find the best one to start playback.

- Extensions: ExoPlayer (v2, v1), (legacy) Android MediaPlayer API.

  - Toro Extended: extension based on ExoPlayer 2, provides the concept of "Next Player to play" (abstract concept for loop/auto scroll, ...), and long click/press handling. See Wiki for more information.

- **Lots of built-in components, but Toro is still highly customizable.**

**See [Wiki](https://github.com/eneim/Toro/wiki) for more details.**

## 2. Toro in Action

| | |
|---|------|
| <img src="/art/sample_6.gif" width="648"> | <img src="/art/sample_1.gif" width="162"> <img src="/art/sample_2.gif" width="162"> <img src="/art/sample_3.gif" width="162"> <img src="/art/sample_4.gif" width="162"> <img src="/art/sample_5.gif" width="162"> <img src="/art/sample_7.gif" width="162"> |

## 3. How to use

##### 0. Pre-requirement

- Min support version: Android SDK level 16 (4.1.x)

##### 1. Add Toro to your project

- Make sure to have this in Project's top level ```build.gradle```

```groovy
allprojects {
	repositories {
    // Android Studio has this by default.
		jcenter()
	}
}
```

- Add this to dependencies

```groovy
ext {
	toroVersion = '2.2.0'
}

dependencies {

  // TODO !IMPORTANT: use only one of the following
  
  // include in your module (app or library)
  // include only core Toro library
  compile "im.ene.toro2:toro:${toroVersion}"

  // include Toro extended helper (Toro and ExoPlayer v2 extension is included already)
  compile "im.ene.toro2:toro-extended:${toroVersion}"
  
  // include extension for ExoPlayer v2 (Toro is included already)
  compile "im.ene.toro2:toro-ext-exoplayer2:${toroVersion}"
  
  // include extension for ExoPlayer v1 (Toro is included already)
  compile "im.ene.toro2:toro-ext-exoplayer:${toroVersion}"
  
  // include extension for Legacy MediaPlayer (Toro is included already)
  compile "im.ene.toro2:oro-ext-mediaplayer:${toroVersion}"
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

- Buy me some coffee for shorter update cycle ...

<a href='https://ko-fi.com/A342OWW' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://az743702.vo.msecnd.net/cdn/kofi2.png?v=0' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

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
