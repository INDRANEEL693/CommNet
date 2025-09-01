package com.example.commnet.models;

public class User {
    private String id;
    private String name;
    private String category;
    private boolean isSelected;

    public User() {} // Needed for Firebase

    public User(String id, String name, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.isSelected = false;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public boolean isSelected() { return isSelected; }

    public void setSelected(boolean selected) { isSelected = selected; }
}
