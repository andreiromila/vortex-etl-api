package com.andreiromila.vetl.role;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("role")
public record Role(
        @Id Long id,
        String name,
        String description,

        @CreatedDate
        Instant createdAt,

        @LastModifiedDate
        Instant modifiedAt
) { }
