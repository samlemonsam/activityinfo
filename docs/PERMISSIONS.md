# Permissions Subsystem

The ActivityInfo Permissions Subsystem is responsible for ensuring that users are only able to perform operations which have been permitted by the database owner or administrator(s). 

The subsystem is primarily designed to allow for identical enforcement of permissions on both the client and the server. In the past, ad-hoc implementations in different classes, permission logic mixed with model data, and separate code paths on server and client led to a number of divergences and unwanted behaviour when evaluating permissions (e.g. creating a record which is allowed by the UI, only for it to be forbidden on the server and fail). Therefore it was decided to overhaul the permissions system to ensure that a user will experience a seamless and consistent enforcement of permissions. 

To achieve this, the subsystem is divided into three components:
1. Database and User Permission Models
2. Database and User Permission Providers
3. Shared Permission Evaluation Class

## Database and User Permission Models

The Database and User Permission Models define a database's metadata/structure as well as the current user's rights to view and modify the database or the data stored within it. Models are shared between server and client via JSON serialization.

### DatabaseMeta
The [DatabaseMeta](../model/src/main/java/org/activityinfo/model/database/DatabaseMeta.java) class defines the shared metadata of an ActivityInfo database. It includes the database's name, description, forms, locks etc. which are *independent* of any given users view of the data. 

A DatabaseMeta comprises a set of [Resources](../model/src/main/java/org/activityinfo/model/database/Resource.java). Each Resource represents a database element such as a Form, Folder, etc. organised in a Resource Tree (e.g. Form 1 is contained within Folder A, which is contained within Database D). 

The owner of a database always has an unrestricted view of the DatabaseMeta and maintains full ability to modify it.

### Database Grant
The [DatabaseGrant](../model/src/main/java/org/activityinfo/model/database/DatabaseGrant.java) class defines a specific user's *assigned* permissions on an ActivityInfo database. A user will only ever have one DatabaseGrant on a given database. 

A DatabaseGrant comprises a set of [GrantModels](../model/src/main/java/org/activityinfo/model/permission/GrantModel.java). Each GrantModel defines what operations a user is allowed to perform **on a specific Resource and it's children**. A GrantModel also defines the restrictions which must be applied to those operations (e.g. can only view records from Partner "BeDataDriven"). 

A user can have separate GrantModels defined for each Resource, a single GrantModel defined at a given level which is inherited by child Resources (e.g. on a Folder), or a mixture of both. 

### UserDatabaseMeta
The [UserDatabaseMeta](../model/src/main/java/org/activityinfo/model/database/UserDatabaseMeta.java) defines a *specific user's view* of an ActivityInfo database and their permissions within it. Specifically, a UserDatabaseMeta defines the metadata of the Database visible to the User, the Resources visible to the User, the permitted operations on those Resources, and any Locks on the Database. 

To construct a UserDatabaseMeta, the subsystem retrieves the shared DatabaseMeta for the database and the specific user's DatabaseGrant on the database and provides it as input for the UserDatabaseMeta's constructors. 

**NB:** The UserDatabaseMeta is the only class which is intended to be shared between the client and server, as it includes the complete information any given user requires to interact with a given database. 

## Database and User Permission Providers

The Database and User Permission Providers retrieve DatabaseMeta, DatabaseGrants and UserDatabaseMeta from the ActivityInfo store. The provider interfaces provide a layer of abstraction over specific store implementations (HRD, MySQL, etc.).

There are three primary Providers:

**DatabaseMetaProvider/DatabaseMetaLoader**

The [DatabaseMetaProvider](../store/spi/src/main/java/org/activityinfo/store/spi/DatabaseMetaProvider.java) retrieves the DatabaseMeta for a given database; or for a set of databases. It organises requests for DatabaseMeta, retrieves extra information from the store required to fetch DatabaseMeta, delegates to the loader, and provides an appropriate response based on the state of the loaded DatabaseMeta. 

The [DatabaseMetaLoader](../store/spi/src/main/java/org/activityinfo/store/spi/DatabaseMetaLoader.java) defines the store loading and caching mechanisms.

Implementations of these interfaces are tied to a specific store implementation (e.g. [HibernateDatabaseMetaProvider](../server/src/main/java/org/activityinfo/server/database/hibernate/HibernateDatabaseMetaProvider.java) and  [HibernateDatabaseMetaLoader](../server/src/main/java/org/activityinfo/server/database/hibernate/HibernateDatabaseMetaLoader.java) are implementations for the MySQL store accessed via the Hibernate API). 

**DatabaseGrantProvider/DatabaseGrantLoader**

The [DatabaseGrantProvider](../store/spi/src/main/java/org/activityinfo/store/spi/DatabaseGrantProvider.java) retrieves the DatabaseGrant(s) for a specific user on a given database; for all assigned users on a given database; or for a specific user on all assigned databases. It organises requests for DatabaseGrants,retrieves extra information from the store required to fetch DatabaseGrants, delegates to the loader, and provides an appropriate response based on the state of the loaded DatabaseGrants. 

The [DatabaseGrantLoader](../store/spi/src/main/java/org/activityinfo/store/spi/DatabaseGrantLoader.java) defines the store loading and caching mechanisms. 

Implementations of these interfaces are tied to a specific store implementation (e.g. [HibernateDatabaseGrantProvider](../server/src/main/java/org/activityinfo/server/database/hibernate/HibernateDatabaseGrantProvider.java) and  [HibernateDatabaseGrantLoader](../server/src/main/java/org/activityinfo/server/database/hibernate/HibernateDatabaseGrantLoader.java) are implementations for the MySQL store accessed via the Hibernate API). 

**UserDatabaseProvider**

The [UserDatabaseProvider](../store/spi/src/main/java/org/activityinfo/store/spi/UserDatabaseProvider.java) retrieves the full UserDatabaseMeta for a specific user on a given database; for a specific user on a set of databases; or for all databases visible to a specific user. 

The UserDatabaseProvider is exposed as an endpoint in [DatabaseResource](../server/src/main/java/org/activityinfo/server/endpoint/rest/DatabaseResource.java). The client can call to the UserDatabaseProvider by implementing this interface and sending a REST request to this endpoint.

The current implementation of the UserDatabaseProvider, [UserDatabaseProviderImpl](../server/src/main/java/org/activityinfo/server/endpoint/rest/UserDatabaseProviderImpl.java), acts as a control flow node for all UserDatabaseMeta requests:
* When the GeoDatabase or a GeoDatabase resource is requested, the implementation delegates to the [GeoDatabaseProvider](../server/src/main/java/org/activityinfo/server/endpoint/rest/GeoDatabaseProvider.java) to retrieve the UserDatabaseMeta for GeoDatabases. 
* Otherwise, the implementation delegates to the [DesignedDatabaseProvider](../server/src/main/java/org/activityinfo/server/endpoint/rest/DesignedDatabaseProvider.java) to retrieve UserDatabaseMeta for user-designed databases. To do so, it delegates to a DatabaseMetaProvider and DatabaseGrantProvider. It then uses the the retrieved DatabaseMeta and DatabaseGrant(s) to construct the UserDatabaseMeta.

UserDatabaseMeta are not cached themselves, but constructed on-demand from the (potentially) cached DatabaseMeta and DatabaseGrant.

## Shared Permission Evaluation Class

Beyond sharing the *model* data of a database and a user's permissions, it is also necessary to ensure that the code path used to evaluate permissions is identical across the server and client. Introducing separate evaluation classes on client and server has the potential to introduce divergences and errors over time. 

Therefore, the [PermissionOracle](../model/src/main/java/org/activityinfo/model/permission/PermissionOracle.java) is the sole class responsible for evaluating and enforcing permissions. It is a  class of static methods common to both the server and client. 

The PermissionOracle only requires a UserDatabaseMeta in order to determine a user's permission for any action. The PermissionOracle informs the system of whether a user is able to perform a requested Operation on a Resource, and if any restrictions should be applied to the Operation. 

### Permission Subsystem Flow
1. The system should retrieve the UserDatabaseMeta for the current user and database via the DatabaseProvider. 
2. It should then construct a [PermissionQuery](../model/src/main/java/org/activityinfo/model/permission/PermissionQuery.java) to specify the Operation required and the Resource it is to be applied to. 
3. The UserDatabaseMeta and PermissionQuery are then passed to the PermissionOracle::query method, which returns a [Permission](../model/src/main/java/org/activityinfo/model/permission/PermissionQuery.java). 
4. The returned Permission specifies whether:
    1. The Operation is permitted; and
    2. The filter to be applied to the execution of the Operation. The filter defines the record-level restrictions to be enforced (e.g. only view records on Partner "BeDataDriven") 
 
 ### Convenience Methods
 The system can call one of the many convenience methods defined in the PermissionsOracle, which construct an appropriate PermissionQuery for each  Operation type. 
 
 For instance, rather than constructing a PermissionQuery to query whether the user is able to view a form, the system can instead call PermissionOracle.canView(ResourceId,UserDatabaseMeta) giving the UserDatabaseMeta and the form's ResourceId. 
 
 The UserDatabaseMeta is required for any and all calls to a PermissionOracle convenience method.
