Apache Couch Db version **1.6**

| CouchDb feature	| HTTP API	|	Support  | Since | Example |
|---|---|:---:|:---:|:----:|
|**Databases**|||||
| Get information about a database  |/{db}| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DatabasesSpec.scala#L36-43)|
| Create a new database  |/{db}| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DatabasesSpec.scala#L30-34)|
| Delete a specified database  |/{db}| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DatabasesSpec.scala#L51-56)|
| Batch mode writes |/{db}?batch=ok|   | | |
|**Documents**|||||
| Retrieve document  |/{db}/{doc_id}| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DocumentsSpec.scala#L45-54)|
| Retrieve document by revision number  |/{db}/{doc_id}|   |  | |
| Create document |/{db}/| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DocumentsSpec.scala#L40-43)|
| Get list of document revisions |/{db}/{doc_id}|   |  | |
| Update document  |/{db}/{doc_id}| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DocumentsSpec.scala#L122-132)|
| Delete document  |/{db}/{doc_id}| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/cohdb/api/DocumentsSpec.scala#L149-154)|
| Copy document  |/{db}/{doc_id}|   |  | |
| Copy document by revision  |/{db}/{doc_id}|   |  | |
| Copy to an existing document  |/{db}/{doc_id}|   |  | |
| Get attachment information  |/{db}/{doc_id}| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DocumentsSpec.scala#L172-188)|
| Create single attachment |/{db}/{doc_id}/{att_name}| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DocumentsSpec.scala#L156-160)|
| Create multiple attachment |/{db}/{doc_id}|   |   | |
| Retrieve attachment |/{db}/{doc_id}/{att_name}| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DocumentsSpec.scala#L162-170)|
| Retrieve multiple attachments  |/{db}/{doc_id}|   |   | |
| Delete attachment  |/{db}/{doc_id}/{attachment_name}| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DocumentsSpec.scala#L209-218)|
| Get all documents in a database  | /{db}/\_all_docs|  &#10003;  | 0.5  | [view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DocumentsSpec.scala#L70-79)|
| Get documents given multiple keys  | /{db}/\_all_docs|  &#10003;  | 0.5 | [view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DocumentsSpec.scala#L81-91)|
| Create and update multiple documents | /{db}/\_bulk_docs|  &#10003;  | 0.5 | [view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DocumentsSpec.scala#L62-68)|
| Get list of changes to documents | /{db}/\_changes| | | |
| Get list of changes for specified document ids | /{db}/\_changes| | | |
| Compact database | /{db}/\_compact| | | |
| Compact view indexes associated with specified design document | /{db}/\_compact/{des\_doc}| | | |
| Commit recent changes to disk | /{db}/\_ensure_full_commit| | | |
| Remove view index files that are not required | /{db}/\_view_cleanup| | | |
| Get security object from database | /{db}/\_security| | | |
| Create and execute temporary view | /{db}/\_temp_view| | | |
| Permanently remove references to delete documents | /{db}/\_purge| | | |
| Given list of document revisions, return this that do not exist| /{db}/\_missing_revs| | | |
| Given list of revision ids, returns those that do not correspond to revisions in database | /{db}/\_revs_diff| | | |
| Get the current revision limit setting | /{db}/\_revs_limit| | | |
|**Design Documents**|||||
| Get content of design document  | /{db}/\_design/{des\_doc}| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DesignSpec.scala#L46-53)|
| Get meta-data about design document  | /{db}/\_design/{des\_doc}/_info| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DesignSpec.scala#L37-44)|
| Create design document  |	/{db}/\_design/{des\_doc}	| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DesignSpec.scala#L32-35)|
| Update design document  |	/{db}/\_design/{des\_doc}	| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DesignSpec.scala#L64-75)|
| Delete design document  |	/{db}/\_design/{des\_doc}	| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DesignSpec.scala#L77-82)|
| Copy design document  |	/{db}/\_design/{des\_doc} |   |  | |
| Create attachment   |	/{db}/\_design/{des\_doc}/{att\_name}	| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DesignSpec.scala#L90-95)|
| Get attachment   |	/{db}/\_design/{des\_doc}/{att\_name}	| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DesignSpec.scala#L97-103)|
| Delete design document attachment  |	/{db}/\_design/{des\_doc}/{att\_name}	| &#10003; | 0.5 |[view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DesignSpec.scala#L77-82)|
| Execute specified view function from the specified design document |{/db}/\_design/{des\_doc}/\_view/{view_name}|  &#10003; | 0.5 | [view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/QueryViewSpec.scala)|
| Applies show function for specified documents|{/db}/\_design/{des\_doc} /\_show/{show\_name}/{doc\_id}|  &#10003; | 0.5 | [view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/QueryShowSpec.scala)|
| Applies list function for the view function from the same design document |{/db}/\_design/{des\_doc} /\_list/{list\_name}/{view\_name}|  &#10003; | 0.5 | [view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/QueryListSpec.scala)|
| Applies list function for the view function from another design document |{/db}/\_design/{des\_doc} / \_list/{list\_name}/{other\_design\_doc}/{view\_name}| | | |
| Rewrite the specified path by rules in specified design document |{/db}/\_design/{des-doc}/\_rewrite/path| | | |
|**Local Documents**|||||
| Get local document  |/{db}/\_local/{doc_id}|   |  | |
| Store local document  |/{db}/\_local/{doc_id}|   |  | |
| Delete local document  |/{db}/\_local/{doc_id}|   |  | |
| Copy local document  |/{db}/\_local/{doc_id}|   |  | |
|**Server**|||||
| Get meta information about instance  | / | &#10003; | 0.5 | [view] (https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/ServerSpec.scala#L28-33) |
| Request a Universally Unique Identifier |	 /\_uuid|  &#10003;  |  0.5 | [view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/ServerSpec.scala#L31-37) |
| List databases  |	/\_all_dbs	| &#10003; | 0.5  | [view](https://github.com/beloglazov/couchdb-scala/blob/5b2e78838c53d1e21a47e3ef8c42d0cc5bb1dcae/src/test/scala/com/ibm/couchdb/api/DatabasesSpec.scala#L45-49)|
| List active tasks  | /\_active_tasks 	| |  | |
| List database events  |	/\_db_updates	|   |  | |
| View Logs  |	/\_log	|   |  | |
| Request, configure or stop a replication request  |	 /\_replicate |   |  | |
| Restart instance  |  	/\_restart	|   | | |
| Statistics about server  |	/\_stats	|    | | |
