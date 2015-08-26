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
import org.http4s.Status

import scalaz.concurrent.Task

class Documents(client: Client, db: String, typeMapping: TypeMapping) {

  val types  = typeMapping.types
  val server = new Server(client)

  private def getClassName[S](obj: S): String = obj.getClass.getCanonicalName

  def create[S: upickle.default.Writer](obj: S): Task[Res.DocOk] = {
    server.mkUuid flatMap (create(obj, _))
  }

  def create[S: upickle.default.Writer](obj: S, id: String): Task[Res.DocOk] = {
    val cl = getClassName(obj)
    if (!types.contains(cl))
      Res.Error("cannot_create", "No type mapping for " + cl + " available: " + types).toTask[Res.DocOk]
    else
      client.put[CouchDoc[S], Res.DocOk](
        s"/$db/$id",
        Status.Created,
        CouchDoc[S](obj, types(cl)))
  }

  private def postBulk[S: upickle.default.Writer](objs: Seq[CouchDoc[S]]): Task[Seq[Res.DocOk]] = {
    client.post[Req.Docs[S], Seq[Res.DocOk]](
      s"/$db/_bulk_docs",
      Status.Created, Req.Docs(objs))
  }

  def createMany[S: upickle.default.Writer](objs: Seq[S]): Task[Seq[Res.DocOk]] = {
    val classes = objs.map(getClassName(_))
    if (classes.exists(!types.contains(_))) {
      val missing = classes.find(!types.contains(_))
      Res.Error("cannot_create", "No type mapping for " + missing).toTask[Seq[Res.DocOk]]
    } else
      postBulk(objs.map(x => CouchDoc[S](x, types(getClassName(x)))))
  }

  def updateMany[S: upickle.default.Writer](objs: Seq[CouchDoc[S]]): Task[Seq[Res.DocOk]] = {
    def invalidDoc(x: CouchDoc[S]): Boolean = x._id.isEmpty || x._rev.isEmpty
    objs.find(invalidDoc) match {
      case Some(doc) =>
        val missingField = if (doc._id.isEmpty) "an ID" else "a REV number"
        Res.Error("cannot_update", s"One or more documents do not contain $missingField.").toTask[Seq[Res.DocOk]]
      case None => postBulk(objs)
    }
  }

  def deleteMany[S: upickle.default.Writer](objs: Seq[CouchDoc[S]]): Task[Seq[Res.DocOk]] = {
    updateMany(objs.map(_.copy(_deleted = true)))
  }

  def get: GetDocumentQueryBuilder = GetDocumentQueryBuilder(client, db)

  def get[S: upickle.default.Reader](id: String): Task[CouchDoc[S]] = {
    get.query[S](id)
  }

  def getMany: GetManyDocumentsQueryBuilder = GetManyDocumentsQueryBuilder(client, db)

  def getMany[S: upickle.default.Reader](ids: Seq[String]): Task[CouchDocs[String, CouchDocRev, S]] = {
    getMany.queryIncludeDocs[S](ids)
  }

  def update[S: upickle.default.Writer](obj: CouchDoc[S]): Task[Res.DocOk] = {
    if (obj._id.isEmpty)
      Res.Error("cannot_update", "Document ID must not be empty").toTask[Res.DocOk]
    else
      client.put[CouchDoc[S], Res.DocOk](
        s"/$db/${obj._id}",
        Status.Created,
        obj)
  }

  def delete[S](obj: CouchDoc[S]): Task[Res.DocOk] = {
    if (obj._id.isEmpty)
      Res.Error("cannot_delete", "Document ID must not be empty").toTask[Res.DocOk]
    else {
      client.delete[Res.DocOk](
        s"/$db/${obj._id}?rev=${obj._rev}",
        Status.Ok)
    }
  }

  def attach[S](obj: CouchDoc[S],
                name: String,
                data: Array[Byte],
                contentType: String = ""): Task[Res.DocOk] = {
    if (obj._id.isEmpty)
      Res.Error("cannot_attach", "Document ID must not be empty").toTask[Res.DocOk]
    else {
      client.put[Res.DocOk](
        s"/$db/${obj._id}/$name?rev=${obj._rev}",
        Status.Created,
        data,
        contentType)
    }
  }

  def attach[S](obj: CouchDoc[S], name: String, path: String): Task[Res.DocOk] = {
    readFile(path) flatMap {
      attachment => attach(obj, name, attachment)
    }
  }

  def getAttachmentResource[S](obj: CouchDoc[S], name: String): Task[String] = {
    if (obj._id.isEmpty)
      Res.Error("not_found", "Document ID must not be empty").toTask[String]
    else
      Task.now(s"/$db/${obj._id}/$name")
  }

  def getAttachmentUrl[S](obj: CouchDoc[S], name: String): Task[String] = {
    getAttachmentResource(obj, name).map(client.url(_).toString())
  }

  def getAttachment[S](obj: CouchDoc[S], name: String): Task[Array[Byte]] = {
    getAttachmentResource(obj, name).flatMap(client.getBinary(_, Status.Ok))
  }

  def deleteAttachment[S](obj: CouchDoc[S], name: String): Task[Res.DocOk] = {
    if (obj._id.isEmpty)
      Res.Error("cannot_delete", "Document ID must not be empty").toTask[Res.DocOk]
    else if (name.isEmpty)
      Res.Error("cannot_delete", "The attachment name is empty").toTask[Res.DocOk]
    else
      client.delete[Res.DocOk](
        s"/$db/${obj._id}/$name?rev=${obj._rev}",
        Status.Ok)
  }

  private def readFile(path: String): Task[Array[Byte]] = Task {
    // TODO: replace with scodec / scalaz-stream / scodec-stream?
    Files.readAllBytes(Paths.get(path))
  }

}
