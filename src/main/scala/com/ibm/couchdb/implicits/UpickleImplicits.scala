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

package com.ibm.couchdb.implicits

import org.http4s.Status
import upickle.{Js, Types}

trait UpickleImplicits extends Types {
  implicit val statusW: upickle.default.Writer[Status] = upickle.default.Writer[Status] {
    x => Js.Num(x.code.toDouble)
  }

  implicit val statusR: upickle.default.Reader[Status] = upickle.default.Reader[Status] {
    case json: Js.Num => Status.fromInt(json.value.toInt).toOption.get
  }
}
