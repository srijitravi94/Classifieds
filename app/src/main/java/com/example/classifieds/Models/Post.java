package com.example.classifieds.Models;

public class Post {
    private String post_id;
    private String user_id;
    private String imagePath;
    private String title;
    private String desc;
    private String price;
    private String country;
    private String state;
    private String city;
    private String email;

    public Post(String post_id, String user_id, String imagePath, String title, String desc, String price, String country, String state, String city, String email) {
        this.post_id = post_id;
        this.user_id = user_id;
        this.imagePath = imagePath;
        this.title = title;
        this.desc = desc;
        this.price = price;
        this.country = country;
        this.state = state;
        this.city = city;
        this.email = email;
    }

    public Post() {

    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Post{" +
                "post_id='" + post_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", price='" + price + '\'' +
                ", country='" + country + '\'' +
                ", state='" + state + '\'' +
                ", city='" + city + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
