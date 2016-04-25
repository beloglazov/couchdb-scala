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
import upickle.default.Aliases.{R, W}
import upickle.default.write

import scalaz.concurrent.Task

sealed trait ViewOperation
abstract class MapWithReduce[A: R] extends ViewOperation
trait MapOnly extends ViewOperation

case class ViewQueryBuilder[K: R, V: R, DR <: DocsInResult, MR <: ViewOperation] private(
    client: Client,
    db: String,
    design: Option[String],
    view: Option[String],
    temporaryView: Option[CouchView] = None,
    params: Map[String, String] = Map.empty[String, String],
    ids: Seq[K] = Seq.empty)(implicit kw: W[K], ckr: R[CouchKeyVals[K, V]]) {

  def conflicts(conflicts: Boolean = true): ViewQueryBuilder[K, V, DR, MR] = {
    set("conflicts", conflicts)
  }

  def descending(descending: Boolean = true): ViewQueryBuilder[K, V, DR, MR] = {
    set("descending", descending)
  }

  def endKey[K2: W](endKey: K2): ViewQueryBuilder[K, V, DR, MR] = {
    set("endkey", write(endKey))
  }

  def endKeyDocId(endKeyDocId: String): ViewQueryBuilder[K, V, DR, MR] = {
    set("endkey_docid", endKeyDocId)
  }

  def group(group: Boolean = true): ViewQueryBuilder[K, V, DR, MR] = {
    set("group", group)
  }

  def groupLevel(groupLevel: Int): ViewQueryBuilder[K, V, DR, MR] = {
    set("group_level", groupLevel)
  }

  def includeDocs[D: R]: ViewQueryBuilder[K, V, IncludeDocs[D], MR] = {
    set("include_docs", true)
  }

  def excludeDocs: ViewQueryBuilder[K, V, ExcludeDocs, MR] = {
    set("include_docs", false)
  }

  def attachments(attachments: Boolean = true): ViewQueryBuilder[K, V, DR, MR] = {
    set("attachments", attachments)
  }

  def attEncodingInfo(attEncodingInfo: Boolean = true): ViewQueryBuilder[K, V, DR, MR] = {
    set("att_encoding_info", attEncodingInfo)
  }

  def inclusiveEnd(inclusiveEnd: Boolean = true): ViewQueryBuilder[K, V, DR, MR] = {
    set("inclusive_end", inclusiveEnd)
  }

  def key[D: W](key: D): ViewQueryBuilder[K, V, DR, MR] = {
    set("key", write(key))
  }

  def limit(limit: Int): ViewQueryBuilder[K, V, DR, MR] = {
    set("limit", limit)
  }

  def reduce[A: R]: ViewQueryBuilder[K, V, DR, MapWithReduce[A]] = {
    set("reduce", true)
  }

  def noReduce: ViewQueryBuilder[K, V, DR, MapOnly] = {
    set("reduce", false)
  }

  def withIds(ids: Seq[K]): ViewQueryBuilder[K, V, DR, MR] = {
    set(params, ids)
  }

  def skip(skip: Int): ViewQueryBuilder[K, V, DR, MR] = {
    set("skip", skip)
  }

  def stale(stale: String): ViewQueryBuilder[K, V, DR, MR] = {
    set("stale", stale)
  }

  def startKey[K2: W](startKey: K2): ViewQueryBuilder[K, V, DR, MR] = {
    set("startkey", write(startKey))
  }

  def startKeyDocId(startKeyDocId: String): ViewQueryBuilder[K, V, DR, MR] = {
    set("startkey_docid", startKeyDocId)
  }

  def updateSeq(updateSeq: Boolean = true): ViewQueryBuilder[K, V, DR, MR] = {
    set("update_seq", updateSeq)
  }

  private def set[I <: DocsInResult, M <: ViewOperation](key: String, value: String):
  ViewQueryBuilder[K, V, I, M] = {
    set(params.updated(key, value), ids)
  }

  private def set[I <: DocsInResult, M <: ViewOperation](key: String, value: Any):
  ViewQueryBuilder[K, V, I, M] = {
    set(key, value.toString)
  }

  private def set[I <: DocsInResult, M <: ViewOperation](_params: Map[String, String],
     _ids: Seq[K]): ViewQueryBuilder[K, V, I, M] = {
    new ViewQueryBuilder(client, db, design, view, temporaryView, _params, _ids)
  }

  @deprecated("Use build.query instead.", "0.7.2")
  def query: Task[CouchKeyVals[K, V]] =
    strategy[CouchKeyVals[K, V]].query

  @deprecated("Use reduce[A].build.query instead.", "0.7.2")
  def queryWithReduce[A: R]: Task[CouchReducedKeyVals[K, A]] = {
    reduce[A].strategy[CouchReducedKeyVals[K, A]].query
  }

  @deprecated("Use withIds[K](ids: Seq[K]).build.query instead.", "0.7.2")
  def query(keys: Seq[K]): Task[CouchKeyVals[K, V]] =
    withIds(keys).strategy[CouchKeyVals[K, V]].query

  @deprecated("Use withIds[K](ids: Seq[K]).reduce[A].build.query instead.", "0.7.2")
  def queryWithReduce[A: R](keys: Seq[K]): Task[CouchReducedKeyVals[K, A]] = {
    withIds(keys).reduce[A].group().strategy[CouchReducedKeyVals[K, A]].query
  }

  @deprecated("Use includeDocs[D].build.query instead.", "0.7.2")
  def queryIncludeDocs[D: R]: Task[CouchDocs[K, V, D]] =
    includeDocs[D].strategy[CouchDocs[K, V, D]].query

  @deprecated("Use withIds[K](ids: Seq[K]).includeDocs[D].build.query instead.", "0.7.2")
  def queryIncludeDocs[D: R](keys: Seq[K]): Task[CouchDocs[K, V, D]] = {
    withIds(keys).includeDocs[D].strategy[CouchDocs[K, V, D]].query
  }

  private def strategy[Q: R]: QueryView[K, Q] =
    new QueryView[K, Q](client, db, design, params, ids, view, temporaryView)
}

object ViewQueryBuilder {

  private type VBuilder[K, V, ID <: DocsInResult, MR <: ViewOperation] =
  ViewQueryBuilder[K, V, ID, MR]

  case class Builder[K: R, V: R, T: R, ID <: DocsInResult, MR <: ViewOperation](
      builder: VBuilder[K, V, ID, MR])(implicit kr: W[K], cdr: R[CouchKeyVals[K, V]]) {
    def build: QueryView[K, T] = QueryView(builder.client, builder.db, builder.design,
      builder.params, builder.ids, builder.view, builder.temporaryView)
  }

  private type BasicBuilder[K, V] = Builder[K, V, CouchKeyVals[K, V], ExcludeDocs, MapOnly]

  private type MapWithReduceBuilder[K, V, A] = Builder[K, V, CouchReducedKeyVals[K, A],
      ExcludeDocs, MapWithReduce[A]]

  private type IncludeDocsBuilder[K, V, D] = Builder[K, V, CouchDocs[K, V, D], IncludeDocs[D],
      MapOnly]

  implicit def buildBasic[K: R, V: R](
      builder: VBuilder[K, V, ExcludeDocs, MapOnly])(implicit kw: W[K]):
  Builder[K, V, CouchKeyVals[K, V], ExcludeDocs, MapOnly] =
    new BasicBuilder(builder)

  implicit def buildReduced[K: R, V: R, A: R](
      builder: VBuilder[K, V, ExcludeDocs, MapWithReduce[A]])(implicit kw: W[K]):
  Builder[K, V, CouchReducedKeyVals[K, A], ExcludeDocs, MapWithReduce[A]] = {
    new MapWithReduceBuilder(if (builder.ids.isEmpty) builder else builder.group())
  }

  implicit def includeDocsBuilder[K: R, V: R, D: R](
      builder: VBuilder[K, V, IncludeDocs[D], MapOnly])(implicit kw: W[K]):
  Builder[K, V, CouchDocs[K, V, D], IncludeDocs[D], MapOnly] = new IncludeDocsBuilder(builder)

  def apply[K: R, V: R](client: Client, db: String, design: String, view: String)
      (implicit kw: W[K]): ViewQueryBuilder[K, V, ExcludeDocs, MapOnly] = {
    new ViewQueryBuilder(client, db, design = Option(design), view = Option(view))
  }

  def apply[K: R, V: R](client: Client, db: String, view: CouchView)(implicit kw: W[K]):
  ViewQueryBuilder[K, V, ExcludeDocs, MapOnly] = {
    new ViewQueryBuilder(client, db, design = None, view = None, temporaryView = Option(view))
  }
}
