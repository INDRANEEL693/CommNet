package com.example.commnet.activities;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public String name;
    public String roll; // for user nodes
    public boolean isUser;
    public List<Node> children = new ArrayList<>();

    // For user node with name + roll
    public Node(String name, String roll, boolean isUser) {
        this.name = name;
        this.roll = roll;
        this.isUser = isUser;
    }

    // For category node
    public Node(String name, boolean isUser) {
        this.name = name;
        this.roll = null;
        this.isUser = isUser;
    }
}
