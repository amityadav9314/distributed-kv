package com.kv.distributedkv.dtos;

import java.util.ArrayList;
import java.util.List;

public class AvailableNodes {
    private List<ServicePhysicalNode> allNodes = new ArrayList<>();

    public List<ServicePhysicalNode> getAllNodes() {
        return allNodes;
    }

    public void setAllNodes(List<ServicePhysicalNode> allNodes) {
        this.allNodes = allNodes;
    }
}