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

import com.ibm.couchdb.Res
import com.ibm.couchdb.core.Client
import org.http4s.Status
import upickle.default.Aliases.W
import upickle.default.write

import scalaz.concurrent.Task

case class ListQueryBuilder(client: Client,
                            db: String,
                            design: String,
                            list: String,
                            params: Map[String, String] = Map.empty[String, String]) {

  def format(format: String): ListQueryBuilder = {
    set("format", format)
  }

  def descending(descending: Boolean = true): ListQueryBuilder = {
    set("descending", descending)
  }

  def endKey[K: W](endKey: K): ListQueryBuilder = {
    set("endkey", write(endKey))
  }

  def endKeyDocId(endKeyDocId: String): ListQueryBuilder = {
    set("endkey_docid", endKeyDocId)
  }

  def group(group: Boolean = true): ListQueryBuilder = {
    set("group", group)
  }

  def groupLevel(groupLevel: Int): ListQueryBuilder = {
    set("group_level", groupLevel)
  }

  def inclusiveEnd(inclusiveEnd: Boolean = true): ListQueryBuilder = {
    set("inclusive_end", inclusiveEnd)
  }

  def key[K: W](key: K): ListQueryBuilder = {
    set("key", write(key))
  }

  def limit(limit: Int): ListQueryBuilder = {
    set("limit", limit)
  }

  def reduce(reduce: Boolean = true): ListQueryBuilder = {
    set("reduce", reduce)
  }

  def skip(skip: Int): ListQueryBuilder = {
    set("skip", skip)
  }

  def stale(stale: String): ListQueryBuilder = {
    set("stale", stale)
  }

  def startKey[K: W](startKey: K): ListQueryBuilder = {
    set("startkey", write(startKey))
  }

  def startKeyDocId(startKeyDocId: String): ListQueryBuilder = {
    set("startkey_docid", startKeyDocId)
  }

  def updateSeq(updateSeq: Boolean = true): ListQueryBuilder = {
    set("update_seq", updateSeq)
  }

  def addParam(name: String, value: String): ListQueryBuilder = {
    set(name, value)
  }

  private def set(key: String, value: String): ListQueryBuilder = {
    copy(params = params.updated(key, value))
  }

  private def set(key: String, value: Any): ListQueryBuilder = {
    set(key, value.toString)
  }

  def query(view: String): Task[String] = {
    if (view.isEmpty)
      Res.Error("not_found", "View name must not be empty").toTask[String]
    else
      client.getRaw(
        s"/$db/_design/$design/_list/$list/$view",
        Status.Ok,
        params.toSeq)
  }

  def queryAnotherDesign(view: String, anotherDesign: String): Task[String] = {
    if (view.isEmpty)
      Res.Error("not_found", "View name must not be empty").toTask[String]
    else
      client.getRaw(
        s"/$db/_design/$anotherDesign/_list/$list/$view",
        Status.Ok,
        params.toSeq)
  }

}
