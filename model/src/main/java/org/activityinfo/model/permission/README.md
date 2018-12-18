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


