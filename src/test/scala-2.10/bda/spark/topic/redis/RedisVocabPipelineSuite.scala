package bda.spark.topic.redis

import java.util

import org.scalatest.FunSuite
import redis.clients.jedis.{HostAndPort, JedisCluster, Response}

import scala.collection.JavaConversions._
import scala.collection.immutable.HashSet
import collection.JavaConversions._

/**
  * Created by Roger on 17/3/6.
  */
class RedisVocabPipelineSuite extends FunSuite{



  test("RedisVocabPipeline") {
    val jedisNodes = HashSet[HostAndPort](new HostAndPort("bda07", 30001))
    val jedis = new JedisCluster(jedisNodes)
    val client = new RedisVocabPipeline(8L, jedis, 6000)
    client.clear()
    assert(client.vocabSize == 0)

    println("Begin test")
    val terms = Array("Roger", "Gorge", "Hinton", "Mary", "Jack")
    println("get term ids")
    var ids = client.getTermIds(terms, 0)
    assert( ids.sum == -5)
    println("add terms")
    ids = client.addTerms(terms, 0)
    client.incUseCount(terms)
    assert( ids.sum == 10)
    assert(ids(0) == 0 && ids(1) == 1)

    println(client.loadVocab.toArray.sortBy(_._2).mkString(" "))
    val terms2 = Array("Alex", "Xing", "Peter", "Mike")
    ids = client.addTerms(terms2, 1)
    client.incUseCount(terms2)
    println(client.loadVocab.toArray.sortBy(_._2).mkString(" "))
    var counts: Response[util.Map[String, String]] = client.countPipeline.hgetAll(client.countKey)
    client.countPipeline.sync()
    println(counts.get().toArray.sorted.mkString(" "))

    client.decUseCount(terms2)

    val terms3 = Array("Miller", "Tiger", "Woods", "James", "Kobe")
    client.decUseCount(terms3)
     counts = client.countPipeline.hgetAll(client.countKey)
    client.countPipeline.sync()
    println(counts.get().toArray.sorted.mkString(" "))

    ids = client.addTerms(terms3, 2)
    counts = client.countPipeline.hgetAll(client.countKey)
    client.countPipeline.sync()
    println(counts.get().toArray.sorted.mkString(" "))

 client.incUseCount(terms3)
    counts = client.countPipeline.hgetAll(client.countKey)
    client.countPipeline.sync()
    println(counts.get().toArray.sorted.mkString(" "))

    println(ids)
    println(client.loadVocab.toArray.sortBy(_._2).mkString(" "))

    client.decUseCount(terms)
    client.decUseCount(terms3)
    client.clear()

  }
}
