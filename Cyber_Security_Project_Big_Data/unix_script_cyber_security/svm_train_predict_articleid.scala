import org.apache.spark.mllib.classification.{SVMModel, SVMWithSGD}
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.mllib.linalg.Vectors
// import org.apache.spark.mllib.regression.LabeledPoint

// Load training data in LIBSVM format.
// Label term1Index:term1Freq term2Index:term2Freq term3Index:term3Freq ..
// tagged_data is RDD of (Label, SparseVector)
val tagged_data = MLUtils.loadLibSVMFile(sc, "/Users/gjain/Documents/UTD/CS6350/project/sony_final/training_log/sony_svm_training.csv")

// Run training algorithm to build the model
val numIterations = 100
val model = SVMWithSGD.train(tagged_data, numIterations)

// Clear the default threshold.
model.clearThreshold()

val untagged_data = sc.textFile("/Users/gjain/Documents/UTD/CS6350/project/sony_final/to_be_classified/sony_to_be_classified_all.csv")

// parsedData is RDD of (DateAndArticleId, DenseVector)
val parsedData = untagged_data.map { line =>
  val parts = line.split(',')
  (parts(0), Vectors.dense(parts(1).split(' ').map(_.toDouble)))
}


// Use model to predict class
val articleAndScore = parsedData.mapValues( idfVector => model.predict(idfVector) )

// articleAndScore.foreach(println)
articleAndScore.saveAsTextFile("/Users/gjain/Documents/UTD/CS6350/project/sony_final/svm_class")

exit

