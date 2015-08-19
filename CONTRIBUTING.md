
# Contributing to ActivityInfo

## Fork the project

Contributes to the ActivityInfo source are made through pull requests on GitHub. To submit a pull request:

1. [Create a GitHub account](https://github.com/join) if you don't have one already
2. Fork the [bedatadriven/activityinfo](https://github.com/bedatadriven/activityinfo) repository

If you're not familiar with Git or GitHub, read GitHub's [Forking Projects Guide](https://guides.github.com/activities/forking/).

Checkout your forked sources to a local directory. We'll refer to this directory as $ACTIVITYINFO for the rest
of the document.

## Setting up your Development Environment

The next step is to set up an environment on your own machine where you 
can make changes to the source code, test those changes quickly, and run automated tests to confirm that you haven't
broken anything.

### Installing Java

You will need a recent version of a Java Development Kit (JDK) running on your local machine. You can download
this from [Oracle](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

### Installing MySQL

ActivityInfo uses MySql 5.5 for storage, and you will need a locally running database to run 
and test the application locally.

Read the MySql documentation on [setting up MySql](https://dev.mysql.com/doc/refman/5.5/en/installing.html) on your
local machine.

### Defining Gradle Properties

ActivityInfo uses Gradle for building, testing and deployment. Gradle needs certain information about your local
environment in order to run, and these properties are stored in your local gradle.properties file, which you can find:

* On Linux and OS X systems: /home/username/.gradle/gradle.properties
* On Windows: C:\Users\username\.gradle\gradle.properties

You should create or add to this text file the following lines:


    localMySqlUsername=root
    localMySqlPassword=<the password you set when installing mysql>

See the [$ACTIVITYINFO/gradle.properties](gradle.properties) file for other properties that can be changed.

### Running Gradle

To confirm that your environment is set up correctly, open a terminal in your $ACTIVITYINFO directory and run:

    $ ./gradlew test

If you are using Windows, open the Command Prompt and run:

    > gradlew.bat test
    
Gradle should complete with the message:

    BUILD SUCCESSFUL
    
## Running ActivityInfo in Development Mode

ActivityInfo's user interface is built using [GWT](http://gwtproject.org), a toolchain which compiles Java
source code to Javascript, which then runs in the browser as a 
[single-page application](https://en.wikipedia.org/wiki/Single-page_application).

GWT provides a [development mode](http://www.gwtproject.org/doc/latest/DevGuideCompilingAndDebugging.html)
that allows you to interactively make changes to the source code, and then refresh
the browser to see the result.

*Note:* we are still using the "classic" GWT Dev Mode which requires a browser plugin to function. This browser
plugin no longer works on the most recent versions of most browsers; we recommend installing a local copy of
[FireFox 24.2.0esr](https://ftp.mozilla.org/pub/mozilla.org/firefox/releases/24.2.0esr/) and following this 
[tutorial](https://openbpm.wordpress.com/2014/05/31/getting-gwt-plugin-to-work-on-firefox-on-ubuntu-14-04/) to setup
a seperate FireFox profile for testing ActivityInfo. We expect to transition to GWT 2.7 and the new "Super" Dev Mode 
before the end of 2015.

The very first time you run AI locally, you'll need to create an `activityinfo_dev` database by running:

    $ ./gradlew setupDevDatabase
    
This will create the required MySQL tables and populate the database with a bare minimum of geographic reference
data to run AI.

You can start development mode from Gradle by running:

    $ ./gradlew appengineDevMode
    
And then navigating to [http://127.0.0.1:8080/login?gwt.codesvr=127.0.0.1:9997](http://127.0.0.1:8080/login?gwt.codesvr=127.0.0.1:9997)
with Firefox 24. 

You can log in with the account `test@test.org` and any password.




