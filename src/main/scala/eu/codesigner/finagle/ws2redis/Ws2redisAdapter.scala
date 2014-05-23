package eu.codesigner.finagle.ws2redis

import com.twitter.finagle.Service
import com.twitter.finagle.redis.protocol.Commands
import com.twitter.finagle.redis.protocol.Command
import com.twitter.finagle.Filter
import com.twitter.finagle.redis.protocol.Reply
import com.twitter.util.Future
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import com.twitter.finagle.redis.protocol.ErrorReply
import scala.util.control.NoStackTrace

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

class Ws2redisAdapter extends Filter[String, String, Command, Reply] {

  def apply(request: String, service: Service[Command, Reply]) = {
    val (cmd, args) = Ws2redisAdapter.adaptRedisCommand(request)
    val rep = Try(service(Commands.doMatch(cmd, args))) match {
      // #TODO replies may contain binary data, should probably return bytes or JSON with base64 encoded strings?
      case Success(r) => r.flatMap(reply => Future(reply))
      case Failure(e) => Future(new ErrorReply("ERR " + e.getMessage()))
    }
    rep.flatMap(r => Future(r.toChannelBuffer.toString("UTF-8")))
  }
}

object Ws2redisAdapter {
  
  // #TODO? Probably we should support JSON Commands or maybe implement WAMP as of http://wamp.ws/spec/
  def adaptRedisCommand(_s: String): (String, List[Array[Byte]]) = {
    val s = _s.trim
    if (s == null || s.isEmpty()) {
      throw new Exception("null/empty string provided") with NoStackTrace
    }

    // Arity = 1
    if (!s.contains(" ")) {
      return (s, null)
    }

    // Arity > 1 
    val splitted = s.split(' ')
    val cmd = splitted(0)
    if (splitted.length == 1) {
      return (cmd, null)
    }
    // Arguments = the splitted string without the first token (the command)
    return (cmd, splitted.takeRight(splitted.length - 1).map(_.map(_.toByte).toArray[Byte]).toList)
  }

}