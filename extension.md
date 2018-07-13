# Extension

This document describes how to extend the LakeBase Semantic Service.

## Adding new SemanticDataSource

Adding a new SemanticDataSource requires to implement the [SemanticDataSource Interface](src/main/java/de/uni_jena/cs/fusion/semantic/datasource/SemanticDataSource.java) or to reuse an existing implementation to include an Ontology File or SPARQL Endpoint.
The new SemanticDataSource has to be registered in the [Environment](src/main/java/de/uni_jena/cs/fusion/lakebase/Environment.java) of the LakeBase Semantic Service.

## Extending existing SemanticDataSources

All [used external SemanticDataSources](readme.md) will be automatically updated by the LakeBase Semantic Service once per day.
Therefore the LakeBase Semantic Service will benefit from new releases quickly. 
Some of the external SemanticDataSources have established processes for contribution or requesting new concepts.
