package com.careerbridge.model;
public class Student extends User {
    public Student(String n,String e,String p){super(n,e,p,"STUDENT");}
    @Override public String getDashboardType(){return "student-dashboard";}
}
