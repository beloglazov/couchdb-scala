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

import org.http4s.Status

import scalaz.concurrent.Task

object Res {

  case class Ok(ok: Boolean = true)

  case class Error(error: String,
                   reason: String,
                   status: Status = Status.ExpectationFailed,
                   request: String = "",
                   requestBody: String = "") {
    def toTask[T]: Task[T] = Task.fail(CouchException(this))
  }

  case class ServerInfo(couchdb: String,
                        uuid: String,
                        version: String,
                        vendor: ServerVendor)

  case class ServerVendor(version: String, name: String)

  case class DbInfo(committed_update_seq: Int,
                    compact_running: Boolean,
                    data_size: Int,
                    db_name: String,
                    disk_format_version: Int,
                    disk_size: Int,
                    doc_count: Int,
                    doc_del_count: Int,
                    instance_start_time: String,
                    purge_seq: Int,
                    update_seq: Int)

  case class ViewIndexInfo(compact_running: Boolean,
                           data_size: Int,
                           disk_size: Int,
                           language: String,
                           purge_seq: Int,
                           signature: String,
                           update_seq: Int,
                           updater_running: Boolean,
                           waiting_clients: Int,
                           waiting_commit: Boolean)

  case class DesignInfo(name: String, view_index: ViewIndexInfo)

  case class Uuids(uuids: Seq[String])

  case class DocOk(ok: Boolean, id: String, rev: String)

}
