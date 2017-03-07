
# GWT-RPC Client

This modules contains a client based on the [GWT Remote Procedure Call](http://www.gwtproject.org/doc/latest/tutorial/RPC.html)
system as well as client-side implementations of a subset of the commands
to support offline mode.

The Command and CommandResult classes are annotated with Jackson 1.x 
annotations to allow the commands to be invoked via a JSON-RPC endpoint
from the R client.
