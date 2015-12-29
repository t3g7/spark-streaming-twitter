# Machine learning for sentiment analysis

Train a model and predict tweets sentiment with the Naive Bayes classification algorithm.

## Train the model

Relevant data is in the folder `spark-streaming-twitter/ml` marked as `$ML_DATA_DIR`:

- `training_dataset.csv` is the training data, containing tweets and their sentiment
- `trainedModel` is the directory containing the trained model after save

Run the following command to train the model:

	./bin/spark-submit --class ml.Train $PATH_TO_JAR/spark-streaming-twitter-$VERSION.jar $ML_DATA_DIR/training_dataset.csv $ML_DATA_DIR/trainedModel
	
## Predict tweets sentiment

Based on the trained model, run Spark Streaming with the jar: 

	/bin/spark-submit --class ml.PredictFromStream $PATH_TO_JAR/spark-streaming-twitter-$VERSION.jar --consumerKey <consumerKey> --consumerSecret <consumerSecret> --accessToken <accessToken> --accessTokenSecret <accessTokenSecret> $ML_DATA_DIR/trainedModel