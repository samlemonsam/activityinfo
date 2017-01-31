
# .NET Client

The .NET client provides a wrapper for ActivityInfo's API.

## Developing

The library is written in C# and authored primarily with MonoDevelop, an open-source, cross-platform IDE
for the CLR.

Installation instructions are available for [Linux](http://www.monodevelop.com/download/linux/) and 
[other platforms](http://www.monodevelop.com/download/)

## Building 

The client library is built during release by [xbuild](http://www.mono-project.com/docs/tools+libraries/tools/xbuild/),
an open-source implementation of MS Build.

Running `../../gradlew msbuild` will ensure that dependencies are resolved and invoke `xbuild`.

## Publishing

The library is packaged as a NuGet package and pushed to the 
[NuGet Gallery](https://www.nuget.org/packages/ActivityInfo.Client/).


