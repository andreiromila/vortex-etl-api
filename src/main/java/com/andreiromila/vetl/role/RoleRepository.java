package com.andreiromila.vetl.role;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface RoleRepository extends ListCrudRepository<Role, Long> {

    @Query("""
            SELECT user, role
            FROM user_role
            WHERE user in (:users)
            """)
    Set<UserRole> findUserRoleByUsers(@Param("users") List<Long> users);

    @Query("""
            SELECT user, role
            FROM user_role
            WHERE role in (:roles)
            """)
    Set<UserRole> findUserRoleByRoles(@Param("roles") List<Long> roles);

}
