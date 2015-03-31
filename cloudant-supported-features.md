IBM Cloudant API V2 Support

| Cloudant feature	| HTTP API	| Support  | Since | Example |
|---|---|:---:|:---:|:----:|
|**Databases**|||||
| Create a new database  |PUT /{username}.cloudant.com/{db}|   |    | |
| Get information about a database  |GET /{username}.cloudant.com/{db}|   |  | |
| Delete a specified database  |DELETE /{username}.cloudant.com/{db}|   |    | |
| List databases  |	GET /{username}.cloudant.com/\_all_dbs |    |   | |
| Get all documents in a database  |GET /{username}.cloudant.com/{db}/\_all_docs|     |     | |
| Create a new index  |	POST /{username}.cloudant.com/{db}/_index |    |  | |
| Delete an index  |	DELETE /{username}.cloudant.com/{db}/\_index/{design\_doc}/{type}/{name} |    |  | |

|**Documents**|||||
| Create document |POST /{username}.cloudant.com/{db}/|   |    | |
| Retrieve document  |GET /{username}.cloudant.com/{db}/{doc_id}|   |    | |
| Update document  |PUT /{username}.cloudant.com/{db}/{doc_id}|   |    | |
| Query database  |GET /{username}.cloudant.com/{db}/\_all_docs|     |     | |
| Delete document  |DELETE /{username}.cloudant.com/{db}/{doc_id}?rev={rev}|   |    | |
| Create and update multiple documents |POST /{username}.cloudant.com/{db}/\_bulk_docs|     |    | |
| Create attachment |PUT /{username}.cloudant.com/{db}/{doc_id}/{att_name}?rev={rev}|   |    | |
| Retrieve attachment |GET /{username}.cloudant.com/{db}/{doc_id}/{att_name}|   |    | |
| Delete attachment  |DELETE /{username}.cloudant.com/{db}/{doc_id}/{attachment_name}?rev={rev}|   |    | |
| Find document using an index  |	POST /{username}.cloudant.com/{db}/_find|    |  | |
| Get documents given multiple keys |POST /{username}.cloudant.com/{db}/\_design/{design\_doc}/\_view|     |    | |
| Get list of changes to documents |GET /{username}.cloudant.com/{db}/\_changes| | | |

|**Design Documents**|||||
| Create design document  |PUT /{username}.cloudant.com/{db}/\_design/design-doc |   |    | |
| Update design document  |PUT /{username}.cloudant.com/{db}/\_design/design-doc  |   |    | |
| Get design document  |GET /{username}.cloudant.com/{db}/\_design/{des\_doc}|   |    | |
| Get meta-data about design document  |GET /{username}.cloudant.com/{db}/\_design/{des\_doc}/\_info|   |    | | 
| Copy design document  |COPY /{username}.cloudant.com/{db}/\_design/{des\_doc}?rev={rev} |   |  | |
| Delete design document  |DELETE /{username}.cloudant.com/{db}/\_design/{des\_doc}?rev={rev}|   |    | |
| Get List Functions |GET /{username}.cloudant.com/{db}/{design\_id}/\_list/{list\_function}/{map\_reduce\_index}|   |    | |
| Get Show Functions |GET /{username}.cloudant.com/{db}/{design\_id}/\_show/{show\_function}/{document\_id}|   |    | |
| Query Update Handlers |POST /{username}.cloudant.com/{db}/{design\_id}/\_update/{update\_handler}|   |    | |

|**Views**|||||
| Add view to design document |PUT /{username}.cloudant.com/{db}/\_design/|   |    | |
| Query a view|GET /{username}.cloudant.com/{db}/\_design/{design\_id}/\_view|   |    | |
| Query a view using a list of keys|POST /{username}.cloudant.com/{db}/_design/{design\_id}/\_view/{view\_name}|   |    | |

|**Replication**|||||
| | | | | |
|**Server**|||||
| List active tasks  |GET /{username}.cloudant.com/\_active_tasks | |  | |
| List database events  |GET /{username}.cloudant.com/\_db_updates|   |  | |
| Request a Universally Unique Identifier |	 /\_uuid|     |     | |