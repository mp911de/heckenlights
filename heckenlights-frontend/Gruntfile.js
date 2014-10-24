/* jshint node: true */

module.exports = function (grunt) {
    "use strict";

    RegExp.quote = require('regexp-quote')
    var btoa = require('btoa')
    // Project configuration.
    grunt.initConfig({

        // Metadata.
        pkg: grunt.file.readJSON('package.json'),

        // Task configuration.
        clean: {
            dist: ['dist'],
            compress: ['archive.zip']
        },

        jshint: {
            gruntfile: {
                src: 'Gruntfile.js'
            }
        },

        useminPrepare: {
            html: ['index.html', 'landing.html.de.en']
        },


        recess: {
            options: {
                compile: true
            },
            site: {
                src: ['less/site.less'],
                dest: 'css/<%= pkg.name %>.css'
            },
            min: {
                options: {
                    compress: true
                },
                src: ['less/site.less'],
                dest: 'css/<%= pkg.name %>.min.css'
            }
        },


        watch: {
            recess: {
                files: 'less/*.less',
                tasks: ['recess']
            }
        },

        compress: {
            main: {
                options: {
                    archive: 'archive.zip'
                },
                files: [
                    {src: ['css/*', 'js/**', 'fonts/**', 'images/**', 'favicon.ico']},
                    {src: ['landing.html.*']}]
            }
        },

        mustache_render: {
            de: {
                files: [{
                    data: "src/locales/de.json",
                    template: "landing.html",
                    dest: "landing.html.de"
                }]
            },
            en: {
                files: [{
                    data: "src/locales/en.json",
                    template: "landing.html",
                    dest: "landing.html.en"
                }]
            }
        }

    });


    // These plugins provide necessary tasks.
    grunt.loadNpmTasks('browserstack-runner');
    grunt.loadNpmTasks('grunt-contrib-compress');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-connect');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-qunit');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-compress');
    grunt.loadNpmTasks('grunt-html-validation');
    grunt.loadNpmTasks('grunt-jekyll');
    grunt.loadNpmTasks('grunt-recess');
    grunt.loadNpmTasks('grunt-sed');
    grunt.loadNpmTasks('grunt-mustache-render');


    // CSS distribution task.
    grunt.registerTask('dist-css', ['recess']);

    // Full distribution task.
    grunt.registerTask('dist', ['clean', 'dist-css', 'mustache_render', 'compress']);

    // Default task.
    grunt.registerTask('default', ['dist']);

};
