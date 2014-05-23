package eu.codesigner.finagle.ws2redis

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class Ws2redisAdapterTest extends FlatSpec with Matchers {

  "Ws2redisAdapter" should "recognize 1-arity commands " in {
    val (cmd, args) = Ws2redisAdapter.adaptRedisCommand("INFO")
    cmd should be("INFO")
    args should be(null)
  }
  
  it should "recognize 2-arity commands " in {
    val (cmd, args) = Ws2redisAdapter.adaptRedisCommand("GET myKey")
    cmd should be("GET")
    args.map(new String(_)) should be(List("myKey"))
  }

  it should "recognize N-arity commands " in {
    val (cmd, args) = Ws2redisAdapter.adaptRedisCommand("ZRANGE myZset 0 -1")
        cmd should be("ZRANGE")
        args.map(new String(_)) should be(List("myZset", "0", "-1"))
  }

  it should "throw Exception if an empty string is given" in {
    a[Exception] should be thrownBy {
      Ws2redisAdapter.adaptRedisCommand("")
    }
  }
  
  it should "throw Exception if an single space string is given" in {
    a[Exception] should be thrownBy {
      Ws2redisAdapter.adaptRedisCommand(" ")
    }
  }
}