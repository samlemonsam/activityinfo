
# ActivityInfo Desktop Application

This module contains the upcoming "3.0" ActivityInfo user interface
for desktop/laptop users. 

This new version is built with Java 8, [GWT 2.8](http://www.gwtproject.org/), 
[GXT 4.0](https://www.sencha.com/products/gxt/#overview) and built around
the principles of Functional Reactive Programming (FRP). 

## Development

GWT SuperDev mode can be started in two steps:

    ./gradlew ui:app:codeserver
    ./gradlew server:appengineDevMode
    
Navigate to http://localhost:8080/login and then http://localhost:8080/app?ui=3dev


