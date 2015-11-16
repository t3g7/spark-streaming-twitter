# Spark Streaming test

Import Twitter API credentials into ```src/main/resources```

Create the JAR with ```sbt assembly```, which is now located in ```spark-streaming-test/target/scala-2.11/spark-streaming-test-assembly-1.0.jar```.

From the ```$SPARK_HOME``` folder, run the following:


    ./bin/spark-submit --class Test $PATH_TO_JAR/spark-streaming-test-assembly-1.0.jar
