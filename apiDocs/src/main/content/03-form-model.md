
# Form Model

The core concept in ActivityInfo is the Form. Users can create
their own forms, by defining the fields that belong to that form. 

ActivityInfo forms are analogous to "tables" in relational databases,
but support richer field types. 
  
Each FormField has a number of common properties:

| Property       | Type     | Required  | Description |
| -------------- | -------  |  -------- | ----------- |
| label          | string   | Yes       | A concise, human-readable label for the field. |
| description    | string   | No        | A longer, more complete description of the field that may include instructions. |
| code           | string   | No        | A short identifier that can be used to reference the field in the API and in formulas |
| required       | string   | Yes       | If true, then a value *must* be supplied for this field |
| relevanceCondition | string | No      | If provided, a boolean formula that determines the field is relevant |
| type           | string   | Yes       | The field's type. (see below) |
| typeParameters | object   | No        | Additional properties specific to the field's type. (see below) | 

## Field Types

Varying field types allow users to collect a rich set of data and 
enforce data quality rules. 


### text

The text field type allows up to 1024 characters of text on a single line. 

They are encoded in input and output as JSON strings.

### narrative

The `narrative` field type allows up to 4096 characters of text over multiple lines.

`narrative` values are encoded in input and output as JSON strings.

### quantity

The `quantity` field type allows the input of real-valued numbers. 

| Parameter         | Type     | Description
| ----------------- | -------- | --------------------
| units             | string   | Human-readable unit of measure. Used only for display purposes 

They are encoded in input and output as JSON numbers.

### date

The `date` field type represents a value in the [ISO-8601 calendar](https://en.wikipedia.org/wiki/ISO_8601)

`date` values are encoded as JSON strings in the format "YYYY-MM-dd" 
 
### enumerated

The `enumerated` field type represents a choice between a set of 
pre-defined values. 

| Parameter         | Type     | Description
| ----------------- | -------- | --------------------
| cardinality       | string   | Either "single" or "multiple"
| values            | array    | An array of choices for the field.

`enumerated` choices are defined by both an id, which can never change,
and a human-readable label, which _can_ be changed. 

A field that allows the user to choose the donor of a School Reconstruction,
for example, could be described as follows:

```
 {
      "id": "ciwz6q9tkb",
      "label": "Donor",
      "visible": true,
      "required": true,
      "type": "enumerated",
      "typeParameters": {
        "cardinality": "single",
        "values": [
          {
            "id": "ciwz6q9tlc",
            "label": "USAID"
          },
          {
            "id": "ciwz6q9tld",
            "label": "DFID"
          },
          {
            "id": "ciwz6q9tle",
            "label": "ECHO"
          }
        ]
      }
    }
```


### geopoint

The geographic point type represents a point on the earth's surface as 
a latitude / longitude pair within the [WGS 84](https://en.wikipedia.org/wiki/World_Geodetic_System)
coordinate reference system.

`geopoint` values are encoded as JSON objects in the form `{ longitude: number, latitude: number }`,
for example:

```
{
  latitude: 30.425,
  longitude: -136.325
}
```

### geoarea

The geographic area type represents an area on the earth's surface described
by a polygon.
 
### calculated

Calculated fields do not store a value of their own but rather calculate their value
based on the values of other fields in the same record.

| Parameter         | Type     | Description
| ----------------- | -------- | --------------------
| formula           | string   | Formula used to calculate the field's value

### reference 

Reference fields refer to records in other Forms, effectively creating 
relationships between forms. 

The "range" parameter of a reference field is the set of forms to
which the field can refer. 

For example, a user might define two forms in their database: 
a "School" form and a "Student" form. 

To link each "Student" record to the "School" record that s/he attends,
the user could add a reference field to the Student Form, with the "School" 
form as its range:

```
{ 
  label: "School",
  required: true,
  type: "reference",
  typeParameters: {
    range: [
        { 
           formId: "school_form"
        }
    ]
}
```


### attachment

The `attachment` field allows users to upload and store arbitrary documents
or files in a form field.

| Parameter         | Type     | Description
| ----------------- | -------- | --------------------
| cardinality       | string   | Either "single" or "multiple"
| kind              | array    | Either "image" or "attachment"

