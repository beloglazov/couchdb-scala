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

package com.ibm.couchdb.api

import com.ibm.couchdb._
import com.ibm.couchdb.core.Client
import org.http4s.Status

import scalaz.concurrent.Task

class Design(client: Client, db: String) {

  def create(design: CouchDesign): Task[Res.DocOk] = {
    if (design.name.isEmpty)
      Res.Error("cannot_create", "Design name must not be empty").toTask[Res.DocOk]
    else
      client.put[CouchDesign, Res.DocOk](
        s"/$db/_design/${ design.name }",
        Status.Created,
        design)
  }

  def info(name: String): Task[Res.DesignInfo] = {
    if (name.isEmpty)
      Res.Error("not_found", "Design name must not be empty").toTask[Res.DesignInfo]
    else
      client.get[Res.DesignInfo](
        s"/$db/_design/$name/_info",
        Status.Ok)
  }

  def get(name: String): Task[CouchDesign] = {
    if (name.isEmpty)
      Res.Error("not_found", "Design name must not be empty").toTask[CouchDesign]
    else
      client.get[CouchDesign](
        s"/$db/_design/$name",
        Status.Ok
      )
  }

  def getWithAttachments(name: String): Task[CouchDesign] = {
    if (name.isEmpty)
      Res.Error("not_found", "Design name must not be empty").toTask[CouchDesign]
    else
      client.get[CouchDesign](
        s"/$db/_design/$name?attachments=true",
        Status.Ok
      )
  }

  def getById(id: String): Task[CouchDesign] = {
    if (id.isEmpty)
      Res.Error("not_found", "Design ID must not be empty").toTask[CouchDesign]
    else
      client.get[CouchDesign](
        s"/$db/$id",
        Status.Ok
      )
  }

  def update(design: CouchDesign): Task[Res.DocOk] = {
    if (design._id.isEmpty)
      Res.Error("cannot_update", "Design ID must not be empty").toTask[Res.DocOk]
    else
      client.put[CouchDesign, Res.DocOk](
        s"/$db/${ design._id }",
        Status.Created,
        design)
  }

  def delete(design: CouchDesign): Task[Res.DocOk] = {
    if (design._id.isEmpty)
      Res.Error("cannot_delete", "Design ID must not be empty").toTask[Res.DocOk]
    else
      client.delete[Res.DocOk](
        s"/$db/${ design._id }?rev=${ design._rev }",
        Status.Ok)
  }

  def deleteByName(name: String): Task[Res.DocOk] = {
    if (name.isEmpty)
      Res.Error("cannot_delete", "Design name must not be empty").toTask[Res.DocOk]
    else
      get(name) flatMap delete
  }

  def attach(design: CouchDesign,
             name: String,
             data: Array[Byte],
             contentType: String = ""): Task[Res.DocOk] = {
    if (design._id.isEmpty)
      Res.Error("cannot_attach", "Design ID must not be empty").toTask[Res.DocOk]
    else
      client.put[Res.DocOk](
        s"/$db/${ design._id }/$name?rev=${ design._rev }",
        Status.Created,
        data,
        contentType)
  }

  def getAttachment(design: CouchDesign, name: String): Task[Array[Byte]] = {
    if (design._id.isEmpty)
      Res.Error("not_found", "Design ID must not be empty").toTask[Array[Byte]]
    else
      client.getBinary(
        s"/$db/${ design._id }/$name",
        Status.Ok)
  }

  def deleteAttachment(design: CouchDesign, name: String): Task[Res.DocOk] = {
    if (design._id.isEmpty)
      Res.Error("cannot_delete", "Design ID must not be empty").toTask[Res.DocOk]
    else if (name.isEmpty)
      Res.Error("cannot_delete", "Attachment name must not be empty").toTask[Res.DocOk]
    else
      client.delete[Res.DocOk](
        s"/$db/${ design._id }/$name?rev=${ design._rev }",
        Status.Ok)
  }

}
