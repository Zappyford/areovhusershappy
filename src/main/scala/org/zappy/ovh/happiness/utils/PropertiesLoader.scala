package org.zappy.ovh.happiness.utils

import com.typesafe.config.{Config, ConfigFactory}

/**
  * Exposes all the key-value pairs as properties object using Config object of Typesafe Config project.
  */
object PropertiesLoader {
  private val conf: Config = ConfigFactory.load("application.conf")

  val sentiment140URL: String= conf.getString("SENTIMENT140_DATA_URL")
  val sentiment140Name= conf.getString("SENTIMENT140_NAME")
  val sentiment140FilePath= conf.getString("SENTIMENT140_DATA_FILE_PATH")
  val sentiment140TrainingFilePath = conf.getString("SENTIMENT140_TRAIN_DATA_ABSOLUTE_PATH")
  val sentiment140TestingFilePath = conf.getString("SENTIMENT140_TEST_DATA_ABSOLUTE_PATH")

  val englishStopWords = conf.getString("ENGLISH_STOPWORDS")
  val frenchStopWords = conf.getString("FRENCH_STOPWORDS")
  val spanishStopWords = conf.getString("SPANISH_STOPWORDS")
  val germanStopWords = conf.getString("GERMAN_STOPWORDS")

  val naiveBayesModelPath = conf.getString("NAIVEBAYES_MODEL_ABSOLUTE_PATH")
  val modelAccuracyPath = conf.getString("NAIVEBAYES_MODEL_ACCURACY_ABSOLUTE_PATH ")

  val tweetsRawPath = conf.getString("TWEETS_RAW_ABSOLUTE_PATH")
  val saveRawTweets = conf.getBoolean("SAVE_RAW_TWEETS")

  val tweetsClassifiedPath = conf.getString("TWEETS_CLASSIFIED_ABSOLUTE_PATH")

  val debugTwitter4j = conf.getBoolean("DEBUG_TWITTER4J")
  val consumerKey = conf.getString("CONSUMER_KEY")
  val consumerSecret = conf.getString("CONSUMER_SECRET")
  val accessToken = conf.getString("ACCESS_TOKEN_KEY")
  val accessTokenSecret = conf.getString("ACCESS_TOKEN_SECRET")
}
