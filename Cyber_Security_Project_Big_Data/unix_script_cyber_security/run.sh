#!/bin/bash

input_flag=false
output_flag=false

# Get input and output HDFS dirs
while getopts "i:o:" option; do
  case $option in
    i)
       input_dir=$OPTARG
       input_flag=true
       echo -e "\nInput Dir = $input_dir\n" 
       ;;
    o)
       output_dir=$OPTARG
       output_flag=true
       echo -e "\nOutput Dir = $output_dir\n" 
       ;;
    *)
       echo "Usage:"
       echo "  run.sh -i <hdfs_input_dir> -o <hdfs_output_dir "
       exit 1
       ;;
  esac
done

if [[ $input_flag != true ]]; then
  echo "Usage:"
  echo "  run.sh -i <hdfs_input_dir> -o <hdfs_output_dir "
  exit 1
fi

if [[ $output_flag != true ]]; then
  echo "Usage:"
  echo "  run.sh -i <hdfs_input_dir> -o <hdfs_output_dir "
  exit 1
fi

hadoop fs -ls $input_dir
rc=$?;
if [[ $rc != 0 ]]; then
  echo -e "\nHDFS Input Dir not found : $input_dir"; 
  echo "Exiting .."
  exit;
else
  echo -e "HDFS Input Dir found\n"
fi

hadoop fs -ls $output_dir
rc=$?;
if [[ $rc != 0 ]]; then
  echo -e "\nHDFS Output Dir not found : $output_dir"; 
  echo "Exiting .."
  exit;
else
  echo -e "HDFS Output Dir found\n"
fi

# Step 1 : Build Dictionary using articles"
dictionary_dir=$output_dir"/dictionary"
echo "Dictionary dir = $dictionary_dir"
hadoop fs -rm -r -skipTrash $dictionary_dir

ls Dictionary.jar
rc=$?;
if [[ $rc != 0 ]]; then
  echo -e "\nDictionary.jar not found.";
  echo "Exiting .."
  exit;
fi

hadoop jar Dictionary.jar non_trivial_word_count.non_trivial_word_count.WordCount $input_dir $dictionary_dir
rc=$?;
if [[ $rc != 0 ]]; then
  echo -e "\nFailed to create dictionary";
  echo "Exiting .."
  exit;
fi

# Step 2
dict_dir_local="dictionary"
vector_dir_local="vectors"
news_article_dir_local="articles"

rm -rf $dict_dir_local
rm -rf $vector_dir_local
rm -rf $news_article_dir_local

mkdir -p $dict_dir_local
mkdir -p $vector_dir_local

rc=$?;
if [[ $rc != 0 ]]; then
  echo -e "\nFailed to make local dir $vector_dir_local";
  echo "Exiting .."
  exit;
fi

hadoop fs -get $input_dir $news_article_dir_local
rc=$?;
if [[ $rc != 0 ]]; then
  echo -e "\nFailed to get files from HDFS : $input_dir.";
  echo "Exiting .."
  exit;
fi

hadoop fs -get $dictionary_dir/part-* $dict_dir_local
rc=$?;
if [[ $rc != 0 ]]; then
  echo -e "\nFailed to get files from HDFS : $dictionary_dir.";
  echo "Exiting .."
  exit;
fi

ls readingArticle.class
rc=$?;
if [[ $rc != 0 ]]; then
  echo -e "\nFailed to access file readingArticle.class.";
  echo "Exiting .."
  exit;
fi

java readingArticle $news_article_dir_local $vector_dir_local $dict_dir_local/part-*
rc=$?;
if [[ $rc != 0 ]]; then
  echo -e "\nFailed to read articles and generate vectors.";
  echo "Exiting .."
  exit;
fi

article_vectors_dir=$output_dir"/vectors"
hadoop fs -rm -r -skipTrash $article_vectors_dir

untagged_data_dir=$article_vectors_dir"/test"      # /user/gxj150630/cyberattack/vectors/test
hadoop fs -mkdir -p $untagged_data_dir
rc=$?;
if [[ $rc != 0 ]]; then
  echo -e "\nFailed to create HDFS dir : $untagged_data_dir.";
  echo "Exiting .."
  exit;
fi

untagged_vectors_csv=$untagged_data_dir"/untagged_vector.csv"

hadoop fs -put $vector_dir_local/test/data $untagged_vectors_csv
rc=$?;
if [[ $rc != 0 ]]; then
  echo -e "\nFailed to copy file to HDFS : $untagged_vectors_csv.";
  echo "Exiting .."
  exit;
fi

hadoop fs -put $vector_dir_local/training/naivedata $output_dir
nb_train_csv=$output_dir"/naivedata"

# Step 3 : Use SPARK ML Lib to train model and classify articles
nb_prediction_dir=$output_dir"/prediction/nb" # /user/gxj150630/cyberattack/prediction/nb

hadoop fs -rm -r -skipTrash $nb_prediction_dir

# ls sony_nb_training.csv
# rc=$?;
# if [[ $rc != 0 ]]; then
  # echo -e "\nTraining CSV not found";
  # echo "Exiting .."
  # exit;
# fi

# hadoop fs -put sony_nb_training.csv $output_dir

export NB_TRAIN_CSV=$nb_train_csv
export UNTAGGED_CSV=$untagged_vectors_csv
export NB_PREDICTION_DIR=$nb_prediction_dir

spark-shell -i env_nb_train_predict_articleid.scala --driver-memory 16G --executor-memory 16G

rc=$?;
if [[ $rc != 0 ]]; then
  echo -e "\nFailed to run Scala code";
  echo "Exiting .."
  exit;
fi



# Step 4 : Count cyber attack related articles by day. Report number of articles and stock price by date
ls CountArticles.class
rc=$?;
if [[ $rc != 0 ]]; then
  echo -e "\nCountArticles.class not found in current directory.";
  echo "Exiting .."
  exit;
fi

nb_prediction_dir_local="nb_prediction"
rm $nb_prediction_dir_local/part*
rm $nb_prediction_dir_local/output/*
mkdir -p $nb_prediction_dir_local/output

rc=$?;
if [[ $rc != 0 ]]; then
  echo -e "\nFailed to create $nb_prediction_dir_local.";
  echo "Exiting .."
  exit;
fi

hadoop fs -get $nb_prediction_dir/part-* $nb_prediction_dir_local
rc=$?;
if [[ $rc != 0 ]]; then
  echo -e "\nFailed to get files from HDFS : $nb_prediction_dir/part-*.";
  echo "Exiting .."
  exit;
fi

java CountArticles $nb_prediction_dir_local $nb_prediction_dir_local/output/ stock_price.csv
rc=$?;
if [[ $rc != 0 ]]; then
  echo -e "\nFailed to count articles.";
  echo "Exiting .."
  exit;
fi

: <<'END'
END

echo "Success"
 
