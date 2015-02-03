# couchdb-scala

[![Build Status](https://travis-ci.org/beloglazov/couchdb-scala.svg?branch=master)](https://travis-ci.org/beloglazov/couchdb-scala)

This is a Scala library providing a purely functional client for
[CouchDB](http://couchdb.apache.org/). The design goals are compositionality,
expressiveness, and type-safety.

It's based on the following awesome libraries:
[Scalaz](https://github.com/scalaz/scalaz),
[Http4s](https://github.com/http4s/http4s),
[uPickle](https://github.com/lihaoyi/upickle),
[Monocle](https://github.com/julien-truffaut/Monocle), and others.


## Tutorial

This Scala client tries to stay as close to the native CouchDB API as possible,
while adding type-safety and automatic serialization/deserialization of Scala
objects to and from JSON using uPickle. The best way to get up to speed with the
client is to first have a good understanding of how CouchDB works and its native
API. Some good resources to learn CouchDB are:

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
  - Documents: creating and querying documents and attachments
  - Query: querying views, shows, and lists


### Server API

The server API section provides only 3 operations: getting the server info,
which is equivalent to making a get request to the `/` resource of the CouchDB
server; generating a UUID; and generating a sequence of UUIDs. For example, to
make a server info request:

```Scala
couch.server.info.run
```

The `couch.server` property refers to an instance of the `Server` class, which
represents the server API section. Then, calling the `info` method generates a
[scalaz.concurrent.Task](https://github.com/scalaz/scalaz/blob/scalaz-seven/concurrent/src/main/scala/scalaz/concurrent/Task.scala),
which describes an action of making a GET request to the server. At this point,
an actual request is not yet made. Instead, `Task` encapsulates a description of
a computation, which can be executed later when required. This allows us to
control side-effects and keep the functions pure. Tim Perrett has written a nice
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
then executing `Task`s to obtain the query result, we can compose then in a
functional way using higher-order functions like `map` and `flatMap`, or
for-comprehensions. We will see more examples of this later. In further code
snippets, I will omit calls to the `run` method assuming that the point of
executing effectful computations is externalized.

The other operations of the server API can be performed in a similar way. To
generate a UUID, you just need to call `couch.server.mkUuid`, which returns
`Task[String]`. To generate `n` UUIDs, call `couch.server.mkUuids(n)`, which
returns `Task[Seq[String]]` representing a task of generating a sequence of `n`
UUIDs. For more usage examples, please refer to
[ServerSpec](https://github.com/beloglazov/couchdb-scala/blob/master/src/test/scala/com/ibm/couchdb/api/ServerSpec.scala).


### Databases API

The databases API implements more useful functionality like creating, deleting,
and getting info about databases. To create a database:

```Scala
couch.dbs.create("your-db-name")
```

The `couch.dbs` property refers to an instance of the `Databases` class, which
represents the databases API section. A call to the `create` method returns a
`Task[Res.Ok]`, which represents a request returning an instance of the `Res.Ok`
case class if it succeeds, or a failure object if it fails. Failure handling is
another topic that we go through later, but in two words the actual result of a
`Task` execution is `Throwable \/ A`, which is
[either](https://github.com/scalaz/scalaz/blob/scalaz-seven/core/src/main/scala/scalaz/Either.scala)
an exception or the desired type `A`. In the case or `dbs.create`, the desired
result is of type `Res.Ok`, which is a case class representing a response from
the server in case of a succeeded request.

Other methods provided by the databases API are `dbs.delete("your-db-name")` to
delete a database, `dbs.get("your-db-name")` to get information about a database
returned as an instance of `DbInfo` case class that includes such fields as data
size, number of documents in the database, etc. For examples of using the
databases API, please refer to
[DatabasesSpec](https://github.com/beloglazov/couchdb-scala/blob/master/src/test/scala/com/ibm/couchdb/api/DatabasesSpec.scala).


### Design API

While the API sections described above operate across databases, the Design,
Documents, and Query APIs are applied within the context of a single database.
Therefore, to obtain instances of these interfaces, the context needs to be
specialized by specifying the name of a database of interest:

```Scala
val db = couch.db("your-db-name", TypeMapping.empty)
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
become clear later. We can now create an instance of our design document using
the defined `ageView`:

```Scala
val designDoc = CouchDesign(
    name  = "test-design",
    views = Map("age-view" -> ageView))
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

This method call return an object of type `Task[Res.DocOk]`. The `DocOk` case
class represents a response from the server to a succeeded request involving
creating, modifying, and deleting documents. Compared with `Res.Ok`, it included
2 extra fields: `id` (the ID of the created/updated/deleted document) and `rev
(the revision of the created/updated/deleted document)`. In the case of design
documents, based on the CouchDB specification, the ID is composed of the design
name prefixed with `_design/`. In other words, `designDoc` will get the
`_design/test-design` ID. Each revision is a unique 32-character UUID string. We
can now retrieve the design document from the database by name or by ID:

```Scala
db.design.get("test-design")
db.design.getById("_design/test-design")
```

Once executed, both of these calls return an instance of `CouchDesign`
corresponding to our design document with some extra fields, e.g., `_id`,
`_rev`, `_attachments`, etc. To update a design document, we must first retrieve
it from the database to know the current revision and avoid
[conflicts](http://guide.couchdb.org/draft/conflicts.html), makes changes to the
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
    docOk <- db.design.update(initial.copy(views = updatedViews))
} yield docOk
```

Here, we use a for-comprehension to chain 2 monadic actions. If both actions
succeed, we get a `Res.DocOk` object as a result containing the new revision of
the design document stored in the `_rev` field. The Design API supports a few
other operations, to see their usage examples please refer to
[DesignSpec](https://github.com/beloglazov/couchdb-scala/blob/master/src/test/scala/com/ibm/couchdb/api/DesignSpec.scala).


## Complete example

Here is a basic example of an application that stores a set of case class
instances in a database, retrieves them back, and prints out afterwards:

```Scala
object Basic extends App {

  // Define a simple case class to represent our data model
  case class Person(name: String, age: Int)

  // Define a type mapping used to transform class names into the doc kind
  val \/-(typeMapping) = TypeMapping(classOf[Person] -> "Person")

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

## Author

The project has been developed by [Anton Beloglazov](http://beloglazov.info/).

For more open-source projects from IBM, head over to (http://ibm.github.io).


## Copyright and license

© Copyright 2015 IBM Corporation. Distributed under the [Apache 2.0 license](LICENSE).
