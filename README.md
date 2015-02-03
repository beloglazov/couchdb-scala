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


### Tutorial

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


#### Server API

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


#### Databases API

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



### Complete example

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

### Author

The project has been developed by [Anton Beloglazov](http://beloglazov.info/).

For more open-source projects from IBM, head over to (http://ibm.github.io).


### Copyright and license

© Copyright 2015 IBM Corporation. Distributed under the [Apache 2.0 license](LICENSE).
