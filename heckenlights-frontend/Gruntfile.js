/* jshint node: true */

module.exports = function (grunt) {
    "use strict";

    // Load grunt tasks automatically
    require('load-grunt-tasks')(grunt);

    // Time how long tasks take. Can help when optimizing build times
    require('time-grunt')(grunt);

    // Configurable paths for the application
    var appConfig = {
        app: './',
        dist: 'dist'
    };

    // Project configuration.
    grunt.initConfig({

        // Metadata.
        pkg: grunt.file.readJSON('package.json'),

        // Task configuration.
        clean: {
            dist: ['dist'],
            compress: ['archive.zip'],
            server: '.tmp'
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
                files: ['less/*.less'],
                tasks: ['recess']
            },
            js: {
                files: ['scripts/{,*/}*.js'],
                tasks: ['newer:jshint:all'],
                options: {
                    livereload: '<%= connect.options.livereload %>'
                }
            },
            gruntfile: {
                files: ['Gruntfile.js']
            },
            livereload: {
                options: {
                    livereload: '<%= connect.options.livereload %>'
                },
                files: [
                    '{,*/}*.html',
                    '.tmp/styles/{,*/}*.css',
                    'styles/{,*/}*.css',
                    'images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
                ]
            }
        },

        connect: {
            options: {
                port: 9000,
                // Change this to '0.0.0.0' to access the server from outside.
                hostname: 'localhost',
                livereload: 35729
            },
            livereload: {
                options: {
                    open: true,
                    base: './',
                    middleware: function (connect) {
                        return [
                            connect.static('.tmp'),
                            connect.static(require('path').resolve('.'))
                        ];
                    }
                }
            },
            dist: {
                options: {
                    open: true,
                    base: './'
                }
            }
        },

        compress: {
            main: {
                options: {
                    archive: 'archive.zip'
                },
                files: [
                    {src: ['css/*', 'js/**', 'fonts/**', 'images/**', 'locales/**', 'favicon.ico']},
                    {src: ['landing.html.*']}]
            }
        },

        mustache_render: {
            de: {
                files: [
                    {
                        data: "locales/de.json",
                        template: "landing.html",
                        dest: "landing.html.de"
                    },
                    {
                        data: "locales/de.json",
                        template: "index.html",
                        dest: "index.html.de"
                    }
                ]
            },
            en: {
                files: [
                    {
                        data: "locales/en.json",
                        template: "landing.html",
                        dest: "landing.html.en"
                    },
                    {
                        data: "locales/en.json",
                        template: "index.html",
                        dest: "index.html.en"
                    }
                ]
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

    grunt.registerTask('serve', 'Compile then start a connect web server', function (target) {
        if (target === 'dist') {
            return grunt.task.run(['build', 'connect:dist:keepalive']);
        }

        grunt.task.run([
            'clean:server',
            'connect:livereload',
            'watch'
        ]);
    });

    grunt.registerTask('server', 'DEPRECATED TASK. Use the "serve" task instead', function (target) {
        grunt.log.warn('The `server` task has been deprecated. Use `grunt serve` to start a server.');
        grunt.task.run(['serve:' + target]);
    });


    // CSS distribution task.
    grunt.registerTask('dist-css', ['recess']);

    // Full distribution task.
    grunt.registerTask('build', ['clean', 'dist-css', 'mustache_render', 'compress']);

    grunt.registerTask('dist', ['build']);


    // Default task.
    grunt.registerTask('default', ['dist']);

};
