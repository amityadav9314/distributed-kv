# Distributed Key-Value store

### Idea description
This K-V store follows a partial master-slave for node discovery and peer-to-peer for getting and setting up values.

First a `orchestrator` machine is set up. This acts as master/primary node. Once `orchestrator` comes up, then we are ready to instantiate our secondary nodes.

Whenever any node is instantiated, it registers itself to `orchestrator`. Every secondary nodes takes a -VM argument for `orchestrator`.

`Orchestrator` periodically communicates every node about every other nodes in a list of all available nodes after checking health check of each and every node at that time.

There is no `server` and `client` concept in this K-V store. Each and every node acts both as a `server` and `client`.


### System requirement
    Java >=8
    Maven >=3.5.4
    
### Steps to run
    git clone git@github.com:amityadav9314/distributed-kv.git
    mv distributed-kv /tmp
    cd /tmp/distributed-kv
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
    
### To see on which nodes a key exists
    http://127.0.0.1:8080/all-nodes/my_key
    
### Things pending
    * Rebalancing
    * gRPC call instead of REST call
    * Auto-choose orchestrator (Right now orchestrator is single point of failure)
    * Implement gossip protocol for node discovery, right now orchestrator handles this thing
    * Load balancer server
    

