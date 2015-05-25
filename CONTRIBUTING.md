
# Contributor's Guide
 
ActivityInfo is an open-source web application with a single-page javascript interface
built using Java and the Google Web Toolkit, and designed to run on the Google AppEngine
Platform. 


## Workflow

All development is planned through our [JIRA project](https://bedatadriven.atlassian.net/secure/RapidBoard.jspa?rapidView=9) and submitted as pull requests to our 
[GitHub Repository](https://github.com/bedatadriven/activityinfo).

## Setting up Development Environment

Contributing to AI development requires the following tools installed on your development
machine:

* [Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 7 or 8 
* [MySQL Server 5.5](https://dev.mysql.com/downloads/mysql/5.5.html)


### Gradle

ActivityInfo uses the [Gradle](https://gradle.org) build automation tool to manage the 
building, testing, and deployment of ActivityInfo.

Gradle is invoked from the command line using a wrapper that is included in the source
repository. To invoke a gradle task, open a Terminal and change to the root of the project,
and then enter:

```
./gradlew --tasks
```

This will display a list of build, test, and deployment tasks.

### MySQL


Gradle will create the MySQL databases needed for testing and development automatically,
but you must provide the username and password gradle will use to connect to your local
MySQL server. 

Since these properties are developer-specific, they should be set in your local
[gradle.properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties), for example:

```
localMySqlUsername=root
localMySqlPassword=mysecretpassword
```

### Verifying your setup

Double-check that you have Java 1.7 installed:

```
$ java -version
java version "1.7.0_80"
Java(TM) SE Runtime Environment (build 1.7.0_80-b15)
Java HotSpot(TM) 64-Bit Server VM (build 24.80-b11, mixed mode)
```

And then run the unit tests:

```
$ ./gradlew test
```

### Configuring IntelliJ

ActivityInfo is primarily developed using the IntelliJ IDE from JetBrains, who have
graciously provided the project with an open source license for their IDE. Active contributors
to AI may request a license key from [Alex Bertram](mailto:alex@bedatadriven.com).

It should certainly be possible to use other IDEs, but we only provide instructions for
IntelliJ at this time.

Before opening IntelliJ, you can generate the IntelliJ project file by running:

```
./gradlew idea
```

This will create an `activityinfo.ipr` file that you can open with IntelliJ.

## Running Locally

Invoking

```
$ ./gradlew appengineRun
```

from the command line will fully build the application, initialize a local database called `activityinfo_dev` if
one does not exist, and then start the AppEngine Development Server.

By default, the application will serve from http://localhost:8080/, but you can change the port by setting the
`devServerPort` Gradle property.



