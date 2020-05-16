 YOUR_SPARK_HOME/bin/spark-submit \
 --class "SimpleApp" \
 --master local[4] \
 target/scala-2.11/spark-simple-project.jar


 spark-shell --master local[4] --jars code.jar