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

import java.nio.file.{Files, Paths}

import com.ibm.couchdb._
import com.ibm.couchdb.api.builders.{GetDocumentQueryBuilder, GetManyDocumentsQueryBuilder}
import com.ibm.couchdb.core.Client
import com.ibm.couchdb.model._
import org.http4s.Status

import scalaz.concurrent.Task

class Documents(client: Client, db: String, typeMapping: TypeMapping) {

  val types  = typeMapping.types
  val server = new Server(client)

  private def getClassName[T](obj: T): String = obj.getClass.getCanonicalName

  def create[T: upickle.Writer](obj: T): Task[Res.DocOk] = {
    server.mkUuid flatMap (create(obj, _))
  }

  def create[T: upickle.Writer](obj: T, id: String): Task[Res.DocOk] = {
    val cl = getClassName(obj)
    if (!types.contains(cl))
      Res.Error("cannot_create", "No type mapping for " + cl + " available: " + types).toTask[Res.DocOk]
    else
      client.put[CouchDoc[T], Res.DocOk](
        s"/$db/$id",
        Status.Created,
        CouchDoc[T](obj, types(cl)))
  }

  def createMany[T: upickle.Writer](objs: Seq[T]): Task[Seq[Res.DocOk]] = {
    val classes = objs.map(getClassName(_))
    if (classes.exists(!types.contains(_))) {
      val missing = classes.find(!types.contains(_))
      Res.Error("cannot_create", "No type mapping for " + missing).toTask[Seq[Res.DocOk]]
    } else
      client.post[Req.Docs[T], Seq[Res.DocOk]](
        s"/$db/_bulk_docs",
        Status.Created,
        Req.Docs(objs.map(x => CouchDoc[T](x, types(getClassName(x))))))
  }

  def get: GetDocumentQueryBuilder = GetDocumentQueryBuilder(client, db)

  def get[T: upickle.Reader](id: String): Task[CouchDoc[T]] = {
    get.query[T](id)
  }

  def getMany: GetManyDocumentsQueryBuilder = GetManyDocumentsQueryBuilder(client, db)

  def getMany[T: upickle.Reader](ids: Seq[String]): Task[CouchDocs[String, CouchDocRev, T]] = {
    getMany.queryIncludeDocs[T](ids)
  }

  def update[T: upickle.Writer](obj: CouchDoc[T]): Task[Res.DocOk] = {
    if (obj._id.isEmpty)
      Res.Error("cannot_update", "Document ID must not be empty").toTask[Res.DocOk]
    else
      client.put[CouchDoc[T], Res.DocOk](
        s"/$db/${ obj._id }",
        Status.Created,
        obj)
  }

  def delete[T](obj: CouchDoc[T]): Task[Res.DocOk] = {
    if (obj._id.isEmpty)
      Res.Error("cannot_delete", "Document ID must not be empty").toTask[Res.DocOk]
    else {
      client.delete[Res.DocOk](
        s"/$db/${ obj._id }?rev=${ obj._rev }",
        Status.Ok)
    }
  }

  def attach[T](obj: CouchDoc[T],
                name: String,
                data: Array[Byte],
                contentType: String = ""): Task[Res.DocOk] = {
    if (obj._id.isEmpty)
      Res.Error("cannot_attach", "Document ID must not be empty").toTask[Res.DocOk]
    else {
      client.put[Res.DocOk](
        s"/$db/${ obj._id }/$name?rev=${ obj._rev }",
        Status.Created,
        data,
        contentType)
    }
  }

  def attach[T](obj: CouchDoc[T], name: String, path: String): Task[Res.DocOk] = {
    readFile(path) flatMap { attachment => attach(obj, name, attachment) }
  }

  def getAttachmentResource[T](obj: CouchDoc[T], name: String): Task[String] = {
    if (obj._id.isEmpty)
      Res.Error("not_found", "Document ID must not be empty").toTask[String]
    else
      Task.now(s"/$db/${ obj._id }/$name")
  }

  def getAttachmentUrl[T](obj: CouchDoc[T], name: String): Task[String] = {
    getAttachmentResource(obj, name).map(client.url(_).toString())
  }

  def getAttachment[T](obj: CouchDoc[T], name: String): Task[Array[Byte]] = {
    getAttachmentResource(obj, name).flatMap(client.getBinary(_, Status.Ok))
  }

  def deleteAttachment[T](obj: CouchDoc[T], name: String): Task[Res.DocOk] = {
    if (obj._id.isEmpty)
      Res.Error("cannot_delete", "Document ID must not be empty").toTask[Res.DocOk]
    else if (name.isEmpty)
      Res.Error("cannot_delete", "The attachment name is empty").toTask[Res.DocOk]
    else
      client.delete[Res.DocOk](
        s"/$db/${ obj._id }/$name?rev=${ obj._rev }",
        Status.Ok)
  }

  private def readFile(path: String): Task[Array[Byte]] = Task {
    // TODO: replace with scodec / scalaz-stream / scodec-stream?
    Files.readAllBytes(Paths.get(path))
  }

}
