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

import com.ibm.couchdb.spec.{CouchDbSpecification, Fixtures}

class QueryListSpec extends CouchDbSpecification with Fixtures {

  val db        = "couchdb-scala-query-list-spec"
  val databases = new Databases(client)
  val design    = new Design(client, db)
  val documents = new Documents(client, db, typeMapping)
  val query     = new Query(client, db)
  val list      = query.list(fixDesign.name, FixLists.csvAll).get

  recreateDb(databases, db)

  val createdDesign = awaitRight(design.create(fixDesign))
  val createdAlice  = awaitRight(documents.create(fixAlice))
  val createdBob    = awaitRight(documents.create(fixBob))
  val createdCarl   = awaitRight(documents.create(fixCarl))

  "Query List API" >> {

    "Query a list" >> {
      val expected = "Carl,20\nAlice,25\nBob,30\n"
      awaitRight(list.query(FixViews.compound)) mustEqual expected
    }

    "Query a list descending" >> {
      val expected = "Bob,30\nAlice,25\nCarl,20\n"
      awaitRight(list.descending().query(FixViews.compound)) mustEqual expected
    }

    "Query a list with a start key" >> {
      val expected = "Alice,25\nBob,30\n"
      awaitRight(list.startKey(Seq(21)).query(FixViews.compound)) mustEqual expected
    }

    "Query a list with custom params" >> {
      val expected = "name,age\nCarl,20\nAlice,25\nBob,30\n"
      awaitRight(list.addParam("header", "true").query(FixViews.compound)) mustEqual expected
    }

    "Query a list with a view from another design" >> {
      val expected = "Carl,20\nAlice,25\nBob,30\n"
      awaitRight(list.queryAnotherDesign(FixViews.compound, fixDesign.name)) mustEqual expected
    }


  }

}
