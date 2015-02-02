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

package com.ibm.couchdb.model

import scalaz.Scalaz._
import scalaz._

trait TypeMappingWrapper {

  final class TypeMapping private(val types: Map[String, String])

  object TypeMapping {

    def apply(mapping: (Class[_], String)*): String \/ TypeMapping = {
      if (mapping.map((x: (Class[_], String)) => x._2).toSet.size != mapping.size) {
        "Type aliases must be unique".left[TypeMapping]
      } else {
        new TypeMapping(mapping.map((x: (Class[_], String)) =>
          (x._1.getCanonicalName, x._2)).toMap).right[String]
      }
    }

    val empty = new TypeMapping(Map.empty[String, String])

  }

}
