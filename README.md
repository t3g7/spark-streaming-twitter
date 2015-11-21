# Spark Streaming Twitter App [![Build Status](https://travis-ci.org/t3g7/spark-streaming-twitter.svg)](https://travis-ci.org/t3g7/spark-streaming-twitter)

### Configuration
Import Twitter API credentials into ```src/main/resources```

### Building
Create the JAR with ```sbt assembly```, which is now located in ```target/scala-2.10/spark-streaming-twitter-assembly-$VERSION.jar```.

### Running the app
Note: a Cassandra instance must be running.
From the ```$SPARK_HOME``` folder, run the following:

    ./bin/spark-submit --class TwitterStreamingApp $PATH_TO_JAR/spark-streaming-twitter-assembly-$VERSION.jar
