import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

val tagged_data = sc.textFile(sys.env("NB_TRAIN_CSV"))
val parsed_tagged_data = tagged_data.map { line =>
  val parts = line.split(',')
  LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
}

val modelSony = NaiveBayes.train(parsed_tagged_data, lambda = 1.0, modelType = "multinomial")

val untagged_data = sc.textFile(sys.env("UNTAGGED_CSV"))

// parsed_untagged_data is RDD of (DateAndArticleId, DenseVector)
val parsed_untagged_data = untagged_data.map { line =>
  val parts = line.split(',')
  (parts(0), Vectors.dense(parts(1).split(' ').map(_.toDouble)))
}

// Use model to predict class
val articleAndScore = parsed_untagged_data.mapValues( idfVector => modelSony.predict(idfVector) )

articleAndScore.saveAsTextFile(sys.env("NB_PREDICTION_DIR"))

exit

