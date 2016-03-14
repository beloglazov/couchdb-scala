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

class DatabasesSpec extends CouchDbSpecification {

  val db        = "couchdb-scala-databases-spec"
  val databases = new Databases(client)

  private def clear() = await(databases.delete(db))

  "Databases API" >> {

    "Create a DB" >> {
      clear()
      awaitOk(databases.create(db))
      awaitError(databases.create(db), "file_exists")
    }

    "Get a DB" >> {
      clear()
      awaitOk(databases.create(db))
      val info = awaitRight(databases.get(db))
      info.db_name mustEqual db
      info.doc_count mustEqual 0
      info.doc_del_count mustEqual 0
    }

    "Get all DBs" >> {
      awaitRight(databases.getAll)
      await(databases.create(db))
      awaitRight(databases.getAll) must contain(db)
    }

    "Delete a DB" >> {
      clear()
      awaitError(databases.delete(db), "not_found")
      awaitOk(databases.create(db))
      awaitOk(databases.delete(db))
    }
  }
}
