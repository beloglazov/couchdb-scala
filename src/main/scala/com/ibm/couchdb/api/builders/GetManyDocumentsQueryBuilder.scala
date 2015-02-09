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

package com.ibm.couchdb.api.builders

import com.ibm.couchdb._
import com.ibm.couchdb.core.Client
import org.http4s.Status

import scalaz.concurrent.Task

case class GetManyDocumentsQueryBuilder(client: Client,
                                        db: String,
                                        params: Map[String, String] = Map.empty[String, String]) {

  def conflicts(conflicts: Boolean = true): GetManyDocumentsQueryBuilder = {
    set("conflicts", conflicts)
  }

  def descending(descending: Boolean = true): GetManyDocumentsQueryBuilder = {
    set("descending", descending)
  }

  def endKey[T: upickle.Writer](endKey: T): GetManyDocumentsQueryBuilder = {
    set("endkey", upickle.write(endKey))
  }

  def endKeyDocId(endKeyDocId: String): GetManyDocumentsQueryBuilder = {
    set("endkey_docid", endKeyDocId)
  }

  private def includeDocs(includeDocs: Boolean = true): GetManyDocumentsQueryBuilder = {
    set("include_docs", includeDocs)
  }

  def inclusiveEnd(inclusiveEnd: Boolean = true): GetManyDocumentsQueryBuilder = {
    set("inclusive_end", inclusiveEnd)
  }

  def key[T: upickle.Writer](key: T): GetManyDocumentsQueryBuilder = {
    set("key", upickle.write(key))
  }

  def limit(limit: Int): GetManyDocumentsQueryBuilder = {
    set("limit", limit)
  }

  def skip(skip: Int): GetManyDocumentsQueryBuilder = {
    set("skip", skip)
  }

  def stale(stale: String): GetManyDocumentsQueryBuilder = {
    set("stale", stale)
  }

  def startKey[T: upickle.Writer](startKey: T): GetManyDocumentsQueryBuilder = {
    set("startkey", upickle.write(startKey))
  }

  def startKeyDocId(startKeyDocId: String): GetManyDocumentsQueryBuilder = {
    set("startkey_docid", startKeyDocId)
  }

  def updateSeq(updateSeq: Boolean = true): GetManyDocumentsQueryBuilder = {
    set("update_seq", updateSeq)
  }

  private def set(key: String, value: String): GetManyDocumentsQueryBuilder = {
    copy(params = params.updated(key, value))
  }

  private def set(key: String, value: Any): GetManyDocumentsQueryBuilder = {
    set(key, value.toString)
  }

  def query: Task[CouchKeyVals[String, CouchDocRev]] = {
    client.get[CouchKeyVals[String, CouchDocRev]](
      s"/$db/_all_docs",
      Status.Ok,
      params.toSeq)
  }

  def query(ids: Seq[String]): Task[CouchKeyVals[String, CouchDocRev]] = {
    if (ids.isEmpty)
      Res.Error("not_found", "No IDs specified").toTask[CouchKeyVals[String, CouchDocRev]]
    else
      client.post[Req.DocKeys[String], CouchKeyVals[String, CouchDocRev]](
        s"/$db/_all_docs",
        Status.Ok,
        Req.DocKeys(ids),
        params.toSeq)
  }

  def queryIncludeDocs[D: upickle.Reader]: Task[CouchDocs[String, CouchDocRev, D]] = {
    client.get[CouchDocs[String, CouchDocRev, D]](
      s"/$db/_all_docs",
      Status.Ok,
      includeDocs().params.toSeq)
  }

  def queryIncludeDocs[D: upickle.Reader](ids: Seq[String]): Task[CouchDocs[String, CouchDocRev, D]] = {
    if (ids.isEmpty)
      Res.Error("not_found", "No IDs specified").toTask[CouchDocs[String, CouchDocRev, D]]
    else
      client.post[Req.DocKeys[String], CouchDocs[String, CouchDocRev, D]](
        s"/$db/_all_docs",
        Status.Ok,
        Req.DocKeys(ids),
        includeDocs().params.toSeq)
  }

}
