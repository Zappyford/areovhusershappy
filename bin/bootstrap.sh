#!/bin/bash
if [ "$1" == "all" ]; then
  cd ..
  export SBT_OPTS="-Xms1024M -Xmx2048M -Xss1M -XX:+CMSClassUnloadingEnabled"
  sbt universal:packageBin
  cp target/universal/areovhusershappy-1.0.zip .
  unzip -o areovhusershappy-1.0.zip
  cd areovhusershappy-1.0
  export SPARK_MAJOR_VERSION=2
  spark-submit --master yarn --num-executors 2 --driver-memory 4g --class org.zappy.ovh.happiness.mllib.SparkNaiveBayesModelCreator lib/areovhusershappy-assembly-1.0.jar
  spark-submit --master yarn --class org.zappy.ovh.happiness.main.TweetSentimentAnalysis lib/areovhusershappy-assembly-1.0.jar
elif [ "$1" == "run" ]; then
  cd ../areovhusershappy-1.0
  export SPARK_MAJOR_VERSION=2
  spark-submit --master yarn --class org.zappy.ovh.happiness.main.TweetSentimentAnalysis lib/areovhusershappy-assembly-1.0.jar
fi


