package com.careerbridge.model;
public class Employer extends User {
    public Employer(String n,String e,String p){super(n,e,p,"EMPLOYER");}
    @Override public String getDashboardType(){return "employer-dashboard";}
}
