package org.zappy.ovh.happiness.main

import java.io.{BufferedWriter, File, FileWriter}
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
  logger.debug("Setting up the Twitter wrapper...")
  val cb = new ConfigurationBuilder()
  cb.setDebugEnabled(PropertiesLoader.debugTwitter4j)
    .setOAuthConsumerKey(PropertiesLoader.consumerKey)
    .setOAuthConsumerSecret(PropertiesLoader.consumerSecret)
    .setOAuthAccessToken(PropertiesLoader.accessToken)
    .setOAuthAccessTokenSecret(PropertiesLoader.accessTokenSecret)
  val twitter = new TwitterFactory(cb.build()).getInstance()

  logger.debug("Setting up the Spark environment...")
  //Get the Spark environment in order to call the ML model
  val sc = SparkUtils.createSparkContext()
  sc.setLogLevel(Level.WARN.toString)
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

  //Writing the results to file
  val timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(java.time.Instant.now().toEpochMilli)
  val file = new File("sentiment_results_"+timestamp+".txt")
  val bw = new BufferedWriter(new FileWriter(file))

  //Show numbers of tweets by country for the current day
  val tweetsToday = tweets.filter(_.getCreatedAt.toString <= Calendar.getInstance().getTime.toString)
  logger.info("For today, we have : ")
  bw.write("For today, we have : \n")
  tweetsToday.groupBy(_.getLang).foreach(
    e =>{
      logger.info("For the language : " + e._1 + " we have " + e._2.length + " tweets !")
      bw.write("For the language : " + e._1 + " we have " + e._2.length + " tweets !\n")
    }
  )

  //Show sentiment for the day
  logger.info("The sentiments for today are :")
  bw.write("The sentiments for today are :\n")
  tweetsToday.foreach(
    e =>
      if (e.getLang == "en") {
        val sentiment = CoreNLPSentimentAnalyzer.computeSentiment(e.getText)
        logger.info("The tweet's content : " + e.getText)
        bw.write("The tweet's content : " + e.getText+"\n")
        logger.info("The sentiment found by StanfordNLP : " + sentiment+"\n")
        bw.write("The sentiment found by StanfordNLP : " + sentiment+"\n")
        logger.info("The sentiment found by the Naive Bayes model : " + MLlibSentimentAnalyzer.computeSentiment(e.getText, stopWordsList, naiveBayesModel )+"\n")
        bw.write("The sentiment found by the Naive Bayes model : " + MLlibSentimentAnalyzer.computeSentiment(e.getText, stopWordsList, naiveBayesModel )+"\n")
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
  logger.info("The tendency for last week is : " + weekSentiment)
  bw.write("The tendency for last week is : " + weekSentiment+"\n")
  bw.close()
  logger.info("End of search and analysis")
}
