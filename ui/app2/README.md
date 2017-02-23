
# ActivityInfo Desktop Application 2.0

This module contains the legacy (2.x) ActivityInfo user interface
for desktop/laptop users. 

This version is based on [GWT 2.6](http://www.gwtproject.org/) and 
[GXT 2.x](https://github.com/bedatadriven/gxt).
 
This version is maintenance mode. New development is taking place in
[ui:app](../app).

## Development

GWT Dev Mode can be started in two steps:

    ./gradlew server:appengineDevMode
    
And then navigating to [http://127.0.0.1:8080/login?gwt.codesvr=127.0.0.1:9997](http://127.0.0.1:8080/login?gwt.codesvr=127.0.0.1:9997)
with Firefox 24. 


*Note:* this version of the application uses the "classic" GWT Dev Mode 
which requires a browser plugin to function. This browser
plugin no longer works on the most recent versions of most browsers; 
we recommend installing a local copy of
[FireFox 24.2.0esr](https://ftp.mozilla.org/pub/mozilla.org/firefox/releases/24.2.0esr/) and following this 
[tutorial](https://openbpm.wordpress.com/2014/05/31/getting-gwt-plugin-to-work-on-firefox-on-ubuntu-14-04/) 
to setup a seperate FireFox profile for testing ActivityInfo. 

