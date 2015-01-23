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

import com.ibm.couchdb.api.builders.{ListQueryBuilder, ShowQueryBuilder, ViewQueryBuilder}
import com.ibm.couchdb.core.Client

import scalaz.Scalaz._

class Query(client: Client, db: String) {

  def view[K, V](design: String, view: String)
                (implicit
                 kr: upickle.Reader[K],
                 kw: upickle.Writer[K],
                 vr: upickle.Reader[V]): Option[ViewQueryBuilder[K, V]] = {
    if (design.isEmpty || view.isEmpty) none[ViewQueryBuilder[K, V]]
    else ViewQueryBuilder[K, V](client, db, design, view).some
  }

  def show(design: String, show: String): Option[ShowQueryBuilder] = {
    if (design.isEmpty || show.isEmpty) none[ShowQueryBuilder]
    else ShowQueryBuilder(client, db, design, show).some
  }

  def list(design: String, list: String): Option[ListQueryBuilder] = {
    if (design.isEmpty || list.isEmpty) none[ListQueryBuilder]
    else ListQueryBuilder(client, db, design, list).some
  }

}


