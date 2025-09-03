package com.andreiromila.vetl.role;

import org.springframework.data.relational.core.mapping.Table;

@Table("user_role")
public record RoleReference(Long role) {
}
