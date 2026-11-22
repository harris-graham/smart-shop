package com.smartshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EmpID")
    private Integer empid;

    @Column(name = "EmpName", nullable = false, length = 60)
    private String empname;

    @Column(name = "EmpPNum", length = 20)
    private String emppnum;

    @Column(name = "EmpAddress", length = 100)
    private String empaddress;

    @Column(name = "EmpUName", nullable = false, unique = true, length = 20)
    private String empuname;

    @JsonIgnore
    @Column(name = "EmpPword", nullable = false, length = 100)
    private String emppword;

    @Column(name = "role", length = 20)
    private String role = "EMPLOYEE";

    @Column(name = "theme", length = 10)
    private String theme = "light";

    @Transient
    private int salesum;

    @Transient
    private boolean valid;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Employee(int empid, String empname, String emppnum, String empaddress,
                    String empuname, String emppword, int salesum) {
        this.empid = empid;
        this.empname = empname;
        this.emppnum = emppnum;
        this.empaddress = empaddress;
        this.empuname = empuname;
        this.emppword = emppword;
        this.salesum = salesum;
    }

    public Employee(int empid, String empname, String emppnum, String empaddress,
                    String empuname, String emppword) {
        this.empid = empid;
        this.empname = empname;
        this.emppnum = emppnum;
        this.empaddress = empaddress;
        this.empuname = empuname;
        this.emppword = emppword;
    }

    public Employee(int empid, String empname, String emppnum, String empaddress, String empuname) {
        this.empid = empid;
        this.empname = empname;
        this.emppnum = emppnum;
        this.empaddress = empaddress;
        this.empuname = empuname;
    }

    public Employee(String empname, String emppnum, String empaddress, String empuname, String emppword) {
        this.empname = empname;
        this.emppnum = emppnum;
        this.empaddress = empaddress;
        this.empuname = empuname;
        this.emppword = emppword;
    }

    public Employee(int empid, String empuname, String emppword) {
        this.empid = empid;
        this.empuname = empuname;
        this.emppword = emppword;
    }

    public Employee(int empid, String empuname) {
        this.empid = empid;
        this.empuname = empuname;
    }

    public Employee(String empuname, String emppword) {
        this.empuname = empuname;
        this.emppword = emppword;
    }
}
