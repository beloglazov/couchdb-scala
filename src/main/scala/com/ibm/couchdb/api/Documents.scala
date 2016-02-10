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
import upickle.default.Aliases.{R, W}

import scalaz.concurrent.Task

class Documents(client: Client, db: String, typeMapping: TypeMapping) {

  val types  = typeMapping.types
  val server = new Server(client)

  private def getClassName[D](obj: D): String = obj.getClass.getCanonicalName

  def create[D: W](obj: D): Task[Res.DocOk] = {
    server.mkUuid flatMap (create(obj, _))
  }

  def create[D: W](obj: D, id: String): Task[Res.DocOk] = {
    val cl = getClassName(obj)
    if (!types.contains(cl))
      Res.Error("cannot_create", "No type mapping for " + cl + " available: " + types).toTask[Res.DocOk]
    else
      client.put[CouchDoc[D], Res.DocOk](
        s"/$db/$id",
        Status.Created,
        CouchDoc[D](obj, types(cl)))
  }

  private def postBulk[D: W](objs: Seq[CouchDoc[D]]): Task[Seq[Res.DocOk]] = {
    client.post[Req.Docs[D], Seq[Res.DocOk]](
      s"/$db/_bulk_docs",
      Status.Created, Req.Docs(objs))
  }

  def createMany[D: W](objs: Map[String, D]): Task[Seq[Res.DocOk]] = create(objs.toSeq)

  def createMany[D: W](objs: Seq[D]): Task[Seq[Res.DocOk]] = create(objs.map(("", _)))

  private def create[D: W, S](objs: Seq[(String, D)]): Task[Seq[Res.DocOk]] = {
    objs.find { x => !types.contains(getClassName(x._2)) } match {
      case Some(missing) =>
        Res.Error("cannot_create", "No type mapping for " + missing).toTask[Seq[Res.DocOk]]
      case None =>
        postBulk(objs.map { o => CouchDoc[D](_id = o._1, doc = o._2, kind = types(getClassName(o._2))) })
    }
  }

  def updateMany[D: W](objs: Seq[CouchDoc[D]]): Task[Seq[Res.DocOk]] = {
    def invalidDoc(x: CouchDoc[D]): Boolean = x._id.isEmpty || x._rev.isEmpty
    objs.find(invalidDoc) match {
      case Some(doc) =>
        val missingField = if (doc._id.isEmpty) "an ID" else "a REV number"
        Res.Error("cannot_update", s"One or more documents do not contain $missingField.").toTask[Seq[Res.DocOk]]
      case None => postBulk(objs)
    }
  }

  def deleteMany[D: W](objs: Seq[CouchDoc[D]]): Task[Seq[Res.DocOk]] = {
    updateMany(objs.map(_.copy(_deleted = true)))
  }

  def get: GetDocumentQueryBuilder = GetDocumentQueryBuilder(client, db)

  def get[D: R](id: String): Task[CouchDoc[D]] = {
    get.query[D](id)
  }

  def getMany: GetManyDocumentsQueryBuilder = GetManyDocumentsQueryBuilder(client, db)

  def getMany[D: R](ids: Seq[String]): Task[CouchDocs[String, CouchDocRev, D]] = {
    getMany.queryIncludeDocs[D](ids)
  }

  def update[D: W](obj: CouchDoc[D]): Task[Res.DocOk] = {
    if (obj._id.isEmpty)
      Res.Error("cannot_update", "Document ID must not be empty").toTask[Res.DocOk]
    else
      client.put[CouchDoc[D], Res.DocOk](
        s"/$db/${obj._id}",
        Status.Created,
        obj)
  }

  def delete[D](obj: CouchDoc[D]): Task[Res.DocOk] = {
    if (obj._id.isEmpty)
      Res.Error("cannot_delete", "Document ID must not be empty").toTask[Res.DocOk]
    else {
      client.delete[Res.DocOk](
        s"/$db/${obj._id}?rev=${obj._rev}",
        Status.Ok)
    }
  }

  def attach[D](obj: CouchDoc[D],
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

  def attach[D](obj: CouchDoc[D], name: String, path: String): Task[Res.DocOk] = {
    readFile(path) flatMap {
      attachment => attach(obj, name, attachment)
    }
  }

  def getAttachmentResource[D](obj: CouchDoc[D], name: String): Task[String] = {
    if (obj._id.isEmpty)
      Res.Error("not_found", "Document ID must not be empty").toTask[String]
    else
      Task.now(s"/$db/${obj._id}/$name")
  }

  def getAttachmentUrl[D](obj: CouchDoc[D], name: String): Task[String] = {
    getAttachmentResource(obj, name).map(client.url(_).toString())
  }

  def getAttachment[D](obj: CouchDoc[D], name: String): Task[Array[Byte]] = {
    getAttachmentResource(obj, name).flatMap(client.getBinary(_, Status.Ok))
  }

  def deleteAttachment[D](obj: CouchDoc[D], name: String): Task[Res.DocOk] = {
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
