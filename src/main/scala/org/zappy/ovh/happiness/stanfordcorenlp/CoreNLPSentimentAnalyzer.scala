package org.zappy.ovh.happiness.stanfordcorenlp

import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import org.apache.log4j.Logger

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

object CoreNLPSentimentAnalyzer {

  lazy val pipeline: StanfordCoreNLP = {
    val props = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment")
    new StanfordCoreNLP(props)
  }

  /**
    * Predicts sentiment of the tweet text with StanfordNLP passed after removing the stop words.
    *
    * @param text          -- Complete text of a tweet.
    * @return String Sentiment of the tweet.
    */
  def computeSentiment(text: String): String = {
    val (_, sentiment) = extractSentiments(text)
      .maxBy { case (sentence, _) => sentence.length }
    sentiment
  }

  /**
    * Normalize sentiment for visualization perspective.
    * We are normalizing sentiment as we need to be consistent with the polarity value with MLlib and for visualization.
    *
    * @param sentiment polarity of the tweet
    * @return normalized to either negative, neutral or positive.
    */
  def normalizeCoreNLPSentiment(sentiment: Double): String = {
    sentiment match {
      case s if s <= 0.0 => "neutral" // neutral
      case s if s < 2.0 => "negative" // negative
      case s if s < 3.0 => "neutral" // neutral
      case s if s < 5.0 => "positive" // positive
      case _ => "neutral" // if we cant find the sentiment, we will deem it as neutral.
    }
  }

  /**
    * Extracts the sentiment from the given text
    * @param text the tweet's text
    * @return a List of String's tuple which contains the tweet's text and the sentiment
    */
  def extractSentiments(text: String): List[(String, String)] = {
    val annotation: Annotation = pipeline.process(text)
    val sentences = annotation.get(classOf[CoreAnnotations.SentencesAnnotation])
    sentences
      .map(sentence => (sentence, sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])))
      .map { case (sentence, tree) => (sentence.toString, normalizeCoreNLPSentiment(RNNCoreAnnotations.getPredictedClass(tree))) }
      .toList
  }

  /*
  def computeWeightedSentiment(tweet: String): String = {

    val annotation = pipeline.process(tweet)
    val sentiments: ListBuffer[Double] = ListBuffer()
    val sizes: ListBuffer[Int] = ListBuffer()

    for (sentence <- annotation.get(classOf[CoreAnnotations.SentencesAnnotation])) {
      val tree = sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])
      val sentiment = RNNCoreAnnotations.getPredictedClass(tree)

      sentiments += sentiment.toDouble
      sizes += sentence.toString.length
    }

    val weightedSentiment = if (sentiments.isEmpty) {
      -1
    } else {
      val weightedSentiments = (sentiments, sizes).zipped.map((sentiment, size) => sentiment * size)
      weightedSentiments.sum / sizes.sum
    }

    normalizeCoreNLPSentiment(weightedSentiment)
  }
  */
}