package com.snezhinskiy.crawler.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@Table(name="product")
@Entity
public class Product {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "product_id_seq_gen")
    @GenericGenerator(
        name = "product_id_seq_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @org.hibernate.annotations.Parameter(name = "sequence_name", value = "product_id_seq"),
            @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
            @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
        }
    )
    private Long id;

    private Integer domainHash;
    private Integer hash;
    private String sku;
    private String name;
    private String modificationCode;
    private String modificationName;
    private Double price;
    private String description;
    private String url;
    private String previewUrl;
    private Integer stock;

    @CreationTimestamp
    protected LocalDateTime createdAt;

    @UpdateTimestamp
    protected LocalDateTime updatedAt;
}
