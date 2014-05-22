package eu.codesigner.finagle.ws2redis

import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.Service
import com.twitter.finagle.redis.protocol.{ Reply, Command, Commands }
import com.twitter.finagle.redis.Redis
import java.net.InetSocketAddress
import com.twitter.finagle.redis.ClientError
import com.twitter.finagle.redis.protocol.ErrorReply
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


class RedisBroker(conf: Config) {

  // Client connection towards Redis server, no lazy init!
  var client: Service[Command, Reply] = ClientBuilder()
    .codec(Redis())
    .hosts(conf.getString("redisAddress"))
    .hostConnectionLimit(1)
    .build()

  def forwardToRedis(cmd: String, args: Option[List[Array[Byte]]]): Future[Reply] = {
    // #TODO? Not sure if it's a good idea to rebuild the Redis protocol Reply into to a JSON object or array.
    try {
      val rcmd = Commands.doMatch(cmd, args.getOrElse(null))
      client(rcmd)
    } catch {
      case e: ClientError => Future(ErrorReply(e.message))
    }
  }
}

object RedisBroker {
  
  // Finagle redis wants the command and the args in a certain way..
  def adaptRedisCommand(_s: String): (String, Option[List[Array[Byte]]]) = {
    val s = _s.stripLineEnd
    if (s == null || s.isEmpty()) {
      throw new Exception("null/empty string provided")
    }

    // Arity = 1
    if (!s.contains(" ")) {
      return (s, None)
    }

    // Arity > 1 
    val splitted = s.split(' ')
    val cmd = splitted(0)

    if (splitted.length == 1) {
      return (cmd, None)
    }

    return (cmd, Some(splitted.takeRight(splitted.length - 1).map(_.map(_.toByte).toArray[Byte]).toList))
  }

}