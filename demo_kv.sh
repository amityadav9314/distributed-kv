#!/bin/sh
mvn clean install
echo "Stopping server..."
kill -9 $(lsof -ti:8080)
kill -9 $(lsof -ti:8081)
kill -9 $(lsof -ti:8082)
kill -9 $(lsof -ti:8083)
kill -9 $(lsof -ti:8084)
echo "\t Servers stopped."

echo "Deploying ORCHESTRATOR on port 8080..."
java -jar -Dserver.port=8080 ~/.m2/repository/com/kv/distributed-kv/0.0.1-SNAPSHOT/distributed-kv-0.0.1-SNAPSHOT.jar >/tmp/distributed_kv_8080.log 2>&1 &
sleep 5
echo "\t ORCHESTRATOR is successfully deployed."

echo "Now deploying peer nodes one by one..."
echo "Redeploying 8081..."
java -jar -Dserver.port=8081 -Dorchestrator=127.0.0.1:8080 ~/.m2/repository/com/kv/distributed-kv/0.0.1-SNAPSHOT/distributed-kv-0.0.1-SNAPSHOT.jar >/tmp/distributed_kv_8081.log 2>&1 &
echo "\t Redeploying done of 8081"

echo "Redeploying 8082..."
java -jar -Dserver.port=8082 -Dorchestrator=127.0.0.1:8080 ~/.m2/repository/com/kv/distributed-kv/0.0.1-SNAPSHOT/distributed-kv-0.0.1-SNAPSHOT.jar >/tmp/distributed_kv_8082.log 2>&1 &
echo "\t Redeploying done of 8082"

echo "Redeploying 8083..."
java -jar -Dserver.port=8083 -Dorchestrator=127.0.0.1:8080 ~/.m2/repository/com/kv/distributed-kv/0.0.1-SNAPSHOT/distributed-kv-0.0.1-SNAPSHOT.jar >/tmp/distributed_kv_8083.log 2>&1 &
echo "\t Redploying done of 8083"

echo "Redeploying 8084..."
java -jar -Dserver.port=8084 -Dorchestrator=127.0.0.1:8080 ~/.m2/repository/com/kv/distributed-kv/0.0.1-SNAPSHOT/distributed-kv-0.0.1-SNAPSHOT.jar >/tmp/distributed_kv_8084.log 2>&1 &
echo "\t Redeploying done of 8084"
echo "........................................"
echo "Distributed-kv is up and running now..."
echo "........................................"
echo "Inserting few dummy keys for you to check"
curl --location --request POST '127.0.0.1:8080/kv/best_movie' --header 'Content-Type: text/plain' --data-raw 'Lord of the rings'
echo "\n"
curl --location --request POST '127.0.0.1:8080/kv/best_year' --header 'Content-Type: text/plain' --data-raw '2019'
echo "\n"
curl --location --request POST '127.0.0.1:8080/kv/worst_year' --header 'Content-Type: text/plain' --data-raw '2020'
echo "\n"
curl --location --request POST '127.0.0.1:8080/kv/name' --header 'Content-Type: text/plain' --data-raw 'Amit Yadav'
echo "\n"
echo "Insertion done...Keys inserted are: best_movie, best_year, worst_year, name"
echo "Now read README.md file so that you can get more idea how to see these values"
