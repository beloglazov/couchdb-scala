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

package com.ibm.couchdb.api

import com.ibm.couchdb.spec.CouchDbSpecification

class QueryShowSpec extends CouchDbSpecification {

  val db        = "couchdb-scala-query-show-spec"
  val databases = new Databases(client)
  val design    = new Design(client, db)
  val documents = new Documents(client, db, typeMapping)
  val query     = new Query(client, db)
  val show      = query.show(fixDesign.name, FixShows.csv).get

  recreateDb(databases, db)

  val createdDesign = awaitRight(design.create(fixDesign))
  val createdAlice  = awaitRight(documents.create(fixAlice))
  val createdBob    = awaitRight(documents.create(fixBob))
  val createdCarl   = awaitRight(documents.create(fixCarl))

  "Query Show API" >> {

    "Query a show without ID" >> {
      awaitRight(show.query) mustEqual "empty show"
    }

    "Query a show by ID" >> {
      awaitRight(show.query(createdAlice.id)) mustEqual s"${ fixAlice.name },${ fixAlice.age }"
      awaitRight(show.query(createdBob.id)) mustEqual s"${ fixBob.name },${ fixBob.age }"
    }

    "Query a show by ID with params" >> {
      val res = awaitRight(show.addParam("extra", "test").query(createdAlice.id))
      res mustEqual s"${ fixAlice.name },${ fixAlice.age },test"
    }

  }

}
