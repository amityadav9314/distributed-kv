package com.kv.distributedkv.dtos;

import java.util.ArrayList;
import java.util.List;

public class AvailableNodes {
    private List<Node> allNodes = new ArrayList<>();

    public List<Node> getAllNodes() {
        return allNodes;
    }

    public void setAllNodes(List<Node> allNodes) {
        this.allNodes = allNodes;
    }
}