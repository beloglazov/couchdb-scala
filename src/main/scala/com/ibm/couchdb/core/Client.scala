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

package com.ibm.couchdb.core

import com.ibm.couchdb._
import com.ibm.couchdb.implicits.UpickleImplicits
import com.ibm.couchdb.model.{Config, Res}
import org.http4s.Http4s._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.util.CaseInsensitiveString
import scodec.bits.ByteVector

import scalaz.Scalaz._
import scalaz.concurrent.Task

class Client(config: Config) extends UpickleImplicits {

  type R[T] = upickle.Reader[T]
  type W[T] = upickle.Writer[T]

  private val log = org.log4s.getLogger

  val client = PooledHttp1Client()

  val uriBase = Uri(
    scheme = CaseInsensitiveString(config.scheme).some,
    authority = Uri.Authority(
      host = Uri.IPv4(address = config.host),
      port = config.port.some
    ).some)

  def url(resource: String, params: Seq[(String, String)] = Seq.empty[(String, String)]): Uri = {
    uriBase.copy(path = resource).setQueryParams(
      params.map(x => (x._1, Seq(x._2))).toMap)
  }

  def req(request: Request, expectedStatus: Status): Task[Response] = {
    log.debug(s"Making a request $request")
    client(request) flatMap { response =>
      log.debug(s"Received response $response")
      if (response.status == expectedStatus) {
        Task.now(response)
      } else {
        log.warn(s"Unexpected response status ${ response.status }, expected $expectedStatus")
        for {
          responseBody <- response.as[String]
          requestBody <- EntityDecoder.decodeString(request)
          errorRaw = upickle.read[Res.Error](responseBody)
          error = errorRaw.copy(
            status = response.status,
            request = request.toString,
            requestBody = requestBody
          )
          _ = log.warn(s"Request error $error")
          fail <- Task.fail(CouchException[Res.Error](error))
        } yield fail
      }
    }
  }

  def reqAndRead[T: R](request: Request, expectedStatus: Status): Task[T] = {
    for {
      response <- req(request, expectedStatus)
      asString <- response.as[String]
    } yield upickle.read[T](asString)
  }

  def getRaw(resource: String,
             expectedStatus: Status,
             params: Seq[(String, String)] = Seq.empty[(String, String)]): Task[String] = {
    val request = Request(
      method = GET,
      uri = url(resource, params),
      headers = Headers(Header("Accept", "application/json")))
    req(request, expectedStatus).as[String]
  }

  def get[T: R](resource: String,
                expectedStatus: Status,
                params: Seq[(String, String)] = Seq.empty[(String, String)]): Task[T] = {
    val request = Request(
      method = GET,
      uri = url(resource, params),
      headers = Headers(Header("Accept", "application/json")))
    reqAndRead[T](request, expectedStatus)
  }

  def getBinary(resource: String, expectedStatus: Status): Task[Array[Byte]] = {
    val request = Request(
      method = GET,
      uri = url(resource))
    req(request, expectedStatus).as[ByteVector].map(_.toArray)
  }

  private def put[T: R](resource: String,
                        expectedStatus: Status,
                        entity: EntityEncoder.Entity,
                        contentType: String): Task[T] = {
    val baseHeaders = Headers(Header("Accept", "application/json"))
    val headers =
      if (!contentType.isEmpty) baseHeaders.put(Header("Content-Type", contentType))
      else baseHeaders
    val request = Request(
      method = PUT,
      uri = url(resource),
      headers = headers,
      body = entity.body)
    reqAndRead[T](request, expectedStatus)
  }

  def put[B: W, T: R](resource: String,
                      expectedStatus: Status,
                      body: B): Task[T] = {
    EntityEncoder[String].toEntity(upickle.write(body)) flatMap { entity =>
      put[T](resource, expectedStatus, entity, "")
    }
  }

  def put[T: R](resource: String,
                expectedStatus: Status,
                body: Array[Byte] = Array(),
                contentType: String = ""): Task[T] = {
    EntityEncoder[Array[Byte]].toEntity(body) flatMap { entity =>
      put[T](resource, expectedStatus, entity, contentType)
    }
  }

  private def post[T: R](resource: String,
                         expectedStatus: Status,
                         entity: EntityEncoder.Entity,
                         params: Seq[(String, String)]): Task[T] = {
    val request = Request(
      method = POST,
      uri = url(resource, params),
      headers = Headers(
        Header("Accept", "application/json"),
        Header("Content-Type", "application/json")),
      body = entity.body)
    reqAndRead[T](request, expectedStatus)
  }

  def post[B: W, T: R](resource: String,
                       expectedStatus: Status,
                       body: B,
                       params: Seq[(String, String)] = Seq.empty[(String, String)]): Task[T] = {
    EntityEncoder[String].toEntity(upickle.write(body)) flatMap { entity =>
      post[T](resource, expectedStatus, entity, params)
    }
  }

  def delete[T: R](resource: String, expectedStatus: Status): Task[T] = {
    val request = Request(
      method = DELETE,
      uri = url(resource),
      headers = Headers(Header("Accept", "application/json")))
    reqAndRead[T](request, expectedStatus)
  }

}
