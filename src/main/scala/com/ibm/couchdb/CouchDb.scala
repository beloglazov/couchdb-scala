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

import com.ibm.couchdb.api.{Databases, Design, Documents, Query, Server}
import com.ibm.couchdb.core.Client
import com.ibm.couchdb.model.{Config, TypeMapping}

import scalaz._

case class CouchDbApi(name: String, docs: Documents, design: Design, query: Query)

class CouchDb(host: String, port: Int, scheme: String) {

  val client = new Client(Config(host, port, scheme))
  val server = new Server(client)
  val dbs    = new Databases(client)

  private val memo = Memo.mutableHashMapMemo[(String, TypeMapping), CouchDbApi] {
    case (db, types) =>
      CouchDbApi(
        db,
        new Documents(client, db, types),
        new Design(client, db),
        new Query(client, db))
  }

  def db(name: String, types: TypeMapping): CouchDbApi = memo((name, types))

}

object CouchDb {

  def apply(host: String, port: Int, scheme: String = "http"): CouchDb = {
    new CouchDb(host, port, scheme)
  }

}
