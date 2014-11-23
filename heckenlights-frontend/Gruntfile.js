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

        appConfig: appConfig,

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

        less: {
            compileCore: {
                options: {
                    strictMath: false,
                    sourceMap: true,
                    outputSourceFiles: true,
                    sourceMapURL: '<%= pkg.name %>.css.map',
                    sourceMapFilename: '<%= appConfig.dist %>/css/<%= pkg.name %>.css.map'
                },
                src: 'less/site.less',
                dest: '<%= appConfig.dist %>/css/<%= pkg.name %>.css'
            },
            compileBootstrap: {
                options: {
                    strictMath: false,
                    sourceMap: true,
                    outputSourceFiles: true,
                    sourceMapURL: 'bootstrap.css.map',
                    sourceMapFilename: '<%= appConfig.dist %>/css/bootstrap.css.map'
                },
                src: 'less/bootstrap.less',
                dest: '<%= appConfig.dist %>/css/bootstrap.css'
            },
            compileTheme: {
                options: {
                    strictMath: false,
                    sourceMap: true,
                    outputSourceFiles: true,
                    sourceMapURL: '<%= pkg.name %>-theme.css.map',
                    sourceMapFilename: '<%= appConfig.dist %>/css/<%= pkg.name %>-theme.css.map'
                },
                src: 'less/theme.less',
                dest: '<%= appConfig.dist %>/css/<%= pkg.name %>-theme.css'
            }
        },

        cssmin: {
            options: {
                compatibility: 'ie8',
                keepSpecialComments: '*',
                noAdvanced: true
            },
            minifyCore: {
                src: '<%= appConfig.dist %>/css/<%= pkg.name %>.css',
                dest: '<%= appConfig.dist %>/css/<%= pkg.name %>.min.css'
            },
            minifyBootstrap: {
                src: '<%= appConfig.dist %>/css/bootstrap.css',
                dest: '<%= appConfig.dist %>/css/bootstrap.min.css'
            },
            minifyTheme: {
                src: '<%= appConfig.dist %>/css/<%= pkg.name %>-theme.css',
                dest: '<%= appConfig.dist %>/css/<%= pkg.name %>-theme.min.css'
            }
        },

        csslint: {
            options: {
                csslintrc: 'less/.csslintrc'
            },
            dist: [
                '<%= appConfig.dist %>/css/<%= pkg.name %>.css'
            ]
        },

        // Renames files for browser caching purposes
        filerev: {
            dist: {
                src: [
                    '<%= appConfig.dist %>/js/{,*/}*.js',
                    '<%= appConfig.dist %>/css/{,*/}*.css',
                    '<%= appConfig.dist %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
                ]
            }
        },

        // Add vendor prefixed styles
        autoprefixer: {
            options: {
                browsers: ['last 1 version']
            },
            dist: {
                files: [
                    {
                        expand: true,
                        cwd: '.tmp/styles/',
                        src: '{,*/}*.css',
                        dest: '.tmp/styles/'
                    }
                ]
            }
        },

        // Automatically inject Bower components into the app
        wiredep: {
            app: {
                src: ['*.html.*'],
                ignorePath: /\.\.\//
            }
        },

        // Reads HTML for usemin blocks to enable smart builds that automatically
        // concat, minify and revision files. Creates configurations in memory so
        // additional tasks can operate on them
        useminPrepare: {
            html: '*.html.*',
            options: {
                dest: '<%= appConfig.dist %>',
                flow: {
                    html: {
                        steps: {
                            js: ['concat', 'uglifyjs'],
                            css: ['cssmin']
                        },
                        post: {}
                    }
                }
            }
        },

        // Performs rewrites based on filerev and the useminPrepare configuration
        usemin: {
            html: ['<%= appConfig.dist %>/*.html.*'],
            css: ['<%= appConfig.dist %>/css/*.css'],
            options: {
                assetsDirs: ['./', '<%= appConfig.dist %>', '<%= appConfig.dist %>/js', '<%= appConfig.dist %>/images']
            }
        },

        copy: {
            html: {
                files: [
                    {
                        expand: true,
                        dot: true,
                        cwd: './',
                        dest: '<%= appConfig.dist %>',
                        src: [
                            '*.html.*', 'fonts/*.*', 'locales/*.*', '.htaccess', 'app/**'
                        ]
                    }
                ]
            },
            connect: {
                files: [
                    {
                        expand: true,
                        dot: true,
                        cwd: './',
                        dest: '.tmp',
                        src: [
                            '*.html.*'
                        ]
                    },
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= appConfig.dist %>/css',
                        dest: '.tmp',
                        src: [
                            '*.css'
                        ]
                    }
                ]
            },
            resources: {
                files: [
                    {
                        expand: true,
                        dot: true,
                        cwd: './',
                        dest: '<%= appConfig.dist %>',
                        src: [
                            'favicon.ico',
                            'robots.txt',
                            'humans.txt',
                            '.htaccess',
                            'images/*',
                            'app/*',
                        ]
                    }
                ]
            }
        },

        watch: {
            less: {
                files: 'less/**/*.less',
                tasks: ['less-compile', 'copy:html'],
                options: {
                    livereload: '<%= connect.options.livereload %>'
                }
            },
            html: {
                files: '*.html',
                tasks: ['mustache_render', 'copy:html'],
                options: {
                    livereload: '<%= connect.options.livereload %>'
                }
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
                            connect().use(function (req, res, next) {
                                if (req.url.indexOf('.html.') > -1) {
                                    res.setHeader("Content-Type", "text/html");
                                }
                                next();
                            }),
                            connect().use('/locales', connect.static('./locales')),
                            connect().use('/images', connect.static('./images')),
                            connect().use('/css', connect.static('./dist/css')),
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
                    {
                        expand: true,
                        cwd: '<%= appConfig.dist %>',
                        src: ['**']
                    }
                ]
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

    grunt.registerTask('serve', 'Compile then start a connect web server', function (target) {
        if (target === 'dist') {
            return grunt.task.run(['build', 'connect:dist:keepalive']);
        }

        grunt.task.run([
            'clean:server',
            'autoprefixer',
            'less-compile',
            'copy:html',
            'autoprefixer',
            'connect:livereload',
            'watch'
        ]);
    });

    grunt.registerTask('server', 'DEPRECATED TASK. Use the "serve" task instead', function (target) {
        grunt.log.warn('The `server` task has been deprecated. Use `grunt serve` to start a server.');
        grunt.task.run(['serve:' + target]);
    });

    grunt.registerTask('less-compile', ['less']);

    // CSS distribution task.
    grunt.registerTask('dist-css', ['less-compile', 'cssmin']);

    // Full distribution task.
    grunt.registerTask('build', ['clean', 'mustache_render', 'copy:html', 'wiredep', 'useminPrepare', 'autoprefixer', 'concat', 'dist-css', 'uglify', 'filerev', 'usemin', 'copy:resources']);

    grunt.registerTask('dist', ['build', 'compress']);

    // Default task.
    grunt.registerTask('default', ['dist']);
    grunt.registerTask('config', 'Config', function () {
        grunt.log.writeln(JSON.stringify(grunt.config(), null, 2));
    });

};
