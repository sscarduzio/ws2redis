# ws2redis 
Connect to Redis from anywhere via websocket
## Features
* Connect to Redis from [web browsers](http://caniuse.com/websockets)
* Connect to Redis from any client or server, no need for specific libraries
* Passes through proxies and firewalls like web traffic
* Written in Scala on top of [Twitter's Finagle](http://twitter.github.io/finagle/guide/) high performance framework.

## Build
Use brew on Mac or apt-get on Linux to install dependencies
```
$ brew install scala
$ brew install maven
```
Clone and build the latest version from Github
```
$ git clone https://github.com/sscarduzio/ws2redis.git
$ cd ws2redis
$ mvn clean package
```
## Usage
Review the configuration file (it's a simple java properties file).

By default ws2redis will listen on port 8080 for websocket connections and look for redis on port 6379.
```
$ cat ws2redis.conf
listenAddress="127.0.0.1:8080"
redisAddress="127.0.0.1:6379"
```
Run it!
```
$ scala -cp target/ws2redis-0.0.1.jar:target/lib/* eu.codesigner.finagle.ws2redis.Ws2redis

May 22, 2014 10:53:20 PM com.twitter.finagle.Init$ apply
INFO: Finagle version 6.14.0 (rev=3c3e9b0370f67cb739feca51469dc20eb35aab67) built at 20140417-145826
```


