package com.andreiromila.vetl.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

/**
 * Pageable utility class
 */
public class PageableUtils {

    /**
     * Returns the pageable with filtered sort, based
     * on the provided allowed columns
     *
     * @param pageable {@link Pageable} Unsafe, user input pageable
     * @param columns  {@link Set} The list of allowed sorting columns
     *
     * @return The safe pageable
     */
    public static Pageable getPageableWithSafeSort(final Pageable pageable, final Set<String> columns) {

        // Filter down to the allowed columns only
        final List<Sort.Order> orderList = pageable.getSort().stream()
                .filter(order -> columns.contains(order.getProperty()))
                .toList();

        // Return the new safe pageable
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orderList));
    }

}
