package org.zappy.ovh.happiness.utils

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.sql.SparkSession

object SparkUtils {

  /**
    * Create SparkContext.
    * @return SparkContext
    */
  def createSparkContext(): SparkContext = {
    val conf = new SparkConf()
      .setAppName(this.getClass.getSimpleName)
      .set("spark.serializer", classOf[KryoSerializer].getCanonicalName)
    val sc = SparkContext.getOrCreate(conf)
    sc
  }

  /**
    * Create SparkSession.
    * @return SparkSession
    */
  def createSparkSession(): SparkSession = {
    SparkSession.builder
      .appName(this.getClass.getSimpleName)
      .getOrCreate
  }
}
