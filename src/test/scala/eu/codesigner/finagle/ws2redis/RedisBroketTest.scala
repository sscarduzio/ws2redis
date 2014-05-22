package eu.codesigner.finagle.ws2redis
import org.scalatest._

class RedisBrokerTest extends FlatSpec with Matchers {

  "RedisBroker" should "recognize 1-arity commands " in {
    val (cmd, args) = RedisBroker.adaptRedisCommand("INFO")
    cmd should be("INFO")
    args should be(None)
  }
  
  it should "recognize 2-arity commands " in {
    val (cmd, args) = RedisBroker.adaptRedisCommand("GET myKey")
    cmd should be("GET")
    args should be(List("myKey".getBytes()))
  }

  it should "recognize N-arity commands " in {
    val (cmd, args) = RedisBroker.adaptRedisCommand("ZRANGE myZset 0 -1")
        cmd should be("ZRANGE")
        args should be(List("myZset".getBytes(),"0".getBytes(), "-1".getBytes() ))
  }

  it should "throw Exception if an empty string is popped" in {
    a[Exception] should be thrownBy {
      RedisBroker.adaptRedisCommand("")
    }
  }
}