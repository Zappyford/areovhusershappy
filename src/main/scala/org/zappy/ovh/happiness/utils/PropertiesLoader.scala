package org.zappy.ovh.happiness.utils

import com.typesafe.config.{Config, ConfigFactory}

/**
  * Exposes all the key-value pairs as properties object using Config object of Typesafe Config project.
  */
object PropertiesLoader {
  private val conf: Config = ConfigFactory.load("application.conf")

  val sentiment140URL: String= conf.getString("SENTIMENT140_DATA_URL")
  val sentiment140Name: String= conf.getString("SENTIMENT140_NAME")
  val sentiment140FilePath: String= conf.getString("SENTIMENT140_DATA_FILE_PATH")
  val sentiment140TrainingFilePath: String = conf.getString("SENTIMENT140_TRAIN_DATA_ABSOLUTE_PATH")
  val sentiment140TestingFilePath: String = conf.getString("SENTIMENT140_TEST_DATA_ABSOLUTE_PATH")

  val englishStopWords: String = conf.getString("ENGLISH_STOPWORDS")
  val frenchStopWords: String = conf.getString("FRENCH_STOPWORDS")
  val spanishStopWords: String = conf.getString("SPANISH_STOPWORDS")
  val germanStopWords: String = conf.getString("GERMAN_STOPWORDS")

  val naiveBayesModelPath: String = conf.getString("NAIVEBAYES_MODEL_ABSOLUTE_PATH")

  val debugTwitter4j: Boolean = conf.getBoolean("DEBUG_TWITTER4J")
  val consumerKey: String = conf.getString("CONSUMER_KEY")
  val consumerSecret: String = conf.getString("CONSUMER_SECRET")
  val accessToken: String = conf.getString("ACCESS_TOKEN_KEY")
  val accessTokenSecret: String = conf.getString("ACCESS_TOKEN_SECRET")
}
