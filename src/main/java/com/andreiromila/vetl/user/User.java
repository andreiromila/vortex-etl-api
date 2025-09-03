package com.andreiromila.vetl.user;

import com.andreiromila.vetl.role.RoleReference;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

@Data
@Table("user")
public class User implements UserDetails {

    @Id
    private Long id;

    private String username;
    private String password;
    private String email;
    private String fullName;

    // Spring security fields
    private boolean enabled = true;

    @Transient
    private final boolean accountNonExpired = true;

    @Transient
    private final boolean accountNonLocked = true;

    @Transient
    private final boolean credentialsNonExpired = true;

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

    @Transient
    public Collection<? extends GrantedAuthority> authorities;

}
