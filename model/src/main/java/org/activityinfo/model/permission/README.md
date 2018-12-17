# Permissions Model

The ActivityInfo Permissions Subsystem is responsible for ensuring that Users are only able to perform operations which 
have been permitted by the Database Owner.

## Model Classes
### Operation

The operations for which permissions are enforced are defined in the [Operation](Operation.java) Enum. These include, 
but are not limited to: 
* `VIEW_RESOURCE`: Viewing a Resource.
* `VIEW_RECORDS`: Viewing Records within a Resource.
* `EXPORT_RECORDS`: Exporting Records from a Resource.
* `MANAGE_USERS`: Managing Users on a Resource.

Operations define Resource-level permissions. Operations can be combined with filters, which allow the Database Owner to 
define further Record-level restrictions which must be applied. 

### GrantModel

A [GrantModel](GrantModel.java) defines the set of allowed Operations (and their optional filters) on a specific 
Resource. A GrantModel is inherited by any child Resources. Any Operation which are not defined on a GrantModel are not 
allowed to be performed by the User.

As well as a binary permission (can perform/cannot perform), the Database Owner may set a filter which should be 
applied when performing an Operation. These filters allow the Database Owner to limit the data the User is allowed to 
view or operate on at a Record level. Filters are composed of [formulas](../formula/). 

For example, a User may be limited to viewing data which is associated with a specific Partner field. In this instance, 
the User's `VIEW_RECORD` field will include a `P0000001234 == "p0000004321"` filter, specifying that they are only 
allowed to view records which are associated with the Form Record `p00000043211` in the _referenced_ Form `P0000001234`.

A collection of GrantModels compose a [DatabaseGrant](../database/DatabaseGrant.java). A DatabaseGrant gives the full 
set of allowed Operations for a specific User, and their filters, across all of the Resources within a Database.
