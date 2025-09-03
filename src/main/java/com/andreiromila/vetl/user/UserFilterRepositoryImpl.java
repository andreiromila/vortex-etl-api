package com.andreiromila.vetl.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User filter repository implementation
 */
@Repository
public class UserFilterRepositoryImpl implements UserFilterRepository {

    /**
     * Contains the jdbc aggregate template instance
     */
    private final JdbcAggregateTemplate aggregateTemplate;

    /**
     * User filter repository constructor
     *
     * @param aggregateTemplate {@link JdbcAggregateTemplate} Jdbc Aggregate Template
     */
    public UserFilterRepositoryImpl(final JdbcAggregateTemplate aggregateTemplate) {
        this.aggregateTemplate = aggregateTemplate;
    }

    /**
     * Searches for users based on the list of filters
     *
     * @param query    {@link String} The query filter
     * @param pageable {@link Pageable} The pagination data
     *
     * @return The page with relevant results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<User> search(final String query, final Pageable pageable) {

        // Get the criteria based on the filter list
        final Criteria criteria = getCriteriaList(query).stream()
                .reduce(Criteria.empty(), Criteria::or);

        // Execute the query
        return aggregateTemplate.findAll(Query.query(criteria), User.class, pageable);
    }

    /**
     * Creates the criteria list for searching
     *
     * @param query {@link String} The query to search by
     *
     * @return The list of criteria
     */
    private List<Criteria> getCriteriaList(final String query) {

        // Add the % for the like
        final String likeQuery = "%" + query + "%";

        // Create the list of criteria
        return List.of(
                Criteria.where("fullName").like(likeQuery).ignoreCase(true),
                Criteria.where("username").like(likeQuery).ignoreCase(true),
                Criteria.where("email").like(likeQuery).ignoreCase(true)
        );
    }
}

