package eu.codesigner.finagle.ws2redis

import com.twitter.concurrent.Broker
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.redis.Redis
import com.twitter.finagle.redis.protocol.Command
import com.twitter.finagle.redis.protocol.Reply
import com.twitter.finagle.websocket.WebSocket
import com.twitter.util.Future
import com.typesafe.config.Config

/**
 * Websocket to Redis proxy written in Twitter's Finagle.
 * Copyright [2014] [Simone Scarduzio - scarduzio@gmail.com]
 *
 * ------------------------------------------------------------------------
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

class Ws2redisService(conf: Config) extends Service[WebSocket, WebSocket] {

  // Client connection towards Redis server
  val client: Service[Command, Reply] = ClientBuilder()
    .codec(Redis())
    .hosts(conf.getString("redisAddress"))
    .hostConnectionLimit(1)
    .build()

  val filter = new Ws2redisAdapter
  val filteredClient = filter andThen client

  def apply(req: WebSocket): Future[WebSocket] = {
    val outgoing = new Broker[String]
    val socket = req.copy(messages = outgoing.recv)
    val msgs = req.messages

    req.messages foreach (receivedString => {
      val resp = filteredClient(receivedString)
      resp flatMap {
        s => outgoing ! s
      }
    })
    Future.value(socket)
  }

}