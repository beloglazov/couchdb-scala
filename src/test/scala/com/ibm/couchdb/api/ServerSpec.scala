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

class ServerSpec extends CouchDbSpecification {

  val db     = "couchdb-scala-server-spec"
  val server = new Server(client)

  "Server API" >> {

    "Get info about the DB instance" >> {
      val info = awaitRight(server.info)
      info.couchdb mustEqual "Welcome"
      info.uuid must beUuid
      info.version.length must beGreaterThanOrEqualTo(3)
    }

    "Create a UUID" >> {
      awaitRight(server.mkUuid) must beUuid
    }

    "Create 3 UUIDs" >> {
      val uuids = awaitRight(server.mkUuids(3))
      uuids must have size 3
      uuids must contain(beUuid).forall
    }
  }
}
