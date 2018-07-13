# Usage

The application provides several REST services.
Some of the services can be restricted to scopes.
Currently available scopes are:
* biology
* chemistry
* datetime
* location
* species
* unit

Some services are related to entities.
An entity in this context is:
* data package
* object (e.g. table or media file)
* column
* row
* cell
* meta data property

The following pattern is used to address an entity:

| Package | Object | Column | Row  | Meta | Addressed Entity                                                                   |
|---------|--------|--------|------|------|------------------------------------------------------------------------------------| 
| 1       | null   | null   | null | null | data package 1                                                                     | 
| 1       | null   | null   | null | 1    | meta data properties 1 of data package 1                                           | 
| 1       | 1      | null   | null | null | object 1 in data package 1                                                         | 
| 1       | 1      | null   | null | 1    | meta data properties 1 of object 1 in data package 1                               | 
| 1       | 1      | 1      | null | null | column 1 in object 1 in data package 1                                             | 
| 1       | 1      | 1      | null | 1    | meta data properties 1 of column 1 in object 1 in data package 1                   | 
| 1       | 1      | null   | 1    | null | row 1 in object 1 in data package 1                                                | 
| 1       | 1      | null   | 1    | 1    | meta data properties 1 of row 1 in object 1 in data package 1                      | 
| 1       | 1      | 1      | 1    | null | cell in row 1 and column 1 in object 1 in data package 1                           | 
| 1       | 1      | 1      | 1    | 1    | meta data properties 1 of cell in row 1 and column 1 in object 1 in data package 1 | 

## Suggest Annotations

Request URL: `<base>/annotation/suggest`

Request Method: `POST`

| Name  | Parameter | Type          | Optional | Description                      |
|-------|-----------|---------------|----------|----------------------------------|
| Scope | s         | TODO          | yes      | Scope to limit the annotation on |
| Query | q         | String        | no       | Text to annotate                 |

Response JSON:

    [
      {
        "term": String, // annotatet term contained in the given text
        "annotations": [
          {
            "iri": String,
            "rank": Double, // rank from 0.0 (worse) to 1.0 (best)
            "label": String
          },
          // further proposed annotations
        ]
      },
      // further terms with proposed annotations for the given text
    ]

## Get Annotations

Request URL: `<base>/annotation/get`

Request Method: `POST`

| Name  | Parameter | Type          | Optional | Description |
|-------|-----------|---------------|----------|-------------|
| Query | q         | JSON          | no       | Entities    |

Request JSON:

    [
      {
        "package": Number, // packageID of the requested entity
        "object": Number, // objectID of the requested entity, optional
        "column": Number, // columnID of the requested entity, optional
        "row": Number, // rowID of the requested entity, optional
        "meta": Number // metaID of the requested entity, optional
      },
      // further entities
    ]

Response JSON:

    [
      {
        "entities": [
          {
            "package": Number, // packageID of the requested entity
            "object": Number, // objectID of the requested entity
            "column": Number, // columnID of the requested entity
            "row": Number, // rowID of the requested entity
            "meta": Number // metaID of the requested entity
          },
          // futher entities with same annotation
        ],
        "accepted": [
          {
            "term": String, // is unique in context of this entity
            "iri": String,
            "label" : String | null
          },
          // further accepted annotations
        ],
        "rejected": [
          {
            "term": String,
            "iri": String,
            "label" : String | null
          },
          // further rejected annotations
        ]
      },
      // further entity annotations
    ]

## Set Annotations

Sets Annotations for an entity. Earlier annotations for the same entity will be replaced.

Request URL: `<base>/annotation/set`

Request Method: `POST`

| Name  | Parameter | Type          | Optional | Description        |
|-------|-----------|---------------|----------|--------------------|
| Query | q         | JSON          | no       | Annotations to set |

Request JSON:

    [
      {
        "text": String,
        "scopes": [ // optional
          String,
          // further scopes
        ],
        "entities": [
          {
            "package": Number, // packageID of the requested entity
            "object": Number, // objectID of the requested entity, optional
            "column": Number, // columnID of the requested entity, optional
            "row": Number, // rowID of the requested entity, optional
            "meta": Number // metaID of the requested entity, optional
          },
          // further entities
        ],
        "accepted": [ // optional
          {
            "term": String, // is unique in context of this entity
            "iri": String
          },
          // further accepted annotations
        ],
        "rejected": [ // optional
          {
            "term": String,
            "iri": String
          },
          // further rejected annotations
        ]
      },
      // further text annotations
    ]

Response: HTTP response status codes 204 on valid request, asynchron processing

## Copy Annotations

Copies all annotations of a source entity and its subentities into a target entity  and its subentities.

Request URL: `<base>/annotation/copy`

Request Method: `POST`

| Name  | Parameter | Type          | Optional | Description       |
|-------|-----------|---------------|----------|-------------------|
| Query | q         | JSON          | no       | Source and Target |

Request JSON:

    {
      "source": {
          "package": Number, // packageID of the source entity
          "object": Number, // objectID of the source entity, optional
          "column": Number, // columnID of the source entity, optional
          "row": Number, // rowID of the source entity, optional
          "meta": Number // metaID of the source entity, optional
      },
      "target": {
          "package": Number, // packageID of the target entity
          "object": Number, // objectID of the target entity, optional
          "column": Number, // columnID of the target entity, optional
          "row": Number, // rowID of the requested target, optional
          "meta": Number // metaID of the requested target, optional
      }
    }
 
Response: HTTP response status codes 204 on success

## Delete Annotations

Deletes all annotations of an entity and its subentities.

Request URL: `<base>/annotation/delete`

Request Method: `POST`

| Name  | Parameter | Type          | Optional | Description |
|-------|-----------|---------------|----------|-------------|
| Query | q         | JSON          | no       | Entities    |

Request JSON:

    [
      {
        "package": Number, // packageID of the requested entity
        "object": Number, // objectID of the requested entity, optional
        "column": Number, // columnID of the requested entity, optional
        "row": Number, // rowID of the requested entity, optional
        "meta": Number // metaID of the requested entity, optional
      },
      // further entities
    ]
 
Response: HTTP response status codes 204 on success

## Search

Request URL: `<base>/search`

Request Method: `POST`

| Name  | Parameter | Type          | Optional | Description    |
|-------|-----------|---------------|----------|----------------|
| Query | q         | JSON          | no       | Search request |

Request JSON:

    {
      "include": {
        "text": String,
        "accepted": [ // optional
          {
            "term": String, // is unique in context of this entity
            "iri": String
          },
          // further accepted annotations
        ],
        "rejected": [ // optional
          {
            "term": String,
            "iri": String
          },
          // further rejected annotations
        ]
      },
      "exclude": { // optional
        "text": String,
        "accepted": [ // optional
          {
            "term": String, // is unique in context of this entity
            "iri": String
          },
          // further accepted annotations
        ],
        "rejected": [ // optional
          {
            "term": String,
            "iri": String
          },
          // further rejected annotations
        ]
      },
      "entity": { // optional
        "package": Number, // packageID of the entity to search inside
        "object": Number, // objectID of the entity to search inside, optional
        "column": Number, // columnID of the entity to search inside, optional
        "row": Number, // rowID of the entity to search inside, optional
        "meta": Number // metaID of the entity to search inside, optional
      }
    }

Response JSON:

    [
      {
        "entity": {
          "package": Number, // packageID of the entity to search inside
          "object": Number, // objectID of the entity to search inside, optional
          "column": Number, // columnID of the entity to search inside, optional
          "row": Number, // rowID of the entity to search inside, optional
          "meta": Number // metaID of the entity to search inside, optional
        }
        "rank": Double
      },
      // further results
    ]

## Complete

Request URL: `<base>/complete`

Request Method: `POST`

| Name  | Parameter | Type          | Optional | Description                       |
|-------|-----------|---------------|----------|-----------------------------------|
| Scope | s         | TODO          | yes      | Scope to limit the completions on |
| Query | q         | String        | no       | Text to complete                  |

Response JSON:

    [
      {
        "stump": String,
        "completions": [
          {
            "iri": String,
            "label": String
          },
          // further completions
        ]
      },
      // further stumps
    ]

## Describe

Request URL: `<base>/describe`

Request Method: `POST`

| Name  | Parameter | Type          | Optional | Description          |
|-------|-----------|---------------|----------|----------------------|
| Query | q         | JSON          | no       | Concepts to describe |

Request JSON:

    [
      String, // IRI
      // further IRIs
    ]

Response JSON:

    [
      {
        "iri": String,
        "labels": [
          String, // an label, optional
          // further labels
        ],
        "alternativLabels": [
          String, // an alternativLabel, optional
          // further alternativLabels
        ],
        "descriptions": [
          String, // an description, optional
          // further descriptions
        ],
        "urls": [
          String, // an URL, optional
          // further URLs
        ],
        "types": [
          String,  // a type, optional
          // further types
        ],
        "synonyms": [
          String, // IRI of a synonym concept
        ],
        "broaders": [
          String, // IRI of a broader concept
        ]
      },
      // further IRI descriptions
    ]
