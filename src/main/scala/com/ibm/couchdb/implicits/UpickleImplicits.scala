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

import com.ibm.couchdb._
import org.http4s.Status
import upickle.Js.Value
import upickle.default.Aliases.{R, W}
import upickle.default.{Reader => Rr, Writer => Wr, readJs => rJs, writeJs => wJs}
import upickle.{Js, Types}

import scala.util.Try
import scalaz.{-\/, \/, \/-}

trait UpickleImplicits extends Types {
  implicit val statusW: W[Status] = Wr[Status] {
    x => Js.Num(x.code.toDouble)
  }

  implicit val statusR: R[Status] = Rr[Status] {
    case json: Js.Num => Status.fromInt(json.value.toInt).toOption.get
  }

  implicit def dockViewWithKeysW[K: W]: W[Req.ViewWithKeys[K]] =
    Wr[Req.ViewWithKeys[K]] {
                              case Req.ViewWithKeys(keys, CouchView(map, reduce)) =>
                               Js.Obj(mapReduceParams(map, reduce) ++ Seq("keys" -> wJs(keys)): _*)
                           }

  implicit val couchViewW: W[CouchView] = Wr[CouchView]
    { case CouchView(map, reduce) => Js.Obj(mapReduceParams(map, reduce): _*)
    }

  private def mapReduceParams(map: String, reduce: String = ""): Seq[(String, Value)] = {
    val m = Seq("map" -> wJs(map))
    if (reduce.isEmpty) m else m ++ Seq("reduce" -> wJs(reduce))
  }

  implicit def couchKeyValOrErrorR[K: R, V: R]: Rr[\/[CouchKeyError[K], CouchKeyVal[K, V]]] =
    Rr { case o: Js.Obj => Try(\/-(rJs[CouchKeyVal[K, V]](o))).getOrElse(-\/(rJs[CouchKeyError[K]](o))) }

  implicit def couchKeyValDocOrErrorR[K: R, V: R, D: R]: Rr[\/[CouchKeyError[K], CouchKeyValWithDoc[K, V, D]]] =
    Rr { case o: Js.Obj => Try(\/-(rJs[CouchKeyValWithDoc[K, V, D]](o))).getOrElse(-\/(rJs[CouchKeyError[K]](o))) }
}
