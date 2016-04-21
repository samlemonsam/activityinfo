
# Data Model

At its heart, ActivityInfo is a database that allows users to
manage and share rich, but structured data.

The following concepts provide a uniform model for dealing with any all
data that is managed by ActivityInfo, from user-defined forms to 
saved report definitions.

## Records

The basic building block of data in ActivityInfo is the `Record`, 
a set of named fields and their values that can be represented as a JSON object:

```
{ 
   "@id": "myobject",
   "stringField": "string value",
   "numberField": 134.0,
   "booleanField": false,
   "recordField": {
      "nestedField": "nested value"
   },
   "arrayField": {
      "item1",
      "item2",
   }
}
```

Like JSON objects, the value of record field may have one of several types:

* string
* number
* boolean
* record (nested)
* array

Compared to a JSON object, a Record has a few additional restrictions:

* Fields may be missing, but not `null`
* Strings may not be zero-length

Records also have several "special" fields that provide common identify
and lifecyle information. For example, for records that have an identity, 
the `@id` field is a string containing an identifier.

No other fields but these may begin with an '@' character.

**TODO(dev)**: Merge Resource, FormInstance, and Record Java classes to a single class.
  
## Record Schemas

Record Schemas impose constraints and provide meaning to the fields of
a record they describe. 

**TODO(dev)**: Rename FormClass to RecordSchema??


A Record Schema is itself a `Record` and can be represented as JSON or YAML.

A Record Schema might be application-defined, such as the schema for a Geographic
Point:


```.yaml
  @id: geopoint
  label: Geographic Point
  elements:
    - @id: longitude
      type: field
      label: Longitude
      required: true
      fieldType: quantity
      fieldTypeParameters:
        units: degrees
    - @id: latitude
      type: field
      label: Latitude
      required: true
      fieldType: quantity
      fieldTypeParameters:
        units: degrees
      
```

But it might just as well be defined by a user through the API or the visual form designer:

```.yaml
 @id: collections/4a0f92b6-c9cf-4d4c-ae6a-7b8cea9a67af/schema
 label: School Construction
 elements: 
  - @id: 076595f2-2b10-4308-a5a3-5b2f3a4e9ea0
    type: field
    label: Date of Construction
    code: startDate
    required: true
    fieldType: localDate
  - @id: 2c0cf41c-38e6-4d80-94b6-824283cb7411
    type: field
    label: Number of classrooms
    code: classrooms
    required: true
    fieldType: quantity
    fieldTypeParameters: 
      units: classrooms
      
```

User-defined schemas and their fields are identified by UUIDs, which allow offline clients to generate new ids 
without concern for overlap between clients.

The `code` property allows users to set machine-readable but human-friendly identifiers that can be used in 
expressions or in API calls.

Each field definition in the `RecordSchema` has the following properties:

  * **@id**: the field's unique, immutable id. 
  * **label:** Short human-readable label for the field
  * **code:** Short machine-readable but human-friendly identifier matching the pattern
     [A-Za-z][A-Za-z0-9_]* that can be used in expressions. 
  * **description:** Longer description of the field providing context or 
    instructions to the user
  * **required:** boolean. `true` if the field must always be present
  * **visible:** boolean. `true` if the field is visible during data entry
  * **relevanceCondition:** an boolean-valued expression to that determinens
     whether the field is relevant.
  * **type:** the type of the fields values.
  * **typeParameters:** A record with the fields that further refine the given
    `type`, by constraining values, or by further specifying the appearance of the
    user interface for the field.
    
## Field Types

Field types are provided by the application and play a central role in ActivityInfo. They not 
only provide structure and validation, but also make it possible to build intelligent user
interfaces from the description alone.

Field types are identified by camel-cased type names that are defined by the application. Users
cannot create new field types.

They include:

| Field Type Id     | Description                     | Type Parameters    | Representation | 
|-------------------|---------------------------------|--------------------|----------------|
| text              | Single line of unicode text     |                    | string         |
| narrative         | Multi-line unicode text         |                    | string         |
| barcode           | Barcode value to be scanned     |                    | string         |
| quantity          | Numeric value                   | units              | number         |
| enum              | Choice of given items           | items, cardinality | array          |
| localDate         | Day in ISO-8601 Calendar        |                    | string         |
| localDateInterval | Interval between days           |                    | record         |
| month             | Month in ISO-8601 Calendar      |                    | string         |
| year              | Year in ISO-8601 Calendar       |                    | number         |
| calculated        | Calculated Value                | expression         |                |
| reference         | References items in collections | range, cardinality | string, array  |
| subform           | References dependent collection | collection         |                |
| attachment        | Uploaded files                  | kind, cardinality  | record         |
| geopoint          | WGS 84 Coordinate               |                    | record         |
| geoarea           | WGS 84 Polygon or Multipolygon  |                    | record         |
 
## Collection

A Collection is a set of zero or more `Records` that share a common Record Schema.

Collections may be created by Users (e.g. Activities, Location Types, Forms), or they may be
defined by the application (e.g. Countries, Admin Levels, Saved Reports, Registered Users, etc)


