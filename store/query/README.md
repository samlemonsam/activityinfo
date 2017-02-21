
# ActivityInfo Data Store Query Engine

The Query Engine executes analytical queries over ActivityInfo forms.

Queries are defined by a [ColumnModel](../../core/model/src/main/java/org/activityinfo/model/query/QueryModel.java),
and return a [ColumnSet](../../core/model/src/main/java/org/activityinfo/model/query/ColumnSet.java).

Query execution proceeds in several phases:

1. The ColumnModel is parsed and each ColumnModel, which may a formula expression
   itself referencing one or more fields, is matched to one or more 
   Form Fields. The result of this stage is a the FormScanBatch, which tracks
   which fields are needed from each form.
  
2. Memcache is first queried for columns that are already present in
   the cache. 
   
3. Any Forms in FormScanBatch with non-cached columns are scanned, and 
   a complete column vector is created for each field, and (asynchronously)
   sent to Memcache.
   
4. Any non-cached joining is performed.

5. Query *and* permission filters are applied to columns.

Because permission filters are applied only at the final stage, 
most queries can be resolved using the cache and need not be user-specific,
even if hundreds of users each with their own permissions are querying 
a dataset at once. 

