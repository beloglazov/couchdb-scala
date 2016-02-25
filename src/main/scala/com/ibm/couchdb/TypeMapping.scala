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

package com.ibm.couchdb

final class TypeMapping private(val types: Map[String, String])

object TypeMapping {

  val empty = new TypeMapping(Map.empty[String, String])

  def apply(mapping: (Class[_], String)*): TypeMapping = {
    new TypeMapping(
      mapping.map((x: (Class[_], String)) => (x._1.getCanonicalName, x._2)).toMap)
  }

  def getMappingFor(t: Class[_], typeMapping: TypeMapping): Option[String] = {
    typeMapping.types.get(t.getCanonicalName)
  }
}

