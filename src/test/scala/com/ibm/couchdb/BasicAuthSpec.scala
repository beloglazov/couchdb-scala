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

import com.ibm.couchdb.spec.{CouchDbSpecification, SpecConfig}
import org.http4s.Status

class BasicAuthSpec extends CouchDbSpecification {

  val couch      = CouchDb(SpecConfig.couchDbHost, SpecConfig.couchDbPort)
  val couchAdmin = CouchDb(
    SpecConfig.couchDbHost,
    SpecConfig.couchDbPort,
    https = false,
    SpecConfig.couchDbUsername,
    SpecConfig.couchDbPassword)

  val db       = "couchdb-scala-basic-auth-spec"
  val adminUrl = s"/_config/admins/${SpecConfig.couchDbUsername}"

  "Basic authentication" >> {

    "Only admin can create and delete databases" >> {
      awaitRight(
        couch.client.put[String, String](
          adminUrl, Status.Ok, SpecConfig.couchDbPassword)) mustEqual ""
      awaitError(couch.dbs.create(db), "unauthorized")
      await(couchAdmin.dbs.delete(db))
      awaitOk(couchAdmin.dbs.create(db))
      awaitDocOk(couch.db(db, typeMapping).docs.create(fixAlice))
      awaitDocOk(couchAdmin.db(db, typeMapping).docs.create(fixAlice))
      awaitRight(couchAdmin.client.delete[String](adminUrl, Status.Ok)) must not be empty
    }
  }
}
