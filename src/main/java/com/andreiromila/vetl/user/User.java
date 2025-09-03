package com.andreiromila.vetl.user;

import com.andreiromila.vetl.role.RoleReference;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Set;

@Data
@Table("user")
public class User {

    @Id
    private Long id;

    private String username;
    private String password;
    private String email;
    private String fullName;
    private boolean enabled = true;

    /**
     * Read only property, the first value is
     * set by the database on creation.
     */
    @ReadOnlyProperty
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant modifiedAt;

    private Set<RoleReference> roles;

}
