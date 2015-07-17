
# Icons

This module compiles a set of icons into a single web font file for use in the web application.

## Tools

* [grunt-webfont](https://www.npmjs.com/package/grunt-webfont). See the tutorial 
    [Building icon fonts with Grunt](https://medium.com/@lmartins/building-icon-fonts-with-grunt-4e22107d7f97)
* [gradle-grunt](https://github.com/srs/gradle-grunt-plugin)


## Process

1. Gradle invokes grunt
2. Grunt invokes grunt-webfont task
3. grunt-web-font task assembles a font from the svg files in src/main/svg 
4. grunt-web-font task writes a css file to build/fonts/org/activityinfo/ui/icons/icons.css, based on src/main/template/template.css
5. gradle packages generated files together with gwt sources in a jar, which is consumed by the server module

## Still TODO

[ ] Generate Icons.java automatically from the Gruntfile.js
