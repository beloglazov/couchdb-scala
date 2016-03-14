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

import com.ibm.couchdb.spec.CouchDbSpecification

class DesignSpec extends CouchDbSpecification {

  val db        = "couchdb-scala-design-spec"
  val databases = new Databases(client)
  val design    = new Design(client, db)
  val documents = new Documents(client, db, typeMapping)

  private def clear() = recreateDb(databases, db)

  "Design API" >> {

    "Create a design document" >> {
      clear()
      awaitDocOk(design.create(fixDesign), s"_design/${fixDesign.name}")
    }

    "Get info about a design document" >> {
      clear()
      awaitDocOk(design.create(fixDesign))
      val info = awaitRight(design.info(fixDesign.name))
      info.name mustEqual fixDesign.name
      info.view_index.language mustEqual "javascript"
      info.view_index.disk_size must beGreaterThan(0)
    }

    "Get a design document" >> {
      clear()
      awaitDocOk(design.create(fixDesign))
      val doc = awaitRight(design.get(fixDesign.name))
      doc._id mustEqual s"_design/${fixDesign.name}"
      doc._rev must beRev
      doc.views must haveKey("names")
    }

    "Get a design document by ID" >> {
      clear()
      val created = awaitRight(design.create(fixDesign))
      val doc = awaitRight(design.getById(created.id))
      doc._id mustEqual s"_design/${fixDesign.name}"
      doc._rev must beRev
      doc.views must haveKey("names")
    }

    "Update a design document" >> {
      clear()
      awaitDocOk(design.create(fixDesign))
      val initial = awaitRight(design.get(fixDesign.name))
      val docOk = awaitRight(design.update(initial.copy(language = "typescript")))
      checkDocOk(docOk, s"_design/${fixDesign.name}")
      val updated = awaitRight(design.get(fixDesign.name))
      updated._rev mustEqual docOk.rev
      updated._id mustEqual initial._id
      updated.views mustEqual initial.views
      updated.language mustEqual "typescript"
    }

    "Delete a design document" >> {
      clear()
      awaitDocOk(design.create(fixDesign))
      val doc = awaitRight(design.get(fixDesign.name))
      awaitDocOk(design.delete(doc), s"_design/${fixDesign.name}")
    }

    "Delete a design document by name" >> {
      clear()
      awaitDocOk(design.create(fixDesign))
      awaitDocOk(design.deleteByName(fixDesign.name), s"_design/${fixDesign.name}")
    }

    "Attach a byte array to a design" >> {
      clear()
      awaitDocOk(design.create(fixDesign))
      val doc = awaitRight(design.get(fixDesign.name))
      awaitDocOk(design.attach(doc, fixAttachmentName, fixAttachmentData), doc._id)
    }

    "Get a byte array attachment" >> {
      clear()
      awaitDocOk(design.create(fixDesign))
      val doc = awaitRight(design.get(fixDesign.name))
      awaitDocOk(design.attach(doc, fixAttachmentName, fixAttachmentData), doc._id)
      awaitRight(design.getAttachment(doc, fixAttachmentName)) mustEqual fixAttachmentData
    }

    "Get a design with attachment stubs" >> {
      clear()
      awaitDocOk(design.create(fixDesign))
      val designDoc = awaitRight(design.get(fixDesign.name))
      awaitDocOk(
        design.attach(designDoc, fixAttachmentName, fixAttachmentData, fixAttachmentContentType),
        designDoc._id)
      val doc = awaitRight(design.get(fixDesign.name))
      doc._id mustEqual designDoc._id
      doc._attachments must haveLength(1)
      doc._attachments must haveKey(fixAttachmentName)
      val meta = doc._attachments(fixAttachmentName)
      meta.content_type mustEqual fixAttachmentContentType
      meta.length mustEqual fixAttachmentData.length
      meta.stub mustEqual true
      meta.digest must not be empty
    }

    "Get a design with attachments inline" >> {
      clear()
      awaitDocOk(design.create(fixDesign))
      val designDoc = awaitRight(design.get(fixDesign.name))
      awaitDocOk(
        design.attach(designDoc, fixAttachmentName, fixAttachmentData, fixAttachmentContentType),
        designDoc._id)
      val doc = awaitRight(design.getWithAttachments(fixDesign.name))
      doc._id mustEqual designDoc._id
      doc._attachments must haveLength(1)
      doc._attachments must haveKey(fixAttachmentName)
      val attachment = doc._attachments(fixAttachmentName)
      attachment.content_type mustEqual fixAttachmentContentType
      attachment.length mustEqual -1
      attachment.stub mustEqual false
      attachment.digest must not be empty
      attachment.toBytes mustEqual fixAttachmentData
    }

    "Delete an attachment to a design" >> {
      clear()
      awaitDocOk(design.create(fixDesign))
      val doc = awaitRight(design.get(fixDesign.name))
      val attachment = awaitRight(design.attach(doc, fixAttachmentName, fixAttachmentData))
      val docWithAttachment = awaitRight(design.get(fixDesign.name))
      awaitDocOk(design.deleteAttachment(docWithAttachment, fixAttachmentName), attachment.id)
      awaitError(design.getAttachment(doc, fixAttachmentName), "not_found")
    }
  }
}
