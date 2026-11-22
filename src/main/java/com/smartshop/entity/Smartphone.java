package com.smartshop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "smartphone")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Smartphone {

    @Id
    @Column(name = "SmartID", length = 50)
    private String smartid;

    @Column(name = "SmartName", nullable = false, length = 50)
    private String smartname;

    @Column(name = "SmartPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal smartprice = BigDecimal.ZERO;

    @Column(name = "SmartQty", nullable = false)
    private Integer smartqty = 0;

    @Column(name = "SmartRAM", nullable = false)
    private Integer smartram = 0;

    @Column(name = "SmartStorage", nullable = false)
    private Integer smartstorage = 0;

    @Column(name = "SmartImage", columnDefinition = "TEXT")
    private String smartimg;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Smartphone(String smartid, String smartname, BigDecimal smartprice, Integer smartqty,
                      Integer smartram, Integer smartstorage, String smartimg) {
        this.smartid = smartid;
        this.smartname = smartname;
        this.smartprice = smartprice;
        this.smartqty = smartqty;
        this.smartram = smartram;
        this.smartstorage = smartstorage;
        this.smartimg = smartimg;
    }

    public Smartphone(String smartname, BigDecimal smartprice, Integer smartqty,
                      Integer smartram, Integer smartstorage, String smartimg) {
        this.smartname = smartname;
        this.smartprice = smartprice;
        this.smartqty = smartqty;
        this.smartram = smartram;
        this.smartstorage = smartstorage;
        this.smartimg = smartimg;
    }

    public Smartphone(String smartid, String smartname, BigDecimal smartprice, Integer smartqty,
                      Integer smartram, Integer smartstorage) {
        this.smartid = smartid;
        this.smartname = smartname;
        this.smartprice = smartprice;
        this.smartqty = smartqty;
        this.smartram = smartram;
        this.smartstorage = smartstorage;
    }

    public Smartphone(String smartname, BigDecimal smartprice, Integer smartqty,
                      Integer smartram, Integer smartstorage) {
        this.smartname = smartname;
        this.smartprice = smartprice;
        this.smartqty = smartqty;
        this.smartram = smartram;
        this.smartstorage = smartstorage;
    }

    public Smartphone(String smartid, Integer smartqty) {
        this.smartid = smartid;
        this.smartqty = smartqty;
    }
}
