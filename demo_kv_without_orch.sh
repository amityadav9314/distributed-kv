#!/bin/sh
mvn clean install
echo "I am assuming that ORCHESTRATOR is running on port 8080. If not then either run it or use other script with name demo_kv.sh"
echo "Stopping server..."
kill -9 $(lsof -ti:8081)
kill -9 $(lsof -ti:8082)
kill -9 $(lsof -ti:8083)
kill -9 $(lsof -ti:8084)
echo "\t Servers stopped."

echo "Deploying peer nodes one by one..."
echo "Deploying 8081..."
java -jar -Dserver.port=8081 -Dorchestrator=127.0.0.1:8080 ~/.m2/repository/com/kv/distributed-kv/0.0.1-SNAPSHOT/distributed-kv-0.0.1-SNAPSHOT.jar >/tmp/distributed_kv_8081.log 2>&1 &
echo "\t Deploying done of 8081"

echo "Deploying 8082..."
java -jar -Dserver.port=8082 -Dorchestrator=127.0.0.1:8080 ~/.m2/repository/com/kv/distributed-kv/0.0.1-SNAPSHOT/distributed-kv-0.0.1-SNAPSHOT.jar >/tmp/distributed_kv_8082.log 2>&1 &
echo "\t Deploying done of 8082"

echo "Deploying 8083..."
java -jar -Dserver.port=8083 -Dorchestrator=127.0.0.1:8080 ~/.m2/repository/com/kv/distributed-kv/0.0.1-SNAPSHOT/distributed-kv-0.0.1-SNAPSHOT.jar >/tmp/distributed_kv_8083.log 2>&1 &
echo "\t Deploying done of 8083"

echo "Deploying 8084..."
java -jar -Dserver.port=8084 -Dorchestrator=127.0.0.1:8080 ~/.m2/repository/com/kv/distributed-kv/0.0.1-SNAPSHOT/distributed-kv-0.0.1-SNAPSHOT.jar >/tmp/distributed_kv_8084.log 2>&1 &
echo "\t Deploying done of 8084"
echo "........................................"
echo "Distributed-kv is up and running now..."
