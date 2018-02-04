#!/bin/bash
export SPARK_MAJOR_VERSION=2
spark-submit --master yarn --num-executors 2 --driver-memory 4g --class org.zappy.ovh.happiness.mllib.SparkNaiveBayesModelCreator lib/isovhsupporthappy-assembly-1.0.jar
