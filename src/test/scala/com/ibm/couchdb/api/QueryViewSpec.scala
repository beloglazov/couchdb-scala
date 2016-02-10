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

class QueryViewSpec extends CouchDbSpecification {

  val db           = "couchdb-scala-query-view-spec"
  val databases    = new Databases(client)
  val design       = new Design(client, db)
  val documents    = new Documents(client, db, typeMapping)
  val query        = new Query(client, db)
  val namesView    = query.view[String, String](fixDesign.name, FixViews.names).get
  val compoundView = query.view[(Int, String), FixPerson](fixDesign.name, FixViews.compound).get
  val aggregateView = query.view[String, String](fixDesign.name, FixViews.aggregate).get

  recreateDb(databases, db)

  val createdDesign = awaitRight(design.create(fixDesign))
  val createdAlice  = awaitRight(documents.create(fixAlice))
  val createdBob    = awaitRight(documents.create(fixBob))
  val createdCarl   = awaitRight(documents.create(fixCarl))

  "Query View API" >> {

    "Query a view" >> {
      val docs = awaitRight(namesView.query)
      docs.offset mustEqual 0
      docs.total_rows mustEqual 3
      docs.rows must haveLength(3)
      docs.rows.map(_.id) mustEqual Seq(createdAlice.id, createdBob.id, createdCarl.id)
      docs.rows.map(_.key) mustEqual Seq(fixAlice.name, fixBob.name, fixCarl.name)
      docs.rows.map(_.value) mustEqual Seq(fixAlice.name, fixBob.name, fixCarl.name)
    }

    "Query a view with reducer" >> {
      val docs = awaitRight(aggregateView.queryWithReduce[String, Int])
      docs.rows must haveLength(1)
      docs.rows.head.value mustEqual Seq(fixCarl.age, fixBob.age, fixAlice.age).sum
    }

    "Query a view with reducer given keys" >> {
      val docs = awaitRight(aggregateView.queryWithReduce[String, Int](Seq(createdCarl.id, createdAlice.id)))
      docs.rows must haveLength(2)
      docs.rows.map(_.value).sum mustEqual Seq(fixCarl.age, fixAlice.age).sum
      docs.rows.map(_.key) mustEqual Seq(createdCarl.id, createdAlice.id)
    }

    "Query a view in the descending order" >> {
      val docs = awaitRight(namesView.descending().query)
      docs.offset mustEqual 0
      docs.total_rows mustEqual 3
      docs.rows must haveLength(3)
      docs.rows.map(_.id) mustEqual Seq(createdCarl.id, createdBob.id, createdAlice.id)
      docs.rows.map(_.key) mustEqual Seq(fixCarl.name, fixBob.name, fixAlice.name)
      docs.rows.map(_.value) mustEqual Seq(fixCarl.name, fixBob.name, fixAlice.name)
    }

    "Query a view with compound keys and values" >> {
      val docs = awaitRight(compoundView.query)
      docs.offset mustEqual 0
      docs.total_rows mustEqual 3
      docs.rows must haveLength(3)
      docs.rows.map(_.key) mustEqual Seq((20, "Carl"), (25, "Alice"), (30, "Bob"))
      docs.rows.map(_.value) must contain(allOf(fixAlice, fixBob, fixCarl))
    }

    "Query a view and select by key" >> {
      val docs1 = awaitRight(namesView.key("Alice").query)
      docs1.offset mustEqual 0
      docs1.total_rows mustEqual 3
      docs1.rows must haveLength(1)
      docs1.rows(0).key mustEqual "Alice"
      docs1.rows(0).value mustEqual "Alice"
      val docs2 = awaitRight(compoundView.key((30, "Bob")).query)
      docs2.offset mustEqual 2
      docs2.total_rows mustEqual 3
      docs2.rows must haveLength(1)
      docs2.rows(0).key mustEqual ((30, "Bob"))
    }

    "Query a view and include documents" >> {
      val docs = awaitRight(namesView.queryIncludeDocs[FixPerson])
      docs.offset mustEqual 0
      docs.total_rows mustEqual 3
      docs.rows must haveLength(3)
      docs.rows.map(_.key) mustEqual Seq(fixAlice.name, fixBob.name, fixCarl.name)
      docs.rows.map(_.value) mustEqual Seq(fixAlice.name, fixBob.name, fixCarl.name)
      docs.rows.map(_.doc.doc) mustEqual Seq(fixAlice, fixBob, fixCarl)
    }

    "Query a view with a set of keys" >> {
      val docs = awaitRight(namesView.query(Seq(fixAlice.name, fixBob.name)))
      docs.offset mustEqual 0
      docs.total_rows mustEqual 3
      docs.rows must haveLength(2)
      docs.rows.map(_.key) mustEqual Seq(fixAlice.name, fixBob.name)
      docs.rows.map(_.value) mustEqual Seq(fixAlice.name, fixBob.name)
    }

    "Query a view with a set of keys and include documents" >> {
      val docs = awaitRight(namesView.queryIncludeDocs[FixPerson](Seq(fixAlice.name, fixBob.name)))
      docs.offset mustEqual 0
      docs.total_rows mustEqual 3
      docs.rows must haveLength(2)
      docs.rows.map(_.key) mustEqual Seq(fixAlice.name, fixBob.name)
      docs.rows.map(_.value) mustEqual Seq(fixAlice.name, fixBob.name)
      docs.rows.map(_.doc.doc) mustEqual Seq(fixAlice, fixBob)
    }

  }

}
