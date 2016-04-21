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
import com.ibm.couchdb.api.Query
import com.ibm.couchdb.core.Client
import upickle.default.Aliases.{R, W}
import upickle.default.write

import scala.reflect.ClassTag
import scalaz.concurrent.Task

sealed trait DocsInResult
abstract class IncludeDocs[D: R] extends DocsInResult
trait ExcludeDocs extends DocsInResult

sealed trait MissingIdsInQuery
trait MissingAllowed extends MissingIdsInQuery
trait MissingNotAllowed extends MissingIdsInQuery

sealed trait DocType
abstract class ForDocType[D: R, K: R, V: R] extends DocType
trait AnyDocType extends DocType

case class GetManyDocumentsQueryBuilder[ID <: DocsInResult, AM <: MissingIdsInQuery,
BT <: DocType] private(
    client: Client,
    db: String,
    typeMappings: TypeMapping,
    params: Map[String, String] = Map.empty[String, String],
    ids: Seq[String] = Seq.empty, view: Option[CouchView] = None) {

  val queryOps = QueryOps(client)

  private val log = org.log4s.getLogger

  lazy val tempTypeFilterView: CouchView = {
    CouchView(
      map =
          """
            |function(doc) {
            | emit([doc.kind, doc._id], doc._id);
            |}
          """.stripMargin)
  }

  def conflicts(
      conflicts: Boolean = true): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set[ID, AM, BT]("conflicts", conflicts)
  }

  def descending(
      descending: Boolean = true): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set[ID, AM, BT]("descending", descending)
  }

  def endKey[K: W](endKey: K): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set[ID, AM, BT]("endkey", write(endKey))
  }

  def endKeyDocId(
      endKeyDocId: String): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set[ID, AM, BT]("endkey_docid", endKeyDocId)
  }

  def includeDocs[D: R]: GetManyDocumentsQueryBuilder[IncludeDocs[D], AM, BT] = {
    set[IncludeDocs[D], AM, BT]("include_docs", true)
  }

  def excludeDocs: GetManyDocumentsQueryBuilder[ExcludeDocs, AM, BT] = {
    set[ExcludeDocs, AM, BT]("include_docs", false)
  }

  def allowMissing: GetManyDocumentsQueryBuilder[ID, MissingAllowed, BT] = {
    allowMissing(allow = true)
  }

  def allowMissing(allow: Boolean): GetManyDocumentsQueryBuilder[ID, MissingAllowed, BT] = {
    set[ID, MissingAllowed, BT](params, ids, view)
  }

  def withIds(ids: Seq[String]): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set[ID, AM, BT](params, ids, view)
  }

  def byType[K: R, V: R, D: R](view: CouchView):
  GetManyDocumentsQueryBuilder[IncludeDocs[D], AM, ForDocType[K, V, D]] = {
    set[IncludeDocs[D], AM, ForDocType[K, V, D]](params, ids, Some(view))
  }

  def byTypeUsingTemporaryView[D: R]:
  GetManyDocumentsQueryBuilder[IncludeDocs[D], AM, ForDocType[(String, String), String, D]] = {
    set[IncludeDocs[D], AM, ForDocType[(String, String), String, D]](
      params, ids, Some(tempTypeFilterView))
  }

  def inclusiveEnd(
      inclusiveEnd: Boolean = true): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set[ID, AM, BT]("inclusive_end", inclusiveEnd)
  }

  def key[K: W](key: K): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set[ID, AM, BT]("key", write(key))
  }

  def limit(limit: Int): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set[ID, AM, BT]("limit", limit)
  }

  def skip(skip: Int): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set[ID, AM, BT]("skip", skip)
  }

  def stale(stale: String): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set[ID, AM, BT]("stale", stale)
  }

  def startKey[K: W](
      startKey: K): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set[ID, AM, BT]("startkey", write(startKey))
  }

  def startKeyDocId(
      startKeyDocId: String): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set[ID, AM, BT]("startkey_docid", startKeyDocId)
  }

  def updateSeq(
      updateSeq: Boolean = true): GetManyDocumentsQueryBuilder[ID, AM, BT] = {
    set[ID, AM, BT]("update_seq", updateSeq)
  }

  private def set[I <: DocsInResult, A <: MissingIdsInQuery, B <: DocType]
  (_params: Map[String, String], _ids: Seq[String], _view: Option[CouchView]):
  GetManyDocumentsQueryBuilder[I, A, B] = {
    new GetManyDocumentsQueryBuilder[I, A, B](client, db, typeMappings, _params, _ids, _view)
  }

  private def set[I <: DocsInResult, A <: MissingIdsInQuery, B <: DocType](
      key: String, value: String): GetManyDocumentsQueryBuilder[I, A, B] = {
    set(params.updated(key, value), ids, view)
  }

  private def set[I <: DocsInResult, A <: MissingIdsInQuery, B <: DocType](
      key: String, value: Any): GetManyDocumentsQueryBuilder[I, A, B] = {
    set(key, value.toString)
  }

  @deprecated(
    "Use build.query instead.", "0.7.1")
  def query: Task[CouchKeyVals[String, CouchDocRev]] = {
    queryWithoutIds[CouchKeyVals[String, CouchDocRev]](params)
  }

  @deprecated(
    "Use withIds(ids: Seq[String]).build.query instead.", "0.7.1")
  def query(ids: Seq[String]): Task[CouchKeyVals[String, CouchDocRev]] = {
    queryByIds[CouchKeyVals[String, CouchDocRev]](ids, params)
  }

  @deprecated(
    "Use allowMissing.withIds(ks: Seq[String]).build.query instead.", "0.7.1")
  def queryAllowMissing(
      ids: Seq[String]): Task[CouchKeyValsIncludesMissing[String, CouchDocRev]] = {
    queryByIds[CouchKeyValsIncludesMissing[String, CouchDocRev]](ids, params)
  }

  @deprecated(
    "Fails if different document types exist in the Db. " +
    "Use byType[K, V, D](view: CouchView).build.query or " +
    "byTypeUsingTemporaryView[D].build.query instead.", "0.7.0")
  def queryIncludeDocs[D: R]: Task[CouchDocs[String, CouchDocRev, D]] = {
    queryWithoutIds[CouchDocs[String, CouchDocRev, D]](includeDocs[D].params)
  }

  @deprecated(
    "Use byType[K, V, D](view: CouchView).build.query instead.", "0.7.1")
  def queryByTypeIncludeDocs[K, V, D: R](typeFilterView: CouchView)
      (implicit tag: ClassTag[D], kr: R[K], kw: W[K], vr: R[V]): Task[CouchDocs[K, V, D]] = {
    typeMappings.forType(tag.runtimeClass) match {
      case Some(k) => queryByType[K, V, D](typeFilterView, k)
      case None => Res.Error("not_found", s"type mapping not found").toTask
    }
  }

  @deprecated(
    "Use byTypeUsingTemporaryView[D].build.query instead.", "0.7.1")
  def queryByTypeIncludeDocsWithTemporaryView[D: R](
      implicit tag: ClassTag[D]): Task[CouchDocs[(String, String), String, D]] = {
    log.warn(
      "Only use `queryByTypeIncludeDocsWithTemporaryView[D: R]` for development purposes." +
      "It uses temporary views to perform type based filters and is inefficient. " +
      "Instead, create a permanent view for type based filtering and use the " +
      "`queryByTypeIncludeDocs[K, V, D: R] (typeFilterView: CouchView) method.")
    queryByTypeIncludeDocs[(String, String), String, D](tempTypeFilterView)
  }

  @deprecated(
    "Use includeDocs.withIds(ids: Seq[String]).build.query instead.", "0.7.1")
  def queryIncludeDocs[D: R](ids: Seq[String]): Task[CouchDocs[String, CouchDocRev, D]] = {
    queryByIds[CouchDocs[String, CouchDocRev, D]](ids, includeDocs[D].params)
  }

  @deprecated(
    "Use includeDocs.allowMissing.withIds(ids: Seq[String]).build.query instead.", "0.7.1")
  def queryIncludeDocsAllowMissing[D: R](
      ids: Seq[String]): Task[CouchDocsIncludesMissing[String, CouchDocRev, D]] = {
    queryByIds[CouchDocsIncludesMissing[String, CouchDocRev, D]](ids, includeDocs[D].params)
  }

  private def queryByType[K, V, D: R](view: CouchView, kind: String)
      (implicit kr: R[K], kw: W[K], vr: R[V]): Task[CouchDocs[K, V, D]] = {
    new Query(client, db).temporaryView[K, V](view) match {
      case Some(v) => v.startKey(Tuple1(kind)).endKey(Tuple2(kind, {})).queryIncludeDocs
      case None => Res.Error("not_found", "invalid view specified").toTask
    }
  }

  private def queryWithoutIds[Q: R](ps: Map[String, String]): Task[Q] = {
    queryOps.query[Q](s"/$db/_all_docs", ps)
  }

  private def queryByIds[Q: R](ids: Seq[String], ps: Map[String, String]): Task[Q] = {
    if (ids.isEmpty)
      Res.Error("not_found", "No IDs specified").toTask
    else
      queryOps.queryByIds[String, Q](s"/$db/_all_docs", ids, ps)
  }
}

object GetManyDocumentsQueryBuilder {

  type MDBuilder[ID <: DocsInResult, AM <: MissingIdsInQuery, BT <: DocType] =
  GetManyDocumentsQueryBuilder[ID, AM, BT]

  case class Builder[T: R, ID <: DocsInResult, AM <: MissingIdsInQuery]
  (builder: MDBuilder[ID, AM, AnyDocType]) {
    def build: QueryBasic[T] =
      new QueryBasic[T](builder.client, builder.db, builder.params, builder.ids)
  }

  case class ByTypeBuilder[K: R, V: R, D: R](
      builder: MDBuilder[IncludeDocs[D], MissingNotAllowed, ForDocType[K, V, D]])
      (implicit tag: ClassTag[D], kw: W[K]) {
    def build: QueryByType[K, V, D] = {
      val view = builder.view.getOrElse(builder.tempTypeFilterView)
      QueryByType[K, V, D](builder.client, builder.db, view, builder.typeMappings)
    }
  }

  type BasicBuilder = Builder[CouchKeyVals[String, CouchDocRev], ExcludeDocs, MissingNotAllowed]

  type AllowMissingBuilder = Builder[CouchKeyValsIncludesMissing[String, CouchDocRev],
      ExcludeDocs, MissingAllowed]

  type IncludeDocsBuilder[D] = Builder[CouchDocs[String, CouchDocRev, D], IncludeDocs[D],
      MissingNotAllowed]

  type AllowMissingIncludeDocsBuilder[D] = Builder[CouchDocsIncludesMissing[String, CouchDocRev,
      D], IncludeDocs[D], MissingAllowed]

  implicit def buildBasic(builder: MDBuilder[ExcludeDocs, MissingNotAllowed, AnyDocType]):
  BasicBuilder = new BasicBuilder(builder)

  implicit def buildAllowMissing(builder: MDBuilder[ExcludeDocs, MissingAllowed, AnyDocType]):
  AllowMissingBuilder = new AllowMissingBuilder(builder)

  implicit def buildIncludeDocs[D: R](
      builder: MDBuilder[IncludeDocs[D], MissingNotAllowed, AnyDocType]): IncludeDocsBuilder[D] =
    new IncludeDocsBuilder(builder)

  implicit def buildIncludeDocsAllowMissing[D: R](
      builder: MDBuilder[IncludeDocs[D], MissingAllowed, AnyDocType]):
  AllowMissingIncludeDocsBuilder[D] = new AllowMissingIncludeDocsBuilder(builder)

  implicit def buildByTypeIncludeDocs[K: R, V: R, D: R](
      builder: MDBuilder[IncludeDocs[D], MissingNotAllowed, ForDocType[K, V, D]])
      (implicit tag: ClassTag[D], kw: W[K]): ByTypeBuilder[K, V, D] = {
    ByTypeBuilder(builder)
  }

  def apply(client: Client, db: String, typeMapping: TypeMapping):
  MDBuilder[ExcludeDocs, MissingNotAllowed, AnyDocType] =
    new MDBuilder(client, db, typeMapping)
}
