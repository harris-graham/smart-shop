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
@Table(name = "customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustID")
    private Integer custid;

    @Column(name = "CustName", nullable = false, length = 60)
    private String custname;

    @Column(name = "CustPNum", length = 20)
    private String custpnum;

    @Column(name = "CustAddress", length = 100)
    private String custaddress;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @JsonIgnore
    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "theme", length = 10)
    private String theme = "light";

    @Transient
    private int salesum;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Customer(int custid, String custname, String custpnum, String custaddress, int salesum) {
        this.custid = custid;
        this.custname = custname;
        this.custpnum = custpnum;
        this.custaddress = custaddress;
        this.salesum = salesum;
    }

    public Customer(int custid, String custname, String custpnum, String custaddress) {
        this.custid = custid;
        this.custname = custname;
        this.custpnum = custpnum;
        this.custaddress = custaddress;
    }

    public Customer(String custname, String custpnum, String custaddress) {
        this.custname = custname;
        this.custpnum = custpnum;
        this.custaddress = custaddress;
    }
}
