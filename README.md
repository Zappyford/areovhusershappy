# Are OVH Users happy ?
This project was developped as part of a technical test for the company OVH.

The goal is to analyze the sentiment of tweets from all ovh support's accounts.
I use the StanfordNlp framework to compare the results with the generated Naives Bayes model.

It is built in Scala with the Spark framework more precisely the Spark ML library.

It was tested on CentOS 7 environment.

# Requirements
You need to have :
- sbt
- an HDFS cluster with YARN
- Spark
installed.

# How to use
Just launch the bootstrap.sh script !
You have two options for the script :
- `./bootstrap.sh all` will build the sbt project, unzip it, and then create the ML model and finally launch the program
- `./bootstrap.sh run` will only run the program

The results as long as the tweets can be found in the `sentiment_results_AAAA-MM-DD.txt`
You may encouter an issue with twitter4J, it happens and there's no fix. You just have to launch the application again :/


This project is largely inspired by this project : https://github.com/P7h/Spark-MLlib-Twitter-Sentiment-Analysis
The English corpus come from the Sentiment140 project.
