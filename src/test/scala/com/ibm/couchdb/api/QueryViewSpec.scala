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
import com.ibm.couchdb.{CouchDocs, CouchKeyVals, CouchReducedKeyVals}
import org.specs2.matcher.MatchResult

import scalaz.concurrent.Task

class QueryViewSpec extends CouchDbSpecification {

  val db            = "couchdb-scala-query-view-spec"
  val databases     = new Databases(client)
  val design        = new Design(client, db)
  val documents     = new Documents(client, db, typeMapping)
  val query         = new Query(client, db)
  val namesView     = query.view[String, String](fixDesign.name, FixViews.names).get
  val compoundView  = query.view[(Int, String), FixPerson](fixDesign.name, FixViews.compound).get
  val aggregateView = query.view[String, String](fixDesign.name, FixViews.reduced).get

  recreateDb(databases, db)

  val createdDesign = awaitRight(design.create(fixDesign))
  val createdAlice  = awaitRight(documents.create(fixAlice))
  val createdBob    = awaitRight(documents.create(fixBob))
  val createdCarl   = awaitRight(documents.create(fixCarl))

  "Query View API" >> {

    "Query a view" >> {
      def verify(task: Task[CouchKeyVals[String, String]]): MatchResult[Any] = {
        val docs = awaitRight(task)
        docs.offset mustEqual 0
        docs.total_rows mustEqual 3
        docs.rows must haveLength(3)
        docs.rows.map(_.id) mustEqual Seq(createdAlice.id, createdBob.id, createdCarl.id)
        docs.rows.map(_.key) mustEqual Seq(fixAlice.name, fixBob.name, fixCarl.name)
        docs.rows.map(_.value) mustEqual Seq(fixAlice.name, fixBob.name, fixCarl.name)
      }
      verify(namesView.build.query)
      verify(namesView.noReduce.excludeDocs.build.query)
    }

    "Query a view with reducer" >> {
      def verify(task: Task[CouchReducedKeyVals[String, Int]]): MatchResult[Any] = {
        val docs = awaitRight(task)
        docs.rows must haveLength(1)
        docs.rows.head.value mustEqual Seq(fixCarl.age, fixBob.age, fixAlice.age).sum
      }
      verify(aggregateView.reduce[Int].build.query)
      verify(aggregateView.noReduce.reduce[Int].build.query)
    }

    "Query a view with reducer given keys" >> {
      def verify(task: Task[CouchReducedKeyVals[String, Int]]): MatchResult[Any] = {
        val docs = awaitRight(task)
        docs.rows must haveLength(2)
        docs.rows.map(_.value).sum mustEqual Seq(fixCarl.age, fixAlice.age).sum
        docs.rows.map(_.key) mustEqual Seq(createdCarl.id, createdAlice.id)
      }
      verify(aggregateView.reduce[Int].withIds(Seq(createdCarl.id, createdAlice.id)).build.query)
    }

    "Query a view in the descending order" >> {
      def verify(task: Task[CouchKeyVals[String, String]]): MatchResult[Any] = {
        val docs = awaitRight(task)
        docs.offset mustEqual 0
        docs.total_rows mustEqual 3
        docs.rows must haveLength(3)
        docs.rows.map(_.id) mustEqual Seq(createdCarl.id, createdBob.id, createdAlice.id)
        docs.rows.map(_.key) mustEqual Seq(fixCarl.name, fixBob.name, fixAlice.name)
        docs.rows.map(_.value) mustEqual Seq(fixCarl.name, fixBob.name, fixAlice.name)
      }
      verify(namesView.descending().build.query)
    }

    "Query a view with compound keys and values" >> {
      def verify(
          task: Task[CouchKeyVals[(Int, String), FixPerson]]): MatchResult[Seq[FixPerson]] = {
        val docs = awaitRight(task)
        docs.offset mustEqual 0
        docs.total_rows mustEqual 3
        docs.rows must haveLength(3)
        docs.rows.map(_.key) mustEqual Seq((20, "Carl"), (25, "Alice"), (30, "Bob"))
        docs.rows.map(_.value) must contain(allOf(fixAlice, fixBob, fixCarl))
      }
      verify(compoundView.build.query)
    }

    "Query a view and select by key" >> {
      def verify[K, V, I](
          task: Task[CouchKeyVals[K, V]], expected: Seq[I], total: Int): MatchResult[Any] = {
        val docs = awaitRight(task)
        docs.total_rows mustEqual total
        docs.rows must haveLength(expected.length)
        docs.rows.map(_.key) must containTheSameElementsAs(expected)
      }
      verify(namesView.key("Alice").build.query, Seq("Alice"), 3)
      verify(compoundView.key((30, "Bob")).build.query, Seq((30, "Bob")), 3)
    }

    "Query a view and include documents" >> {
      def verify(task: Task[CouchDocs[String, String, FixPerson]]): MatchResult[Any] = {
        val docs = awaitRight(task)
        docs.offset mustEqual 0
        docs.total_rows mustEqual 3
        docs.rows must haveLength(3)
        docs.rows.map(_.key) mustEqual Seq(fixAlice.name, fixBob.name, fixCarl.name)
        docs.rows.map(_.value) mustEqual Seq(fixAlice.name, fixBob.name, fixCarl.name)
        docs.rows.map(_.doc.doc) mustEqual Seq(fixAlice, fixBob, fixCarl)
      }
      verify(namesView.includeDocs[FixPerson].build.query)
    }

    "Query a view with a set of keys" >> {
      def verify(task: Task[CouchKeyVals[String, String]]): MatchResult[Any] = {
        val docs = awaitRight(task)
        docs.offset mustEqual 0
        docs.total_rows mustEqual 3
        docs.rows must haveLength(2)
        docs.rows.map(_.key) mustEqual Seq(fixAlice.name, fixBob.name)
        docs.rows.map(_.value) mustEqual Seq(fixAlice.name, fixBob.name)
      }
      verify(namesView.withIds(Seq(fixAlice.name, fixBob.name)).build.query)
    }

    "Query a view with a set of keys and include documents" >> {
      def verify(task: Task[CouchDocs[String, String, FixPerson]]): MatchResult[Any] = {
        val docs = awaitRight(task)
        docs.offset mustEqual 0
        docs.total_rows mustEqual 3
        docs.rows must haveLength(2)
        docs.rows.map(_.key) mustEqual Seq(fixAlice.name, fixBob.name)
        docs.rows.map(_.value) mustEqual Seq(fixAlice.name, fixBob.name)
        docs.rows.map(_.doc.doc) mustEqual Seq(fixAlice, fixBob)
      }
      verify(namesView.includeDocs[FixPerson].withIds(Seq(fixAlice.name, fixBob.name)).build.query)
    }
  }
}
