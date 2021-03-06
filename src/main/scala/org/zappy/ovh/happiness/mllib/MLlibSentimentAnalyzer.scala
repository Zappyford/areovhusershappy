package org.zappy.ovh.happiness.mllib

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector

object MLlibSentimentAnalyzer {

  /**
    * Predicts sentiment of the tweet text with Naive Bayes model passed after removing the stop words.
    *
    * @param text          -- Complete text of a tweet.
    * @param stopWordsList -- Broadcast variable for list of stop words to be removed from the tweets.
    * @param model         -- Naive Bayes Model of the trained data.
    * @return String Sentiment of the tweet.
    */
  def computeSentiment(text: String, stopWordsList: Broadcast[List[String]], model: NaiveBayesModel): String = {
    val tweetInWords: Seq[String] = getBarebonesTweetText(text, stopWordsList.value)
    val polarity = model.predict(MLlibSentimentAnalyzer.transformFeatures(tweetInWords))
    normalizeMLlibSentiment(polarity)
  }

  /**
    * Normalize sentiment for visualization perspective.
    * We are normalizing sentiment as we need to be consistent with the polarity value with Core NLP and for visualization.
    *
    * @param sentiment polarity of the tweet
    * @return normalized to either negative, neutral or positive.
    */
  def normalizeMLlibSentiment(sentiment: Double): String = {
    sentiment match {
      case x if x == 0 => "negative" // negative
      case x if x == 2 => "neutral" // neutral
      case x if x == 4 => "positive" // positive
      case _ => "neutral" // if cant figure the sentiment, term it as neutral
    }
  }

  /**
    * Strips the extra characters in tweets. And also removes stop words from the tweet text.
    *
    * @param tweetText     -- Complete text of a tweet.
    * @param stopWordsList -- Broadcast variable for list of stop words to be removed from the tweets.
    * @return Seq[String] after removing additional characters and stop words from the tweet.
    */
  def getBarebonesTweetText(tweetText: String, stopWordsList: List[String]): Seq[String] = {
    //Remove URLs, RT, MT and other redundant chars / strings from the tweets.
    tweetText.toLowerCase()
      .replaceAll("\n", "")
      .replaceAll("rt\\s+", "")
      .replaceAll("\\s+@\\w+", "")
      .replaceAll("@\\w+", "")
      .replaceAll("\\s+#\\w+", "")
      .replaceAll("#\\w+", "")
      .replaceAll("(?:https?|http?)://[\\w/%.-]+", "")
      .replaceAll("(?:https?|http?)://[\\w/%.-]+\\s+", "")
      .replaceAll("(?:https?|http?)//[\\w/%.-]+\\s+", "")
      .replaceAll("(?:https?|http?)//[\\w/%.-]+", "")
      .split("\\W+")
      .filter(_.matches("^[a-zA-Z]+$"))
      .filter(!stopWordsList.contains(_))
    //.fold("")((a,b) => a.trim + " " + b.trim).trim
  }

  val hashingTF = new HashingTF()

  /**
    * Transforms features to Vectors.
    *
    * @param tweetText -- Complete text of a tweet.
    * @return Vector
    */
  def transformFeatures(tweetText: Seq[String]): Vector = {
    hashingTF.transform(tweetText)
  }
}