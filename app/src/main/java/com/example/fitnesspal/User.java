package com.example.fitnesspal;

public class User {
    private String name;
    private int age;
    private String gender;



    public User(String name, int age, String gender, int height, int weight) {
        this.name = name;
        this.age = age;
        this.gender = gender;

    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }


}