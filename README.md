# Distributed Key-Value store

### System requirement
    Java >=8
    Maven >=3.5.4
    
### Steps to run
    git clone git@github.com:amityadav9314/distributed-kv.git
    mv distributed-kv /tmp
    cd /tmp/distributed-kv
    mvn clean install
    ./demo_kv.sh
    
### After following above steps following node must be available up and running
    127.0.0.1:8080
    127.0.0.1:8081
    127.0.0.1:8082
    127.0.0.1:8083
    127.0.0.1:8084
    
### To add a new key (You can use any node)
    curl --location --request POST '127.0.0.1:8080/kv/best_movie' \
    --header 'Content-Type: text/plain' \
    --data-raw 'Lord of the rings'
    
### To get value of a key (You can use any node)
    curl --location --request GET '127.0.0.1:8080/kv/best_movie'
    
### To see all data on a node
    http://localhost:8081/kv/all-data
    
### To see what all nodes a particular node recognize
    http://localhost:8084/all-nodes
    

