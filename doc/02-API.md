
# API

The key elements of the ActivityInfo data model are [Records, Schema, and Collections](01-Data-Model.md).

ActivityInfo provides a number of APIs to allow ActivityInfo's front-end as well as external application
developers to interact with collections.

# Listing Collections

TODO


# Creating Collections

TODO

# Fetching Resources

A single resource can be fetched by it's identifier.


# Creating and Updating Resources

Individual resources can be created, update, or deleted through the Update API. The Update API 
provides a single endpoint and syntax for updating resources in ActivityInfo.

`POST /update`

Request Body:

```.json
{
    "changes": [
        
        {
            "@id": "s14",
            "startDate": "2015-01-01",
            "endDate": "2015-02-01",
            "comments": "Updated comment" 
        },
        
        {
            "@id": "collection/a33/schema",
            "fields": [
                { 
                    "@id": "F124343",
                    "label": "New Field",
                    "type": "quantity",
                    "typeParameters": {
                        "units": "Households"
                    }
               }
           ]
        }
    ]
}
```

In the example above, the first change to a site object is similar to the `UpdateSite` command from the 
ActivityInfo 2.0 API, while the second change is comparable to an execution of a `CreateIndicator` command.

## Creating new Resources

This API is also used for creating new resources. In this case, the client is responsible for generating a 
globally unique ID.

TODO: UUID or random numbers? Legacy collections?

## Field Identifiers

For the convience of external developers working with user-defined forms, submitted changes can reference fields
either using a field's unique identifier, or using the more human-friendly `code` property of a field.

# Querying Collections

## Querying tables

The Table Query API allows developers to select a flattened, two-dimension view of a collection.

`POST /tableQuery`


```.json
{
    rowSources: [
        {  "collection": "a33" }
    ],
    format: "columns",
    columns: {
        "id": "@id",
        "partner": "partner.name",
        "project": "project.path"    
    },
    filter: "province.name = 'Nord Kivu'"
}
```

The result of the table query depends on the `format` property. The "rows" format returns array containing a
a JSON object for each row:

```
[
    {
        "id": "s14",
        "partner": "NRC",
        "project": "USAID"
    },
    {
        "id": "s15",
        "partner": "ARC",
        "project": "USAID"
    }
]
```

The "columns" format returns a `ColumnSet` object, which is better suited to analysis:

```.json
{
    "numRows": 2,
    "columns":  {
        
        "id": {
            "type": "string",
            "storage": "array",
            "values": ["s14", "s15" ]
        },
        
        "partner": {
            "type": "string",
            "storage": "array",
            "values": ["NRC", "ARC" ]
        },
        "project": {
            "type": "string",
            "storage": "constant",
            "value": "USAID"
        }
    }
}
```

For the convenience of external developers, there is also a simplified GET endpoint for the query API.

` GET /collections/{collectionId}/query/columns?id=@id&partner=partner.name&projet=project.name`

## Querying graphs

TODO


# Synchronizing Collections

For versioned collections, the API allows you to fetch request changes relative to your local version.

```
    { 
        "collection": "a33",
        "localVersion": "102323445554"
    }
```

Response:


    {
        "collection": "a33",
        "version":  "102323445901",
        "complete": true,
        "dependencies": [
            {
                "collection": "MG/provinces",
                "version": "102323445901"
            }
        ],
        "changes": [
        
            {
                "@id": "s14",
                "startDate": "2015-04-01",
                "comments": "Updated comments",
                "partner": null
            },
            {  
                "@id": "s523",
                "@deleted": true
            }
        ]
    }
```

If the collection depends on other collections through a `Reference Field`, then 