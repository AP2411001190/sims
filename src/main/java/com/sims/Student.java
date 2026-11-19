package com.sims;

public class Student {
    private int id;
    private String regNo, name, email, phone, department;
    private int year;
    private double cgpa;

    public Student(int id, String regNo, String name, String email,
                   String phone, String department, int year, double cgpa) {
        this.id = id; this.regNo = regNo; this.name = name; this.email = email;
        this.phone = phone; this.department = department; this.year = year; this.cgpa = cgpa;
    }

    public int getId() { return id; }
    public String getRegNo() { return regNo; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDepartment() { return department; }
    public int getYear() { return year; }
    public double getCgpa() { return cgpa; }
}
