IBM Cloudant API V2 Support

Endpoints are prefixed with  `/{username}.cloudant.com/`

| Cloudant feature	| HTTP API	| Support  | Since | Example |
|---|---|:---:|:---:|:----:|
|**Databases**|||||
| Create a new database  |`PUT /{db}` |   |    | |
| Get information about a database  | `GET /{db}` |   |  | |
| Delete a specified database  |`DELETE /{db}` |   |    | |
| List databases  |	`GET /_all_dbs ` |    |   | |
| Get all documents in a database  |`GET /{db}/_all_docs` |     |     | |
| Create a new index  |	`POST /{db}/_index` |    |  | |
| Delete an index  |	`DELETE /{db}/_index/{design_doc}/{type}/{name}` |    |  | |
|**Documents**|||||
| Create document | `POST /{db}/` |   |    | |
| Retrieve document  | `GET /{db}/{doc_id}` |   |    | |
| Update document  | `PUT /{db}/{doc_id}` |   |    | |
| Query database  | `GET /{db}/_all_docs` |     |     | |
| Delete document  | `DELETE /{db}/{doc_id}?rev={rev}` |   |    | |
| Create and update multiple documents | `POST /{db}/_bulk_docs` |     |    | |
| Create attachment | `PUT /{db}/{doc_id}/{att_name}?rev={rev}` |   |    | |
| Retrieve attachment | `GET /{db}/{doc_id}/{att_name}` |   |    | |
| Delete attachment  | `DELETE /{db}/{doc_id}/{attachment_name}?rev={rev}` |   |    | |
| Find document using an index  | `POST /{db}/_find` |    |  | |
| Get documents given multiple keys | `POST /{db}/_design/{design_doc}/_view` |     |    | |
| Get list of changes to documents | `GET /{db}/_changes` | | | |
|**Design Documents**|||||
| Create design document  | `PUT /{db}/_design/design-doc` |   |    | |
| Update design document  | `PUT /{db}/_design/design-doc`  |   |    | |
| Get design document  | `GET /{db}/_design/{des_doc}` |   |    | |
| Get meta-data about design document  | `GET /{db}/_design/{des_doc}/_info` |   |    | | 
| Copy design document  |  `COPY /{db}/_design/{des_doc}?rev={rev}` |   |  | |
| Delete design document   | `DELETE /{db}/_design/{des_doc}?rev={rev}` |   |    | |
| Get List Functions | `GET /{db}/{design_id}/_list/{list_function}/{map_reduce_index}` |   |    | |
| Get Show Functions | `GET /{db}/{design_id}/_show/{show_function}/{document_id}` |   |    | |
| Query Update Handlers | `POST /{db}/{design_id}/_update/{update_handler}` |   |    | |
|**Views**|||||
| Add view to design document | `PUT /{db}/_design/` |   |    | |
| Query a view| `GET /{db}/_design/{design_id}/_view` |   |    | |
| Query a view using a list of keys| `POST /{db}/_design/{design_id}/_view/{view_name}` |   |    | |
|**Replication**|||||
| | | | | |
|**Server**|||||
| List active tasks  | `GET /_active_tasks`  | |  | |
| List database events  | `GET /_db_updates` |   |  | |
| Request a Universally Unique Identifier |	 `GET /_uuid` |     |     | |
