This is a plugin for Gradle <http://gradle.org/> which lets you use Sass <http://sass-lang.com/> in your build.

To use it, add it to your buildscript classpath::

	buildscript {
	    dependencies {
	        classpath "com.timgroup:SassPlugin:1.1.1120"
	    }
	}

And apply the plugin::

	apply plugin: 'com.youdevise.sass'

You will then have a task called `compileSass` which you can run without further ado. It will find Sass files (Sass rather than SCSS - probably) in an input directory, and compile them into CSS files in an output directory, preserving the directory hierarchy of the files as it does so.

By default, `compileSass` looks for input in `src/main/sass`, and emits output into `build/sass`. This can be customised with the task's `inputDir` and `outputDir` properties::

	compileSass {
	    inputDir = file('web/sass')
	    outputDir = new File(project.buildDir, 'sass-css')
	}

You can also configure the location of the Sass compiler's cache if you like::

	compileSass {
	    cacheLocation = file('/var/lib/sass/cache')
	}

The `compileSass` task is an instance of `com.youdevise.gradle.plugins.CompileSassTask`; if you want to compile multiple sets of Sass, you can simply create more instances of this.
