
# Server

The ActivityInfo Server is designed to be run as a Google AppEngine 
Service, which mostly built on the Java Servlet API.

The server component is responsible for: 

* Sign up, Login, and Authentication
* Serving API requests (GWT-RPC, REST, and ODK, etc)
* Serving GeoJson
* Rending PDF/Word/Doc Reports and Exports
* Sending daily/weekly email digests and scheduled reports

The result of the server build also includes the compiled javascript
for the front end applications and the generated API documentation.

## Gradle Tasks


### appengineDevMode

Run the local AppEngine server for local development

### archive

Build the versioned WAR and archive on Google Cloud Storage for subsequent
deployment and testing.

