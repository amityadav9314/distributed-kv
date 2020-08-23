package com.kv.distributedkv.hash;

import com.kv.distributedkv.dtos.ServicePhysicalNode;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHash {
    private final SortedMap<Long, ServicePhysicalNode> ring = new TreeMap<>();
    private final HashFunction hashFunction;

    public ConsistentHash(Collection<ServicePhysicalNode> pNodes) {
        this(pNodes, new MD5HashFunction());
    }

    public ConsistentHash(Collection<ServicePhysicalNode> pNodes, HashFunction hashFunction) {
        if (hashFunction == null) {
            throw new RuntimeException("HashFunction is null.");
        }
        this.hashFunction = hashFunction;
        if (pNodes != null) {
            for (ServicePhysicalNode node : pNodes) {
                addNode(node);
            }
        }
    }

    public void addNode(ServicePhysicalNode node) {
        ring.put(hashFunction.hash(node.getKey()), node);
    }

    public void removeNode(ServicePhysicalNode node) {
        Iterator<Long> iterator = ring.keySet().iterator();
        while (iterator.hasNext()) {
            Long key = iterator.next();
            ServicePhysicalNode currentNode = ring.get(key);
            if (currentNode.getKey().equalsIgnoreCase(node.getKey())) {
                iterator.remove();
                break;
            }
        }
        throw new RuntimeException("Node not found to remove");
    }

    public ServicePhysicalNode routeNode(String key) {
        if (ring.isEmpty()) {
            throw new RuntimeException("Ring is empty");
        }

        long hashValue = hashFunction.hash(key);
        SortedMap<Long, ServicePhysicalNode> tailMap = ring.tailMap(hashValue);
        long nodeHashValue = !tailMap.isEmpty() ? tailMap.firstKey() : ring.firstKey();
        return ring.get(nodeHashValue);
    }
}
