module.exports = function(grunt) {
    
    // Project configuration.
    grunt.initConfig({
        pkg: grunt.file.readJSON("package.json"),

        less: {
            build: {
                options: {
                    //plugins: [
                    //    new (require('less-plugin-autoprefix'))({browsers: ["last 2 versions"]}),
                    //    new (require('less-plugin-clean-css'))(cleanCssOptions)
                    //]
                },
                files: {
                    "build/css/org/activityinfo/ui/style/base.css": "src/main/less/base.less"
                }
            }
        },
        cssjanus: {
            build: {
                files: {
                    'build/css/org/activityinfo/ui/style/base-rtl.css': 'build/css/org/activityinfo/ui/style/base.css'
                }
            }
        }
    });

    // Load the plugin that provides the “uglify” task.
    grunt.loadNpmTasks("grunt-contrib-less");
    grunt.loadNpmTasks("grunt-cssjanus");


    // Default task(s).
    grunt.registerTask("build", ["less", "cssjanus"]);

};

