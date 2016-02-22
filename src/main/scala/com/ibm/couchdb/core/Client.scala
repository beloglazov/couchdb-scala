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
import org.http4s.Http4s._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.headers.Authorization
import org.http4s.util.CaseInsensitiveString
import scodec.bits.ByteVector
import upickle.default.Aliases.{R, W}
import upickle.default.{read, write}

import scalaz.Scalaz._
import scalaz.concurrent.Task

class Client(config: Config) {

  private val log = org.log4s.getLogger

  val client = PooledHttp1Client()

  val baseHeaders = config.credentials match {
    case Some(x) => Headers(Authorization(BasicCredentials(x._1, x._2)))
    case None => Headers()
  }
  val baseHeadersWithAccept = baseHeaders.put(Header("Accept", "application/json; charset=utf-8"))

  val baseUri = Uri(
    scheme = CaseInsensitiveString(if (config.https) "https" else "http").some,
    authority = Uri.Authority(
      host = Uri.IPv4(address = config.host),
      port = config.port.some
    ).some)

  def url(resource: String, params: Seq[(String, String)] = Seq.empty[(String, String)]): Uri = {
    baseUri.copy(path = resource).setQueryParams(
      params.map(x => (x._1, Seq(x._2))).toMap)
  }

  def req(request: Request, expectedStatus: Status): Task[Response] = {
    log.debug(s"Making a request $request")
    client.toHttpService.run(request) flatMap { response =>
      log.debug(s"Received response $response")
      if (response.status == expectedStatus) {
        Task.now(response)
      } else {
        log.warn(s"Unexpected response status ${response.status}, expected $expectedStatus")
        for {
          responseBody <- response.as[String]
          requestBody <- EntityDecoder.decodeString(request)
          errorRaw = read[Res.Error](responseBody)
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
    } yield read[T](asString)
  }

  def getRaw(resource: String,
             expectedStatus: Status,
             params: Seq[(String, String)] = Seq.empty[(String, String)]): Task[String] = {
    val request = Request(
      method = GET,
      uri = url(resource, params),
      headers = baseHeadersWithAccept)
    req(request, expectedStatus).as[String]
  }

  def get[T: R](resource: String,
                expectedStatus: Status,
                params: Seq[(String, String)] = Seq.empty[(String, String)]): Task[T] = {
    val request = Request(
      method = GET,
      uri = url(resource, params),
      headers = baseHeadersWithAccept)
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
    val headers =
      if (!contentType.isEmpty) baseHeadersWithAccept.put(Header("Content-Type", contentType))
      else baseHeadersWithAccept
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
    EntityEncoder[String].toEntity(write(body)) flatMap { entity =>
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

  def post[B: W, T: R](resource: String,
                       expectedStatus: Status,
                       body: B,
                       params: Seq[(String, String)] = Seq.empty[(String, String)]): Task[T] = {
    EntityEncoder[String].toEntity(write(body)) flatMap { entity =>
      val request = Request(
        method = POST,
        uri = url(resource, params),
        headers = baseHeadersWithAccept.put(
          Header("Content-Type", "application/json")),
        body = entity.body)
      reqAndRead[T](request, expectedStatus)
    }
  }

  def delete[T: R](resource: String, expectedStatus: Status): Task[T] = {
    val request = Request(
      method = DELETE,
      uri = url(resource),
      headers = baseHeadersWithAccept)
    reqAndRead[T](request, expectedStatus)
  }
}
