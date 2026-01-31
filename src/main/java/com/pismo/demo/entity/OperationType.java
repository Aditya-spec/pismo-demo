package com.pismo.demo.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "operation_type")
public class OperationType {

    @Id
    @Column(name = "operation_type_id")
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(name = "sign_multiplier", nullable = false)
    private Integer signMultiplier;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSignMultiplier() {
        return signMultiplier;
    }

    public void setSignMultiplier(Integer signMultiplier) {
        this.signMultiplier = signMultiplier;
    }
}
