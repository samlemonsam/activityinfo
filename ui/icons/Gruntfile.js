module.exports = function(grunt) {
    
    // Project configuration.
    grunt.initConfig({
        pkg: grunt.file.readJSON("package.json"),

        webfont: {
            icons: {
                src: "src/main/svg/*.*",
                dest: "build/fonts/org/activityinfo/ui/icons",
                options: {
                    engine: 'node',
                    autoHint: false,
                    normalize: true,
                    htmlDemo: false,
                    stylesheet: "css",
                    template: "src/main/template/template.css"
                }
            }
        }
    });

    // Load the plugin that provides the “uglify” task.
    grunt.loadNpmTasks("grunt-webfont");

    // Default task(s).
    grunt.registerTask("build", ["webfont"]);

};

