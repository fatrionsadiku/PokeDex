<h1 align="center">Pokedex</h1>

<p align="center">  
Pokedex application built with Hilt, Coroutines,Jetpack(ViewModel), and Material Design based on MVVM architecture.
</p>
</br>

<p align="center">
<img src="/mockups/pokedex_mockup.jpg"/>
</p>

<img src="/mockups/pokeapp_showcase.gif" align="right" width="320"/>

## Tech stack & Open-source libraries
- Minimum SDK level 21
- [Kotlin](https://kotlinlang.org/) based, [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) for asynchronous.
- Jetpack
  - Lifecycle: Observe Android lifecycles and handle UI states upon the lifecycle changes.
  - ViewModel: Manages UI-related data holder and lifecycle aware. Allows data to survive configuration changes such as screen rotations.
  - [Hilt](https://dagger.dev/hilt/): for dependency injection.
  - [DataStore](https://developer.android.com/topic/libraries/architecture/datastore): Data storage solution that allows you to store key-value pairs or typed objects with protocol buffers
- Architecture
- MVVM Architecture (View - DataBinding - ViewModel - Model)
- [Retrofit2 & OkHttp3](https://github.com/square/retrofit): Construct the REST APIs and paging network data.
- [Glide](https://github.com/bumptech/glide), [GlidePalette](https://github.com/florent37/GlidePalette): Loading images from network.
- [Coil](https://coil-kt.github.io/coil), [CoilSVG](https://coil-kt.github.io/coil/svgs/): Loading SVG's from network.
- [Lottie](https://github.com/airbnb/lottie-android): JSON-based animation file format that allows you to ship animations on any platform as easily as shipping static assets.
- [Palette](https://developer.android.com/develop/ui/views/graphics/palette-colors): A support library that extracts prominent colors from images
- [Rainbow](https://github.com/skydoves/rainbow): Fluent syntactic sugar of Android for applying gradations, shading, and tinting.
- Custom Views
  - [ProgressView](https://github.com/skydoves/progressview): A polished and flexible ProgressView, fully customizable with animations.
## Architecture
**Pokedex** is based on the MVVM architecture and the Repository pattern, which follows the [Google's official architecture guidance](https://developer.android.com/topic/architecture).

## Open API

<img src="https://user-images.githubusercontent.com/24237865/83422649-d1b1d980-a464-11ea-8c91-a24fdf89cd6b.png" align="right" width="21%"/>

Pokedex using the [PokeAPI](https://pokeapi.co/docs/v2) for constructing RESTful API.<br>
PokeAPI provides a RESTful API interface to highly detailed objects built from thousands of lines of data related to Pokémon.
