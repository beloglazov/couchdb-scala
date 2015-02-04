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

package com.ibm.couchdb

import com.ibm.couchdb.api.{Design, Documents}
import com.ibm.couchdb.spec.{CouchDbSpecification, SpecConfig}
import org.http4s.Status
import org.specs2.matcher.MatchResult

class CouchDbSpec extends CouchDbSpecification {

  val couch = CouchDb(SpecConfig.couchDbHost, SpecConfig.couchDbPort)

  val db1 = "couchdb-scala-couchdb-spec1"
  val db2 = "couchdb-scala-couchdb-spec2"

  "User interface" >> {

    "Get info about the DB instance" >> {
      awaitRight(couch.server.info).couchdb mustEqual "Welcome"
    }

    "Create and query 2 DBs" >> {

      def testDb(dbName: String): MatchResult[Seq[CouchDocMeta[String, String]]] = {
        await(couch.dbs.delete(dbName))
        val error = awaitLeft(couch.dbs.delete(dbName))
        error.error mustEqual "not_found"
        error.reason mustEqual "missing"
        error.status mustEqual Status.NotFound
        error.request must contain("DELETE")
        error.request must contain(dbName)
        error.requestBody must beEmpty

        awaitOk(couch.dbs.create(dbName))
        couch.db(dbName, typeMapping).name mustEqual dbName
        couch.db(dbName, typeMapping).docs must beAnInstanceOf[Documents]
        couch.db(dbName, typeMapping).design must beAnInstanceOf[Design]

        val db = couch.db(dbName, typeMapping)
        db must beTheSameAs(couch.db(dbName, typeMapping))
        awaitDocOk(db.docs.create(fixAlice))
        awaitDocOk(db.design.create(fixDesign))

        val docs = awaitRight(db.query.view[String, String](fixDesign.name, FixViews.names).get.query)
        docs.rows must haveLength(1)
      }

      testDb(db1)
      testDb(db2)
    }

  }

}
