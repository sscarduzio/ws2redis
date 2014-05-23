package eu.codesigner.finagle.ws2redis

import java.io.File

import com.twitter.finagle.HttpWebSocket
import com.twitter.util.Await
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
    val conf = ConfigFactory.parseFile(new File("ws2redis.conf"))

    val server = HttpWebSocket.serve(conf.getString("listenAddress"), new Ws2redisService(conf))
    Await.ready(server)
  }

}