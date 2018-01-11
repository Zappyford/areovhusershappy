package main.scala.org.zappy.ovh.happiness

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import org.apache.log4j.{Logger, PropertyConfigurator}
import org.zappy.ovh.happiness.stanfordcorenlp.CoreNLPSentimentAnalyzer
import twitter4j.{Query, TwitterFactory}

import scala.collection.JavaConversions._

object TweetSentimentAnalysis extends App {

  val logger = Logger.getLogger(TweetSentimentAnalysis.getClass)
  PropertyConfigurator.configure("log4j.properties")
  logger.info("Is OVH support happy ? Let's find out !")
  val today = Calendar.getInstance.getTimeInMillis
  //Number of milliseconds in a week
  val oneWeekMilli = 604800000
  //Calculate the date one week ago
  val oneWeekAgo = today - oneWeekMilli
  val dateOneWeekAgo = new SimpleDateFormat("yyyy-MM-dd").format(new Date(oneWeekAgo))
  //With Twitter4J, we call the Twitter API
  /*val cb = new ConfigurationBuilder()
  cb.setDebugEnabled(false)
    .setOAuthConsumerKey("HozJ56uUlbgNuYlk9o7ZT6yev")
    .setOAuthConsumerSecret("W1doi2wKxLSYOsHIfqrTcWyyp9sPKpUsJAiqStmMv5fKdBUrv9")
    .setOAuthAccessToken("3081500387-bqweaCzXhKmDQsaOYPKUE4dZ930w0Lgo3JbzJQy")
    .setOAuthAccessTokenSecret("4v5QpSL4PYAmCsT5CQp7YUjrYR32OByy16QsHerzA71aO")
  val twitter = new TwitterFactory(cb.build()).getInstance()*/
  //We search all the tweets mentioning any ovh support account
  logger.info("Setting up the Twitter wrapper...")
  val twitter = new TwitterFactory().getInstance()
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
        logger.info("The tweet's content : " + e.getText)
        logger.info("The sentiment found : " + CoreNLPSentimentAnalyzer.computeSentiment(e.getText))
      }
  )
  //  //Show numbers of tweets by country for the week and show the tendency
  var weekSentimentScore = 0
  var weekSentiment = ""
  tweets
    .map(
      e =>
        CoreNLPSentimentAnalyzer.computeSentiment(e.getText)
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
