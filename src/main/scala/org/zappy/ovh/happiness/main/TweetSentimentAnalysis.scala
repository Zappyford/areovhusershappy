package org.zappy.ovh.happiness.main

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import org.apache.log4j.Logger
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.zappy.ovh.happiness.mllib.MLlibSentimentAnalyzer
import org.zappy.ovh.happiness.stanfordcorenlp.CoreNLPSentimentAnalyzer
import org.zappy.ovh.happiness.utils.{FileUtils, PropertiesLoader, SparkUtils}
import twitter4j.conf.ConfigurationBuilder
import twitter4j.{Query, TwitterFactory}

import scala.collection.JavaConversions._

object TweetSentimentAnalysis extends App {

  val logger = Logger.getLogger(TweetSentimentAnalysis.getClass)
  logger.info("Are OVH users happy ? Let's find out !")

  val today = Calendar.getInstance.getTimeInMillis
  //Number of milliseconds in a week
  val oneWeekMilli = 604800000
  //Calculate the date one week ago
  val oneWeekAgo = today - oneWeekMilli
  val dateOneWeekAgo = new SimpleDateFormat("yyyy-MM-dd").format(new Date(oneWeekAgo))

  //With Twitter4J, we call the Twitter API
  logger.info("Setting up the Twitter wrapper...")
  val cb = new ConfigurationBuilder()
  cb.setDebugEnabled(PropertiesLoader.debugTwitter4j)
    .setOAuthConsumerKey(PropertiesLoader.consumerKey)
    .setOAuthConsumerSecret(PropertiesLoader.consumerSecret)
    .setOAuthAccessToken(PropertiesLoader.accessToken)
    .setOAuthAccessTokenSecret(PropertiesLoader.accessTokenSecret)
  val twitter = new TwitterFactory(cb.build()).getInstance()

  logger.info("Setting up the Spark environment...")
  //Get the Spark environment in order to call the ML model
  val sc = SparkUtils.createSparkContext()
  //Load Naive Bayes Model from the path specified inb the configuration file
  val naiveBayesModel = NaiveBayesModel.load(sc, PropertiesLoader.naiveBayesModelPath)
  //Load the stop words list (one for each languages)
  val stopWordsList = sc.broadcast(FileUtils.loadFile(PropertiesLoader.englishStopWords).toList)

  //We search all the tweets mentioning any ovh support account
  logger.info("Searching for the tweets about @ovh_support_ :")
  val query = new Query("ovh_support_")
  query.setSince(dateOneWeekAgo)
  val tweets = twitter.search(query).getTweets
  logger.info(tweets.size() + " tweets were find !")

  //Show numbers of tweets by country for the current day
  val tweetsToday = tweets.filter(_.getCreatedAt.toString <= Calendar.getInstance().getTime.toString)
  logger.info("For today, we have : ")
  tweetsToday.groupBy(_.getLang).foreach(
    e =>
      logger.info("For the language : " + e._1 + " we have " + e._2.length + " tweets !")
  )

  //Show sentiment for the day
  logger.info("The sentiments for today are :")
  tweetsToday.foreach(
    e =>
      if (e.getLang == "en") {
        val sentiment = CoreNLPSentimentAnalyzer.computeSentiment(e.getText)
        logger.info("The tweet's content : " + e.getText)
        logger.info("The sentiment found : " + sentiment)
        logger.info("The sentiment found : " + MLlibSentimentAnalyzer.computeSentiment(e.getText, stopWordsList, naiveBayesModel ))
      }
  )

  //Show numbers of tweets by country for the week and show the tendency
  var weekSentimentScore = 0
  var weekSentiment = ""
  tweets
      .filter(_.getLang == "en")
    .map(
      e => CoreNLPSentimentAnalyzer.computeSentiment(e.getText)
    )
    .groupBy(e => e)
    .map(e => (e._1, e._2.length))
    .foreach(
      e =>
        if (e._2 > weekSentimentScore) {
          weekSentimentScore = e._2
          weekSentiment = e._1
        }
    )
  logger.info("The tendency for the week is : " + weekSentiment)
  logger.info("End of search and analysis")
}
