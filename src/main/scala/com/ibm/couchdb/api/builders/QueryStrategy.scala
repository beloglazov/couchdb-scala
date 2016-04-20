/*
 * Copyright 2016 IBM Corporation
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

import com.ibm.couchdb.api.Query
import com.ibm.couchdb.core.Client
import com.ibm.couchdb.{CouchDocs, CouchView, Res, TypeMapping}
import upickle.default.Aliases.{R, W}
import upickle.default._

import scala.reflect.ClassTag
import scalaz.concurrent.Task

case class QueryBasic[C: R](
    client: Client, db: String, params: Map[String, String] = Map.empty,
    ids: Seq[String] = Seq.empty) {
  def query: Task[C] = {
    val url = s"/$db/_all_docs"
    ids match {
      case Nil => QueryUtils.query[C](client, url, params)
      case _ => QueryUtils.queryByIds[String, C](client, url, ids, params)
    }
  }
}

case class QueryByType[K, V, D: R](
    client: Client, db: String, typeFilterView: CouchView,
    typeMappings: TypeMapping, params: Map[String, String] = Map.empty,
    ids: Seq[String] = Seq.empty)
    (implicit tag: ClassTag[D], kr: R[K], kw: W[K], vr: R[V]) {

  def query: Task[CouchDocs[K, V, D]] = {
    typeMappings.forType(tag.runtimeClass) match {
      case Some(k) => queryByType(typeFilterView, k)
      case None => Res.Error("not_found", s"type mapping not found").toTask
    }
  }

  private def queryByType(view: CouchView, kind: String, ps: Map[String, String] = Map.empty) = {
    new Query(client, db).temporaryView[K, V](view) match {
      case Some(v) => v.startKey(Tuple1(kind)).endKey(Tuple2(kind, {})).queryIncludeDocs[D]
      case None => Res.Error("not_found", "invalid view specified").toTask
    }
  }
}
