package com.smartshop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HistoryID")
    private Integer historyid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SmartID")
    private Smartphone smartphone;

    @Column(name = "QuantityChange", nullable = false)
    private Integer quantityChange;

    @Column(name = "PreviousQty", nullable = false)
    private Integer previousQty;

    @Column(name = "NewQty", nullable = false)
    private Integer newQty;

    @Column(name = "ChangeType", nullable = false, length = 20)
    private String changeType;

    @Column(name = "ReferenceID")
    private Integer referenceId;

    @Column(name = "Notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ChangedBy")
    private Employee changedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public InventoryHistory(Smartphone smartphone, Integer quantityChange, Integer previousQty,
                           Integer newQty, String changeType, Integer referenceId,
                           String notes, Employee changedBy) {
        this.smartphone = smartphone;
        this.quantityChange = quantityChange;
        this.previousQty = previousQty;
        this.newQty = newQty;
        this.changeType = changeType;
        this.referenceId = referenceId;
        this.notes = notes;
        this.changedBy = changedBy;
    }
}
