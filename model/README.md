# Model

The `model` module defines the set of data objects within the ActivityInfo system. These objects include user-facing 
objects such as [Forms](src/main/java/org/activityinfo/model/form/FormClass.java) and 
[Records](src/main/java/org/activityinfo/model/form/FormRecord.java), as well as system objects such as 
[QueryModels](src/main/java/org/activityinfo/model/query/QueryModel.java) and 
[GrantModels](src/main/java/org/activityinfo/model/permission/GrantModel.java).

Model objects can be shared between the server and client.

## Module Structure

The `model` module is split into separate packages for each data type:

* [account](src/main/java/org/activityinfo/model/account/): The metadata for a User's billing account
* [analysis](src/main/java/org/activityinfo/model/analysis/): The metadata for an analysis performed on one or more Forms
* [api](src/main/java/org/activityinfo/model/api/): The metadata for an API call
* [database](src/main/java/org/activityinfo/model/database/): The metadata for a User's Database 
* [date](src/main/java/org/activityinfo/model/date/): Date model in ActivityInfo
* [error](src/main/java/org/activityinfo/model/error/): Errors which may occur when calling to the ActivityInfo API 
* [form](src/main/java/org/activityinfo/model/form/): The metadata for a User-designed flexible Form 
* [formTree](src/main/java/org/activityinfo/model/formTree/): The set of Form metadata required to resolve all data in a Form 
* [formula](src/main/java/org/activityinfo/model/formula/): Formulas which allow for referencing of Fields and Forms, and performing calculations on data
* [job](src/main/java/org/activityinfo/model/job/): Long-running jobs 
* [legacy](src/main/java/org/activityinfo/model/legacy/): Adapters for legacy data 
* [permission](src/main/java/org/activityinfo/model/permission/): Define a Users permissions
* [query](src/main/java/org/activityinfo/model/query/): Defines a User query for data from a set of Forms 
* [resource](src/main/java/org/activityinfo/model/resource/): The metadata for a Database resource 
* [type](src/main/java/org/activityinfo/model/type/): The various Field types which can compose a Form 
* [util](src/main/java/org/activityinfo/model/account/): Utility classes and methods  
