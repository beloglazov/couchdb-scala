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

import scalaz.concurrent.Task

case class ShowQueryBuilder(
    client: Client,
    db: String,
    design: String,
    show: String,
    params: Map[String, String] = Map.empty[String, String]) {

  def details(details: Boolean = true): ShowQueryBuilder = {
    set("details", details)
  }

  def format(format: String): ShowQueryBuilder = {
    set("format", format)
  }

  def addParam(name: String, value: String): ShowQueryBuilder = {
    set(name, value)
  }

  private def set(key: String, value: String): ShowQueryBuilder = {
    copy(params = params.updated(key, value))
  }

  private def set(key: String, value: Any): ShowQueryBuilder = {
    set(key, value.toString)
  }

  def query: Task[String] = {
    client.getRaw(
      s"/$db/_design/$design/_show/$show",
      Status.Ok,
      params.toSeq)
  }

  def query(id: String): Task[String] = {
    if (id.isEmpty)
      Res.Error("not_found", "Document ID must not be empty").toTask
    else
      client.getRaw(
        s"/$db/_design/$design/_show/$show/$id",
        Status.Ok,
        params.toSeq)
  }
}
