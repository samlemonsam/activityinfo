
# Data Store

This directory contains modules related to the ActivityInfo Data Store.

The ActivityInfo Data Store is basically our own database implementation
that combines a few operating characteristics that are vital for 
ActivityInfo but rarely found together:

* Rich, nested, and _flexible_ schemas that can be changed at any time 
  by users.
* Real-time analytics across hundreds of forms/tables/collections with
  together hundreds of thousands of rows.
* "Row" and "field" level permissions
* Synchronization with browser-based databases.

The core of the AI Data Store is:

* [store:spi](spi/) - a Service Provider Interface (SPI) for implementing 
  storage for Forms and their Records.
* [store:query](query/) - a fast, caching, columnar query engine optimized
  for real-time queries.
  
Actual storage is provided by several storage implementations:

* [store:mysql](mysql/) - a storage implementation that maps existing 
  MySQL tables used by ActivityInfo to forms
* [store:hrd](hrd/) - a new storage implementation based on the AppEngine
  High Replication Datastore (HRD) that will eventually become 
  ActivityInfo's primary storage backend.
* [store:testing](testing/) - a simple in-memory storage for unit-testing.


  