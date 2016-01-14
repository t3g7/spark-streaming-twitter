# Machine learning for sentiment analysis

Train a model and predict tweets sentiment with the Naive Bayes (ml.naivebayes) and Random Forest (ml.randomforest) classification algorithms.

## Train the model

Relevant data is in the folder `spark-streaming-twitter/ml` marked as `$ML_DATA_DIR`:

- `training_dataset.csv` is the training data, containing tweets and their sentiment
- `trainedModel` is the directory containing the trained model after save

Run the following command to train the model with Naive Bayes classification:

    ./bin/spark-submit --class ml.naivebayes.TrainNB $PATH_TO_JAR/spark-streaming-twitter-$VERSION.jar $ML_DATA_DIR/training_dataset.csv $ML_DATA_DIR/trainedModel
    
Run the following command to train the model with Random Forest classification:

    ./bin/spark-submit --class ml.randomforest.TrainRF $PATH_TO_JAR/spark-streaming-twitter-$VERSION.jar $ML_DATA_DIR/training_dataset.csv $ML_DATA_DIR/trainedModel
    
## Predict tweets sentiment

Based on the trained model with Naive Bayes classification, run: 

    ./bin/spark-submit --class ml.naivesbayes.PredictFromStreamNB $PATH_TO_JAR/spark-streaming-twitter-$VERSION.jar --consumerKey <consumerKey> --consumerSecret <consumerSecret> --accessToken <accessToken> --accessTokenSecret <accessTokenSecret> $ML_DATA_DIR/trainedModel
    
Based on the trained model with Random Forest classification, run: 
    
    ./bin/spark-submit --class ml.randomforest.PredictFromStreamRF $PATH_TO_JAR/spark-streaming-twitter-$VERSION.jar --consumerKey <consumerKey> --consumerSecret <consumerSecret> --accessToken <accessToken> --accessTokenSecret <accessTokenSecret> $ML_DATA_DIR/trainedModel