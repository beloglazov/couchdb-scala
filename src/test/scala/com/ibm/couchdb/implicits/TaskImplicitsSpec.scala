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

package com.ibm.couchdb.implicits

import com.ibm.couchdb.api.Databases
import com.ibm.couchdb.spec.CouchDbSpecification

class TaskImplicitsSpec extends CouchDbSpecification {

  val db        = "couchdb-scala-task-implicits-spec"
  val databases = new Databases(client)

  private def clear() = await(databases.delete(db))

  "Task implicits" >> {

    "Ignore error" >> {
      clear()
      awaitOk(databases.create(db))
      awaitError(databases.create(db), "file_exists")
      awaitOk(databases.create(db).ignoreError)
    }
  }
}
