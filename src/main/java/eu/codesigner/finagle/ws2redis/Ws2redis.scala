package eu.codesigner.finagle.ws2redis

import com.twitter.finagle.HttpWebSocket
import com.twitter.finagle.Service
import com.twitter.finagle.websocket.WebSocket
import com.twitter.util.Future
import com.twitter.concurrent.Broker
import com.twitter.util.{ Await, Future }
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

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

object Ws2redis {
  def main(args: Array[String]) {
    val conf = ConfigFactory.load()
    val rb = new RedisBroker(conf)
    // #TODO? Not sure if we should support JSON Commands or maybe directly implement WAMP as of http://wamp.ws/spec/

    val server = HttpWebSocket.serve(conf.getString("listenAddress"), new Service[WebSocket, WebSocket] {

      def apply(req: WebSocket): Future[WebSocket] = {
        val outgoing = new Broker[String]
        val socket = req.copy(messages = outgoing.recv)
        val msgs = req.messages

        req.messages foreach (receivedString => {
          val (cmd, args) = RedisBroker.adaptRedisCommand(receivedString)
          val resp = rb.forwardToRedis(cmd, args)
          resp flatMap {
            s =>
              {
                val str = s.toChannelBuffer.toString("UTF-8")
                println(str)
                // Enqueue
                outgoing ! str
              }
          }
        })
        Future.value(socket)
      }
    })
    Await.ready(server)
  }
}