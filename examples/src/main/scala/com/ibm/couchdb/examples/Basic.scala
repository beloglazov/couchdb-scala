/*
 * Copyright 2015 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.couchdb.examples

import com.ibm.couchdb._
import org.slf4j.LoggerFactory

import scalaz._
import scalaz.concurrent.Task

object Basic extends App {
  private val logger = LoggerFactory.getLogger(Basic.getClass)

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

  typeMapping.get(Person.getClass).foreach { mType =>
    val actions: Task[Seq[Person]] = for {
    // Delete the database or ignore the error if it doesn't exist
      _ <- couch.dbs.delete(dbName).ignoreError
      // Create a new database
      _ <- couch.dbs.create(dbName)
      // Insert documents into the database
      _ <- db.docs.createMany(Seq(alice, bob, carl))
      // Retrieve all documents from the database and unserialize to Person
      docs <- db.docs.getMany.includeDocs[Person].byTypeUsingTemporaryView(mType).build.query
    } yield docs.getDocsData

    // Execute the actions and process the result
    actions.attemptRun match {
      // In case of an error (left side of Either), print it
      case -\/(e) => logger.error(e.getMessage, e)
      // In case of a success (right side of Either), print each object
      case \/-(a) => a.foreach(x => logger.info(x.toString))
    }
  }
}
