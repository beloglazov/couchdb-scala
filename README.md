# CouchDB-Scala

[![Build Status](https://travis-ci.org/beloglazov/couchdb-scala.svg?branch=master)](https://travis-ci.org/beloglazov/couchdb-scala)

This is a purely functional Scala client for
[CouchDB](http://couchdb.apache.org/). The design goals are compositionality,
expressiveness, type-safety, and ease of use.

It's based on these awesome libraries:
[Scalaz](https://github.com/scalaz/scalaz),
[Http4s](https://github.com/http4s/http4s),
[uPickle](https://github.com/lihaoyi/upickle-pprint), and
[Monocle](https://github.com/julien-truffaut/Monocle).


## Getting started

Add the following dependency to your SBT config:

```Scala
libraryDependencies += "com.ibm" %% "couchdb-scala" % "0.7.0"
```


## Tutorial

This Scala client tries to stay as close to the native CouchDB API as possible,
while adding type-safety and automatic serialization/deserialization of Scala
objects to and from JSON using uPickle. The best way to get up to speed with the
client is to first obtain a good understanding of how CouchDB works and its
native API. Some good resources to learn CouchDB are:

  - [CouchDB: The Definitive Guide](http://guide.couchdb.org/)
  - [CouchDB Documentation](http://docs.couchdb.org/en/)

To get started, add the following import to your Scala file:

```Scala
import com.ibm.couchdb._
```

Then, you need to create a client instance by passing in the IP address or host
name of the CouchDB server, port number, and optionally the scheme (which
defaults to `http`):

```Scala
val couch = CouchDb("127.0.0.1", 5984)
```

Through this object, you get access to the following CouchDB API sections:

  - Server: server-level operations
  - Databases: operations on databases
  - Design: operations for creating and managing design documents
  - Documents: creating, modifying, and querying documents and attachments
  - Query: querying views, shows, and lists


### Server API

The server API section provides only 3 operations: getting the server info,
which is equivalent to making a GET request to the `/` resource of the CouchDB
server; generating a UUID; and generating a sequence of UUIDs. For example, to
make a server info request using the client instance created above:

```Scala
couch.server.info.run
```

The `couch.server` property refers to an instance of the `Server` class, which
represents the server API section. Then, calling the `info` method generates a
[scalaz.concurrent.Task](https://github.com/scalaz/scalaz/blob/scalaz-seven/concurrent/src/main/scala/scalaz/concurrent/Task.scala),
which describes an action of making a GET request to the server. At this point,
an actual request is not yet made. Instead, `Task` encapsulates a description of
a computation, which can be executed later. This allows us to control
side-effects and keep the functions pure. Tim Perrett has written a very nice
[blog
post](http://timperrett.com/2014/07/20/scalaz-task-the-missing-documentation/)
with more background and documentation on Scalaz's `Task`.

The return type of `couch.server.info` is `Task[Res.ServerInfo]`, which means
that when this task is executed, it may return a `ServerInfo` object or fail. To
execute a `Task`, we need to call the `run` method, which triggers the actual
GET request to server, whose response is then automatically parsed and mapped
onto the `ServerInfo` case class that contains a few fields describing the
server instance like the CouchDB version, etc. Ideally, instead of executing a
`Task` and causing side-effects in the middle of a program, we should delay the
execution as much as possible to keep the core application logic pure. Rather
then executing `Task`s to obtain the query result, we can perform action on the
query results and compose `Task`s in a functional way using higher-order
functions like `map` and `flatMap`, or for-comprehensions. We will see more
examples of this later. In further code snippets, I will omit calls to the `run`
method assuming that the point where effectful computations are executed is
externalized.

The other operations of the server API can be performed in a similar way. To
generate a UUID, you just need to call `couch.server.mkUuid`, which returns
`Task[String]`. To generate `n` UUIDs, call `couch.server.mkUuids(n)`, which
returns `Task[Seq[String]]` representing a task of generating a sequence of `n`
UUIDs. For more usage examples, please refer to
[ServerSpec](src/test/scala/com/ibm/couchdb/api/ServerSpec.scala).


### Databases API

The databases API implements more useful functionality like creating, deleting,
and getting information about databases. To create a database:

```Scala
couch.dbs.create("awesome-database")
```

The `couch.dbs` property refers to an instance of the `Databases` class, which
represents the databases API section. A call to the `create` method returns a
`Task[Res.Ok]`, which represents a request returning an instance of the `Res.Ok`
case class if it succeeds, or a failure object if it fails. Failure handling is
done using methods on `Task`, part of which are covered in Tim Perrett's [blog
post](http://timperrett.com/2014/07/20/scalaz-task-the-missing-documentation/).
In two words the actual result of a `Task` execution is `Throwable \/ A`, which
is
[either](https://github.com/scalaz/scalaz/blob/scalaz-seven/core/src/main/scala/scalaz/Either.scala)
an exception or the desired type `A`. In the case or `dbs.create`, the desired
result is of type `Res.Ok`, which is a case class representing a response from
the server in case of a succeeded request.

Other methods provided by the databases API are `dbs.delete("awesome-database")`
to delete a database, `dbs.get("awesome-database")` to get information about a
database returned as an instance of `DbInfo` case class that includes such
fields as data size, number of documents in the database, etc. For some examples
of using the databases API, please refer to
[DatabasesSpec](src/test/scala/com/ibm/couchdb/api/DatabasesSpec.scala).


### Design API

While the API sections described earlier operate at the level above databases,
the Design, Documents, and Query APIs are applied within the context of a single
database. Therefore, to obtain instances of these interfaces, the context needs
to be specialized by specifying the name of a database of interest:

```Scala
val db = couch.db("awesome-database", TypeMapping.empty)
```

This method call returns an instance of the `CouchDbApi` case class representing
the context of a single database, through which we can get access to the Design,
Documents, and Query APIs. The `db` method takes 2 arguments: the database name
and an instance of `TypeMapping`. We will discuss `TypeMapping` later, for now
we can just pass an empty mapping using `TypeMapping.empty`. Through
`CouchDbApi` we can obtain an instance of the `Design` class representing the
Design API section for our database:

```Scala
db.design
```

The Design API allows us to create, retrieve, update, delete, and manage
attachments to design documents stored in the current database (you can get the
name of the database from an instance of `CouchDbApi` using `db.name`).

Let's take a look at an example of a design document with a single view. First,
assume we have a collection of people each corresponding to an object of a case
class `Person` with a name and age fields:

```Scala
case class Person(name: String, age: Int)
```

Let's define a view with just a map function that emits person names as keys and
ages as values. To do that, we are going to use a `CouchView` case class:

```Scala
val ageView = CouchView(map =
    """
    |function(doc) {
    |   emit(doc.doc.name, doc.doc.age);
    |}
    """.stripMargin)
```

Basically, we define our map function in plain JavaScript and assign it to the
`map` field of a `CouchView` object. This function maps each document to a pair
of the person's name as the key and age as the value. Notice, that we need to
use `doc.doc` to get to the fields of the person object for reasons that will
become clear later.
To define a view that contains a reduce operation, specify the relevant Javascript
function to the `reduce` attribute of the `CouchView` case class constructor like so:

```Scala
val totalAgeView = CouchView(map =
    """
    |function(doc) {
    |   emit(doc._id, doc.doc.age);
    |}
    """.stripMargin,
    reduce =
    """
    |function(key, values, rereduce) {
    |   return sum(values);
    |}
    """.stripMargin)
```

We can now create an instance of our design document using
the defined `ageView` and `totalAgeView`:

```Scala
val designDoc = CouchDesign(
    name  = "test-design",
    views = Map("age-view" -> ageView, "total-age-view" -> totalAgeView))
```

`CouchDesign` supports other fields like `shows` and `lists`, but for this
simple example we only specify the design `name` and `views` as a `Map` from
view names to `CouchView` objects. Proper management of complex design documents
is a separate topic (e.g., JavaScript functions can be stored in separate `.js`
files and loaded dynamically). We can finally proceed to submitting the defined
design document to our database:

```Scala
db.design.create(designDoc)
```

This method call returns an object of type `Task[Res.DocOk]`. The `DocOk` case
class represents a response from the server to a succeeded request involving
creating, modifying, and deleting documents. Compared with `Res.Ok`, it includes
2 extra fields: `id` (the ID of the created/updated/deleted document) and `rev`
(the revision of the created/updated/deleted document). In the case of design
documents, the ID is composed of the design name prefixed with `_design/`. In
other words, `designDoc` will get the `_design/test-design` ID. Each revision is
a unique 32-character UUID string. We can now retrieve the design document from
the database by name or by ID:

```Scala
db.design.get("test-design")
db.design.getById("_design/test-design")
```

Once the returned `Task`s are executed, each of these calls returns an instance
of `CouchDesign` corresponding to our design document with some extra fields,
e.g., `_id`, `_rev`, `_attachments`, etc. To update a design document, we must
first retrieve it from the database to know the current revision and avoid
[conflicts](http://guide.couchdb.org/draft/conflicts.html), make changes to the
content, and submit the updated version. Let's say we want to add another view,
which emits ages as keys and names as values assigned to a `nameView` variable,
then our updated view `Map` is:

```Scala
val updatedViews = Map(
    "age-view"  -> ageView,
    "name-view" -> nameView)
```

We can now submit the changes to the database as follows:

```Scala
for {
    initial <- db.design.get("test-design")
    docOk   <- db.design.update(initial.copy(views = updatedViews))
} yield docOk
```

Here, we use a for-comprehension to chain 2 monadic actions. If both actions
succeed, we get a `Res.DocOk` object as a result containing the new revision of
the design document stored in the `_rev` field. The Design API supports a few
other operations, to see their usage examples please refer to
[DesignSpec](src/test/scala/com/ibm/couchdb/api/DesignSpec.scala).


### Documents API

The Documents API implements operations for creating, querying, modifying, and
deleting documents and their attachments. At this stage, it's time to discuss
how Scala objects are represented in CouchDB and what `TypeMapping` is used for.
One of the design goals of `CouchDB-Scala` is to make it as easy as possible to
store and retrieve documents by automating the process of serialization and
deserialization to and from JSON. This functionality is based on
[uPickle](https://github.com/lihaoyi/upickle-pprint), which uses macros to
automatically generate readers and writers for case classes. However, it also
allows implementing custom readers and writers for your domain classes if they
are not *case classes*. For example, these can be
[Thrift](https://thrift.apache.org/) /
[Scrooge](https://github.com/twitter/scrooge) generated entities or your custom
classes.

CouchDB automatically adds several fields to every document containing metadata
about the document, such as `_id`, `_rev`, `_attachments`, `_conflicts`, etc. To
take advantage of uPickle's support for case classes, a decision was made to
have a case class called `CouchDoc[D]` that has all the metadata fields
generated by CouchDB and also includes 2 special fields: `doc` for storing an
instance of your domain class `D`, and `kind` for storing a string
representation of the document type that can be used for filtering in views,
shows, and lists (we use `kind` instead of `type` here, as `type` is a reserved
keyword in Scala). In other words, if your domain model is represented by a set
of case classes, the serialization and deserialization will be handled
completely transparently for you. `TypeMapping` is used for defining a mapping
from you domain model classes to a string representation of the corresponding
document type. Continuing the previous example with the `Person` case class, we
can define a `TypeMapping`, for example, as follows:

```Scala
val typeMapping = TypeMapping(classOf[Person] -> "Person")
```

Here, we are specifying a mapping from the class name `Person` to a document
kind as a string. The `TypeMapping` factory maps classes to their canonical
names to preserve uniqueness. Whenever a document is submitted to the database,
the `kind` field is automatically populated based on the specified mapping. If
the type mapping is not specified (as we did above by using
`TypeMapping.empty`), the `kind` field is ignored. We can now provide the newly
defined `TypeMapping` to create a fully specified database context:

```Scala
val db = couch.db("awesome-database", typeMapping)
```

Similarly to the other API sections, we can use the database context to get an
instance of the `Documents` class representing the Documents API section:

```Scala
db.docs
```

Let's define some data:

```Scala
val alice = Person("Alice", 25)
val bob   = Person("Bob", 30)
val carl  = Person("Carl", 20)
```

We can now store these objects in the database as follows:

```Scala
db.docs.create(alice)
```

This method assigns a UUID generated with `server.mkUuid` that we've seen above
to the document being stored. Another option is to specify our own document ID
if it's known to be unique:

```Scala
db.docs.create(bob, "bob")
```

As another alternative, we can create multiple documents with auto-generated
UUIDs at once using a batch request:

```Scala
db.docs.createMany(Seq(alice, bob, carl))
```

We can retrieve a document from the database by ID:

```Scala
db.docs.get[Person]("bob")
```

Here, we have to be explicit about the expected object type to allow uPickle to
do its magic, that's why we specify the type parameter to the `get` method. This
method returns `Task[CouchDoc[Person]]`, which basically means that we are
getting back a task that after executing successfully will give us an instance
of `CouchDoc[Person]`. This object will contain an instance of `Person` in the
`doc` field equivalent to the original `Person("Bob", 30)`.

You can also retrieve a set of documents by IDs using:

```Scala
db.docs.getMany.queryIncludeDocs[Person](Seq("id1", "id1"))
```

A call to `getMany` returns an instance of `GetManyDocumentsQueryBuilder`, which
is a class allowing you to build a query in a type-safe way. Under the hood, it
makes a request to the
[/{db}/_all_docs](http://docs.couchdb.org/en/1.6.1/api/database/bulk-api.html#get--db-_all_docs)
endpoint. As you can see from the linked documentation on this endpoint, it has
many optional parameters. The `GetManyDocumentsQueryBuilder` class provides a
fluent interface for constructing queries to this endpoint. For example, to
limit the number of documents to the maximum of 10 and return them in the
descending order:

```Scala
db.docs.getMany.limit(10).descending.queryIncludeDocs[Person](Seq("id1", "id2"))
```

This creates an instance of `Task[CouchDocs[String, CouchDocRev, Person]]`,
which looks complicated but just represents a task that returns basically a
sequence of documents. The `queryIncludeDocs` method serves as a way to complete
the query construction process, which also sets the `include_docs` option to
include the full content of the documents mapped to `Person` objects on arrival.

It's also possible to execute a query without including the document content
using `db.docs.getMany.query`, which is equivalent to keeping the `include_docs`
set to its default `false` value. This query will only return metadata on the
matching documents. In this case, we don't need to specify the type parameter as
no mapping is required since the document content is not retrieved.

To retrieve all documents in the database of a given type without specifying ids, you could use either:
```Scala
val allPeople1 = db.docs.getMany.queryByTypeIncludeDocsWithTemporaryView[Person]
val allPeople2 = db.docs.getMany.queryByTypeIncludeDocs[Person](yourOwnPermTypeFilterView)
```

The first approach, `queryByTypeIncludeDocsWithTemporaryView[T]`, uses a temporary view
under the hood for type based filtering. While convenient for development purposes, it is inefficient
and should be not be used in production. On the other hand, `queryByTypeIncludeDocs[T](CouchView)`,
uses a permanent view passed as argument for type based filtering. Because it uses permanent views
it is more efficient and is thus the recommended method for querying multiple documents by type.

There is a similar query builder for retrieving single documents
`GetDocumentQueryBuilder` that makes GET requests to the
[/{db}/{docid}](http://docs.couchdb.org/en/1.6.1/api/document/common.html#get--db-docid)
endpoint. This query builder can accessed through `db.docs.get`.

There are other operations provided by the Documents API, such as updating
documents, deleting documents, adding attachments, retrieving attachments, etc.
For more usage examples, please refer to
[DocumentsSpec](src/test/scala/com/ibm/couchdb/api/DocumentsSpec.scala).


### Query API

The Query API provides an interface for querying views, shows, and lists. Let's
say we want to query our `age-view` defined earlier. To do that, we first
obtain an instance of `ViewQueryBuilder` as follows:

```Scala
val ageView = db.query.view[String, Int]("test-design", "age-view").get
val totalAgeView = db.query.view[String, Int]("test-design", "total-age-view").get
```

We need to specify 2 type parameters to the `view` method representing the types
of the key and value emitted by the view. In the case of `age-view` and `total-age-view`, it's
`String` for the key (person name) and `Int` for the value (person age).

We can now use the `ageView` query builder to retrieve all the documents from the view:

```Scala
ageView.query
```

This method call returns an instance of `Task[CouchKeyVals[String, Int]]`.
Since we haven't specified the `include_docs` option, this query only retrieves
a sequence of document IDs, keys, and values emitted by the view's map function.
This method makes a call to the
[/{db}/_design/{ddoc}/_view/{view}](http://docs.couchdb.org/en/1.6.1/api/ddoc/views.html#get--db-_design-ddoc-_view-view)
endpoint, and the builder supports all the relevant options.

Similarly, to query the total age of Persons in the document using the
`totalAgeView` builder we can do:

```Scala
totalAgeView.queryWithReduce[Int]
```

The type parameter `T` specified to `queryWithReduce[T]`, in this case `Int`,
is the expected return type of the view's `reduce` function.

We can also make more complex queries. Let's say we want to get 10 people
starting from the name Bob and include the document content:

```Scala
ageView.startKey("Bob").limit(10).queryIncludeDocs[Person]
```

This returns an instance of `Task[CouchDocs[String, Int, Person]]`, which once
executed results in a sequence of objects encapsulating the metadata about the
documents (`id`, `key`, `value`, `offset`, `total_rows`) and the corresponding
`Person` objects. Please follow the definitions of case classes in
[CouchModel](src/main/scala/com/ibm/couchdb/model/CouchModel.scala)
to fully understand the structure of the returned objects.

It's also possible to only get the documents from a view that match the
specified keys. For example, we can use that to get only documents of Alice and
Carl:

```Scala
ageView.query(Seq("Alice", "Carl"))
```

This return an instance of `Task[CouchKeyVals[String, Int]]`. For other usage
examples of the view Query API, please refer to
[QueryViewSpec](src/test/scala/com/ibm/couchdb/api/QueryViewSpec.scala).

The APIs for querying shows and lists are structured similarly to view querying
and follow the official CouchDB specification. Please refer to
[QueryShowSpec](src/test/scala/com/ibm/couchdb/api/QueryShowSpec.scala)
and
[QueryListSpec](src/test/scala/com/ibm/couchdb/api/QueryListSpec.scala)
for more details and examples.


### Authentication

At the moment, the client supports only the [basic
authentication](http://docs.couchdb.org/en/1.6.1/api/server/authn.html#basic-authentication)
method. To use it, just pass your username and password to the `CouchDb`
factory:

```Scala
val couch = CouchDb("127.0.0.1", 6984, https = true, "username", "password")
```

Please note that [enabling
HTTPS](http://docs.couchdb.org/en/1.6.1/config/http.html#config-ssl) is
recommended to avoid sending your credentials in plain text. The default CouchDB
HTTPS port is 6984.


### Complete example

Here is a basic example of an application that stores a set of case class
instances in a database, retrieves them back, and prints out afterwards:

```Scala
object Basic extends App {

  // Define a simple case class to represent our data model
  case class Person(name: String, age: Int)

  // Define a type mapping used to transform class names into the doc kind
  val typeMapping = TypeMapping(classOf[Person] -> "Person")

  // Define some sample data
  val alice = Person("Alice", 25)
  val bob   = Person("Bob", 30)
  val carl  = Person("Carl", 20)

  // Create a CouchDB client instance
  val couch  = CouchDb("127.0.0.1", 5984)
  // Define a database name
  val dbName = "couchdb-scala-basic-example"
  // Get an instance of the DB API by name and type mapping
  val db     = couch.db(dbName, typeMapping)

  val actions = for {
  // Delete the database or ignore the error if it doesn't exist
    _ <- couch.dbs.delete(dbName).ignoreError
    // Create a new database
    _ <- couch.dbs.create(dbName)
    // Insert documents into the database
    _ <- db.docs.createMany(Seq(alice, bob, carl))
    // Retrieve all documents from the database and unserialize to Person
    docs <- db.docs.getMany.queryIncludeDocs[Person]
  } yield docs.getDocsData

  // Execute the actions and process the result
  actions.attemptRun match {
    // In case of an error (left side of Either), print it
    case -\/(e) => println(e)
    // In case of a success (right side of Either), print each object
    case \/-(a) => a.map(println(_))
  }

}
```

You can run this example from the project directory using `sbt`:

```Bash
sbt "run-main com.ibm.couchdb.examples.Basic"
```


## Mailing list

Please feel free to join our mailing list, we welcome all questions and
suggestions: https://groups.google.com/forum/#!forum/couchdb-scala


## Contributing

We welcome contributions, but request you follow these guidelines. Please raise
any bug reports on the project's [issue
tracker](https://github.com/beloglazov/couchdb-scala/issues).

In order for us to accept pull-requests, the contributor must first complete a
Contributor License Agreement (CLA). This clarifies the intellectual property
license granted with any contribution. It is for your protection as a
Contributor as well as the protection of IBM and its customers; it does not
change your rights to use your own Contributions for any other purpose.

You can download the CLAs here:

  - [individual](cla/cla-individual.pdf)
  - [corporate](cla/cla-corporate.pdf)

If you are an IBMer, please contact us directly as the contribution process is
slightly different.


## Contributors

  - [Anton Beloglazov](http://beloglazov.info/) ([@beloglazov](https://github.com/beloglazov))
  - Ermyas Abebe ([@ermyas](https://github.com/ermyas))


## Copyright and license

© Copyright 2015 IBM Corporation, Google Inc. Distributed under the [Apache 2.0
license](LICENSE).
