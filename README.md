# Spark Streaming project test

Simple program to filter lines containing the word *error* from localhost server running at 7777, and print them.

Create the JAR with ```sbt assembly```, which is now located in ```spark-streaming-test/target/scala-2.11/spark-streaming-test-assembly-1.0.jar```.

Run ```nc -lk 7777``` in a terminal. In another terminal, from ```$SPARK_HOME``` folder, run the following:

    ./bin/spark-submit --class Test $PATH_TO_JAR/spark-streaming-test-assembly-1.0.jar
