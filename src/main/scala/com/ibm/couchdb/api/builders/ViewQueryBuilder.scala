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

import com.ibm.couchdb.core.Client
import com.ibm.couchdb.model.{CouchDocs, CouchDocsMeta, Req, Res}
import org.http4s.Status

import scalaz.concurrent.Task

case class ViewQueryBuilder[K, V](client: Client,
                                  db: String,
                                  design: String,
                                  view: String,
                                  params: Map[String, String] = Map.empty[String, String])
                                 (implicit
                                  kr: upickle.Reader[K],
                                  kw: upickle.Writer[K],
                                  vr: upickle.Reader[V],
                                  cdr: upickle.Reader[CouchDocsMeta[K, V]],
                                  dkw: upickle.Writer[Req.DocKeys[K]]) {

  def conflicts(conflicts: Boolean = true): ViewQueryBuilder[K, V] = {
    set("conflicts", conflicts)
  }

  def descending(descending: Boolean = true): ViewQueryBuilder[K, V] = {
    set("descending", descending)
  }

  def endKey[T: upickle.Writer](endKey: T): ViewQueryBuilder[K, V] = {
    set("endkey", upickle.write(endKey))
  }

  def endKeyDocId(endKeyDocId: String): ViewQueryBuilder[K, V] = {
    set("endkey_docid", endKeyDocId)
  }

  def group(group: Boolean = true): ViewQueryBuilder[K, V] = {
    set("group", group)
  }

  def groupLevel(groupLevel: Int): ViewQueryBuilder[K, V] = {
    set("group_level", groupLevel)
  }

  private def includeDocs(includeDocs: Boolean = true): ViewQueryBuilder[K, V] = {
    set("include_docs", includeDocs)
  }

  def attachments(attachments: Boolean = true): ViewQueryBuilder[K, V] = {
    set("attachments", attachments)
  }

  def attEncodingInfo(attEncodingInfo: Boolean = true): ViewQueryBuilder[K, V] = {
    set("att_encoding_info", attEncodingInfo)
  }

  def inclusiveEnd(inclusiveEnd: Boolean = true): ViewQueryBuilder[K, V] = {
    set("inclusive_end", inclusiveEnd)
  }

  def key[T: upickle.Writer](key: T): ViewQueryBuilder[K, V] = {
    set("key", upickle.write(key))
  }

  def limit(limit: Int): ViewQueryBuilder[K, V] = {
    set("limit", limit)
  }

  def reduce(reduce: Boolean = true): ViewQueryBuilder[K, V] = {
    set("reduce", reduce)
  }

  def skip(skip: Int): ViewQueryBuilder[K, V] = {
    set("skip", skip)
  }

  def stale(stale: String): ViewQueryBuilder[K, V] = {
    set("stale", stale)
  }

  def startKey[T: upickle.Writer](startKey: T): ViewQueryBuilder[K, V] = {
    set("startkey", upickle.write(startKey))
  }

  def startKeyDocId(startKeyDocId: String): ViewQueryBuilder[K, V] = {
    set("startkey_docid", startKeyDocId)
  }

  def updateSeq(updateSeq: Boolean = true): ViewQueryBuilder[K, V] = {
    set("update_seq", updateSeq)
  }

  private def set(key: String, value: String): ViewQueryBuilder[K, V] = {
    copy(params = params.updated[String](key, value))(kr, kw, vr, cdr, dkw)
  }

  private def set(key: String, value: Any): ViewQueryBuilder[K, V] = {
    set(key, value.toString)
  }

  def query: Task[CouchDocsMeta[K, V]] = {
    client.get[CouchDocsMeta[K, V]](
      s"/$db/_design/$design/_view/$view",
      Status.Ok,
      params.toSeq)
  }

  def query(keys: Seq[K]): Task[CouchDocsMeta[K, V]] = {
    if (keys.isEmpty)
      Res.Error("not_found", "No keys specified").toTask[CouchDocsMeta[K, V]]
    else
      client.post[Req.DocKeys[K], CouchDocsMeta[K, V]](
        s"/$db/_design/$design/_view/$view",
        Status.Ok,
        Req.DocKeys(keys),
        params.toSeq)
  }

  def queryIncludeDocs[D: upickle.Reader]: Task[CouchDocs[K, V, D]] = {
    client.get[CouchDocs[K, V, D]](
      s"/$db/_design/$design/_view/$view",
      Status.Ok,
      includeDocs().params.toSeq)
  }

  def queryIncludeDocs[D: upickle.Reader](keys: Seq[K]): Task[CouchDocs[K, V, D]] = {
    if (keys.isEmpty)
      Res.Error("not_found", "No keys specified").toTask[CouchDocs[K, V, D]]
    else
      client.post[Req.DocKeys[K], CouchDocs[K, V, D]](
        s"/$db/_design/$design/_view/$view",
        Status.Ok,
        Req.DocKeys(keys),
        includeDocs().params.toSeq)
  }

}
