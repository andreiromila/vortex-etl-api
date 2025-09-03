package com.andreiromila.vetl.responses;

import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Contains the custom page format
 *
 * @param content       {@link List} The list of content
 * @param size          {@link Integer} The page size
 * @param page          {@link Integer} The page number (1 based)
 * @param totalElements {@link Integer} Total number of elements in the database
 * @param totalPages    {@link Integer} Total number of pages
 * @param <T>           The type for the content
 */
public record CustomPage<T>(
        List<T> content,
        int size,
        int page,
        long totalElements,
        int totalPages
) {

    /**
     * Constructor for the custom page for spring {@link Pageable} object
     *
     * @param content       {@link List} The content for the page
     * @param pageable      {@link Pageable} The page information
     * @param totalElements {@link Long} The total number of elements in the database
     */
    public CustomPage(List<T> content, Pageable pageable, long totalElements) {
        this(content, pageable.getPageSize(), page(pageable), totalElements, totalPages(pageable, totalElements));
    }

    /**
     * Returns the page number with 1 for the first page
     *
     * @param pageable {@link Pageable} The page information
     *
     * @return The page number, 1 for the first page
     */
    private static int page(final Pageable pageable) {
        return pageable.getPageNumber() + 1;
    }

    /**
     * Calculate the total number of pages
     *
     * @param pageable      {@link Pageable} The page information
     * @param totalElements {@link Long} The total elements in the database
     *
     * @return The number of total pages
     */
    private static int totalPages(final Pageable pageable, final long totalElements) {
        return (int) Math.ceil((double) totalElements / pageable.getPageSize());
    }

}