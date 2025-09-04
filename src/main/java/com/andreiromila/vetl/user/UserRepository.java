package com.andreiromila.vetl.user;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface UserRepository extends UserFilterRepository, ListCrudRepository<User, Long>, PagingAndSortingRepository<User, Long> {

    Optional<User> findByUsername(final String username);

    Optional<User> findByEmail(final String email);

    @Modifying
    @Query("INSERT INTO user_role (user, role) VALUES (:userId, :roleId)")
    void insertUserRole(Long userId, Long roleId);

    default void insertUserRoles(Long userId, Set<Long> roles) {
        roles.forEach(it -> insertUserRole(userId, it));
    }

    /**
     * Updates the avatar key for a specific user.
     *
     * @param userId    {@link Long} The ID of the user to update.
     * @param avatarKey {@link String} The new avatar object key from MinIO.
     */
    @Modifying
    @Query("UPDATE user SET avatar_key = :avatarKey WHERE id = :userId")
    void updateAvatarKey(@Param("userId") Long userId, @Param("avatarKey") String avatarKey);

}
