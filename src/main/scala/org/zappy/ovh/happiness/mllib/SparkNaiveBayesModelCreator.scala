package org.zappy.ovh.happiness.mllib

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.sql.{Row, SparkSession, _}
import org.zappy.ovh.happiness.utils.{FileUtils, PropertiesLoader, SparkUtils}

import scala.io.Source

/**
  * Creates a Model of the training dataset using Spark MLlib's Naive Bayes classifier.
  */
// spark-submit --class "org.p7h.spark.sentiment.mllib.SparkNaiveBayesModelCreator" --master spark://spark:7077 spark-streaming-corenp-mllib-tweet-sentiment-assembly-0.1.jar
object SparkNaiveBayesModelCreator {


  def main(args: Array[String]) {
    val sc = SparkUtils.createSparkContext()
    sc.setLogLevel(Level.WARN.toString)

    val logger: Logger = Logger.getLogger(SparkNaiveBayesModelCreator.getClass)
    val spark = SparkUtils.createSparkSession()

    val stopWordsList = sc.broadcast(FileUtils.loadFile(PropertiesLoader.englishStopWords).toList)

    logger.info("Importing the Sentiment140 file...")
    importSentiment140Files()

    logger.info("Creating the Naive Bayes Model...")
    createAndSaveNBModel(sc, spark, stopWordsList)

    val accuracy = validateAccuracyOfNBModel(sc, spark, stopWordsList)
    logger.info("Prediction accuracy compared to actual: " + accuracy)
  }

  /**
    * Creates a Naive Bayes Model of Tweet and its Sentiment from the Sentiment140 file.
    *
    * @param sc            -- Spark Context.
    * @param spark         -- Spark Session
    * @param stopWordsList -- Broadcast variable for list of stop words to be removed from the tweets.
    */
  def createAndSaveNBModel(sc: SparkContext, spark: SparkSession, stopWordsList: Broadcast[List[String]]): Unit = {
    val tweetsDF: DataFrame = loadSentiment140File(spark, PropertiesLoader.sentiment140TrainingFilePath).cache()
    val labeledRDD = tweetsDF.select("polarity", "status").rdd.map {
      case Row(polarity: String, tweet: String) =>
        val tweetInWords: Seq[String] = MLlibSentimentAnalyzer.getBarebonesTweetText(tweet, stopWordsList.value)
        LabeledPoint(polarity.toDouble, MLlibSentimentAnalyzer.transformFeatures(tweetInWords))
    }
    labeledRDD.cache()

    val naiveBayesModel: NaiveBayesModel = NaiveBayes.train(labeledRDD, lambda = 1.0, modelType = "multinomial")
    naiveBayesModel.save(sc, PropertiesLoader.naiveBayesModelPath)
  }

  /**
    * Validates and check the accuracy of the model
    * by comparing the polarity of a tweet from the
    * dataset and compares it with the MLlib predicted polarity.
    * @param sc            -- Spark Context.
    * @param spark         -- Spark Session
    * @param stopWordsList -- Broadcast variable for list of stop words to be removed from the tweets.
    * @return the accuracy
    */
  def validateAccuracyOfNBModel(sc: SparkContext, spark: SparkSession, stopWordsList: Broadcast[List[String]]): Double = {
    val naiveBayesModel: NaiveBayesModel = NaiveBayesModel.load(sc, PropertiesLoader.naiveBayesModelPath)
    val tweetsDF: DataFrame = loadSentiment140File(spark, PropertiesLoader.sentiment140TestingFilePath).cache()
    import spark.implicits._
    val actualVsPrediction = tweetsDF.select("polarity", "status").map {
      case Row(polarity: String, tweet: String) =>
        val tweetText = replaceNewLines(tweet)
        val tweetInWords: Seq[String] = MLlibSentimentAnalyzer.getBarebonesTweetText(tweetText, stopWordsList.value)
        (polarity.toDouble,
          naiveBayesModel.predict(MLlibSentimentAnalyzer.transformFeatures(tweetInWords)),
          tweetText)
    }
    100.0 * actualVsPrediction.filter(x => x._1 == x._2).count() / tweetsDF.count()
  }

  /**
    * Remove new line characters.
    *
    * @param tweetText -- Complete text of a tweet.
    * @return String with new lines removed.
    */
  def replaceNewLines(tweetText: String): String = {
    tweetText.replaceAll("\n", "")
  }

  /**
    * Loads the Sentiment140 file from the specified path using SparkSession.
    *
    * @param spark                -- Spark Session.
    * @param sentiment140FilePath -- Absolute file path of Sentiment140.
    * @return -- Spark DataFrame of the Sentiment file with the tweet text and its polarity.
    */
  def loadSentiment140File(spark: SparkSession, sentiment140FilePath: String): DataFrame = {
    //load the training data to DF
    import spark.implicits._
    Source.fromFile(sentiment140FilePath)("ISO-8859-1").getLines().toSeq.map(s => s.split(",")).map(line => (line(0).replace("\"", ""), line(5).replace("\"", ""))).toDF("polarity", "status")
  }

  /**
    * Imports the sentiment140 data on filesystem
    */
  def importSentiment140Files(): Unit = {
    FileUtils.fileDownloader(PropertiesLoader.sentiment140URL, PropertiesLoader.sentiment140FilePath + PropertiesLoader.sentiment140Name)
    FileUtils.fileUnzipper(PropertiesLoader.sentiment140FilePath + PropertiesLoader.sentiment140Name, PropertiesLoader.sentiment140FilePath)
  }
}