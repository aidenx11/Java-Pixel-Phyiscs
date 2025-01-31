# Java Pixel Physics  

## [Try the web version](https://aidenx11.github.io/Java-Pixel-Physics/)
*Note: The web version has a smaller screen size and resolution than the desktop version to save performance.*

## [Download Latest Release](https://github.com/aidenx11/Java-Pixel-Physics/releases/tag/v1.0.0) 

## Overview
  Inspired by the various falling sand simulators from when I was a kid such as [The Sandbox](https://apps.apple.com/us/app/the-sandbox-building-craft/id520777858), and more modern games such as [Noita](https://en.wikipedia.org/wiki/Noita_(video_game)), I aimed to create a simulation of my own in Java using the [LibGDX](https://libgdx.com/) framework.

https://github.com/aidenx11/Java-Pixel-Physics/assets/42153616/eb25e230-383b-4ef5-bfd8-10c8e1452923

  As of now, the simulation has basic gasses, water and lava, and a small variety of solids that interact with gravity. There is logic for objects burning, getting wet, melting, and settling. There is also logic for gravity, acceleration and basic concepts of momentum/inertia. 

  Most of the logic is performed by manipulating a 2D array of "element" objects, changing their positions in the array and their individual fields as needed for their behavior. Each frame, every element checks other elements surrounding itself and decides what it needs to do. For more information on this design pattern, see the concept of [Cellular Automata](https://en.wikipedia.org/wiki/Cellular_automaton).

  Optimizations have been made using chunks. The screen is separated into a grid of chunks, and each chunk will only update the elements inside of it if it needs to. Support for multithreading is possible, but not necessary yet.
