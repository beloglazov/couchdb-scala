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

package com.ibm.couchdb.spec

object SpecConfig {

  val couchDbHost      = System.getProperty("couchDbHost", "127.0.0.1")
  val couchDbPort      = System.getProperty("couchDbPort", "5984").toInt
  val couchDbHttpsPort = System.getProperty("couchDbHttpsPort", "6984").toInt
  val couchDbUsername  = System.getProperty("couchDbUsername", "admin")
  val couchDbPassword  = System.getProperty("couchDbPassword", "admin")

  private val log = org.log4s.getLogger

  log.info("----------------------")
  log.info(s"couchDbHost: $couchDbHost")
  log.info(s"couchDbPort: $couchDbPort")
  log.info("----------------------")
}
