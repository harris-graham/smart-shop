package com.smartshop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sale")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SaleID")
    private Integer saleid;

    @Column(name = "SaleQty", nullable = false)
    private Integer saleqty;

    @Column(name = "SaleTotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal saletotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SmartID")
    private Smartphone smartphone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustID")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmpID")
    private Employee employee;

    @CreationTimestamp
    @Column(name = "SaleDate", updatable = false)
    private LocalDateTime saleDate;

    @Column(name = "status", length = 20)
    private String status = "COMPLETED";

    /** Order that produced this sale, when it originated from a checkout. */
    @Column(name = "OrderID")
    private Integer orderid;

    public Sale(Integer saleqty, BigDecimal saletotal, Smartphone smartphone,
                Customer customer, Employee employee) {
        this.saleqty = saleqty;
        this.saletotal = saletotal;
        this.smartphone = smartphone;
        this.customer = customer;
        this.employee = employee;
    }
}
