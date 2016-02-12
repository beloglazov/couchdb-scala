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
import upickle.default.Aliases.{R, W}
import upickle.default.write

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

  def endKey[K: W](endKey: K): GetManyDocumentsQueryBuilder = {
    set("endkey", write(endKey))
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

  def key[K: W](key: K): GetManyDocumentsQueryBuilder = {
    set("key", write(key))
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

  def startKey[K: W](startKey: K): GetManyDocumentsQueryBuilder = {
    set("startkey", write(startKey))
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
    queryWithoutIds[CouchKeyVals[String, CouchDocRev]](params)
  }

  def query(ids: Seq[String]): Task[CouchKeyVals[String, CouchDocRev]] = {
    queryByIds[CouchKeyVals[String, CouchDocRev]](ids, params)
  }

  def queryAllowMissing(ids: Seq[String]): Task[CouchKeyValsIncludesMissing[String, CouchDocRev]] = {
    queryByIds[CouchKeyValsIncludesMissing[String, CouchDocRev]](ids, params)
  }

  def queryIncludeDocs[D: R]: Task[CouchDocs[String, CouchDocRev, D]] = {
    queryWithoutIds[CouchDocs[String, CouchDocRev, D]](includeDocs().params)
  }

  def queryIncludeDocs[D: R](ids: Seq[String]): Task[CouchDocs[String, CouchDocRev, D]] = {
    queryByIds[CouchDocs[String, CouchDocRev, D]](ids, includeDocs().params)
  }

  def queryIncludeDocsAllowMissing[D: R](ids: Seq[String]): Task[CouchDocsIncludesMissing[String, CouchDocRev, D]] = {
    queryByIds[CouchDocsIncludesMissing[String, CouchDocRev, D]](ids, includeDocs().params)
  }

  private def queryWithoutIds[Q: R](parameters: Map[String, String]): Task[Q] = {
    client.get[Q](s"/$db/_all_docs", Status.Ok, parameters.toSeq)
  }

  private def queryByIds[Q: R](ids: Seq[String], parameters: Map[String, String]): Task[Q] = {
    if (ids.isEmpty)
      Res.Error("not_found", "No IDs specified").toTask[Q]
    else
      client.post[Req.DocKeys[String], Q](s"/$db/_all_docs", Status.Ok, Req.DocKeys(ids), parameters.toSeq)
  }
}
