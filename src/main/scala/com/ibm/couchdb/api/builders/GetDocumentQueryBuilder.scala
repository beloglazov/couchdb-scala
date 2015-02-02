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
import com.ibm.couchdb.model.Res
import org.http4s.Status

import scalaz.concurrent.Task

case class GetDocumentQueryBuilder(client: Client,
                                   db: String,
                                   params: Map[String, String] = Map.empty[String, String]) {

  def attachments(attachments: Boolean = true): GetDocumentQueryBuilder = {
    set("attachments", attachments)
  }

  def attEncodingInfo(attEncodingInfo: Boolean = true): GetDocumentQueryBuilder = {
    set("att_encoding_info", attEncodingInfo)
  }

  def attsSince(attsSince: Seq[String]): GetDocumentQueryBuilder = {
    set("att_encoding_info", upickle.write(attsSince))
  }

  def conflicts(conflicts: Boolean = true): GetDocumentQueryBuilder = {
    set("conflicts", conflicts)
  }

  def deletedConflicts(deletedConflicts: Boolean = true): GetDocumentQueryBuilder = {
    set("deleted_conflicts", deletedConflicts)
  }

  def latest(latest: Boolean = true): GetDocumentQueryBuilder = {
    set("latest", latest)
  }

  def localSeq(localSeq: Boolean = true): GetDocumentQueryBuilder = {
    set("local_seq", localSeq)
  }

  def meta(meta: Boolean = true): GetDocumentQueryBuilder = {
    set("meta", meta)
  }

  def openRevs(openRevs: Seq[String]): GetDocumentQueryBuilder = {
    set("open_revs", upickle.write(openRevs))
  }

  def openRevs(openRevs: String): GetDocumentQueryBuilder = {
    set("open_revs", openRevs)
  }

  def rev(rev: String): GetDocumentQueryBuilder = {
    set("rev", rev)
  }

  def revs(revs: Boolean = true): GetDocumentQueryBuilder = {
    set("revs", revs)
  }

  def revsInfo(revsInfo: Boolean = true): GetDocumentQueryBuilder = {
    set("revs_info", revsInfo)
  }

  private def set(key: String, value: String): GetDocumentQueryBuilder = {
    copy(params = params.updated(key, value))
  }

  private def set(key: String, value: Any): GetDocumentQueryBuilder = {
    set(key, value.toString)
  }

  def query[T: upickle.Reader](id: String): Task[CouchDoc[T]] = {
    if (id.isEmpty)
      Res.Error("not_found", "No ID specified").toTask[CouchDoc[T]]
    else
      client.get[CouchDoc[T]](
        s"/$db/$id",
        Status.Ok,
        params.toSeq)
  }

}
