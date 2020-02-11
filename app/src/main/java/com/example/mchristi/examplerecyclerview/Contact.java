package com.example.mchristi.examplerecyclerview;

public class Contact {

    // this uid is required if we want to identify which contact to remove/update
    // it's value is provided by Firebase
    String uid;
    String name;
    String image;
    String phone;
    int likes;

    public Contact() {

    }

    public Contact(String name, String image, String phone) {
        this.name = name;
        this.image = image;
        this.phone = phone;
        this.likes = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
