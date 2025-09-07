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
     * Devuelve un Pageable con ordenación segura, filtrando las columnas no permitidas.
     * Si no se proporcionan criterios de ordenación, mantiene el del Pageable original.
     */
    public static Pageable getPageableWithSafeSort(final Pageable pageable, final Set<String> allowedColumns) {
        return getPageableWithSafeSort(pageable, allowedColumns, Sort.unsorted());
    }

    /**
     * Devuelve un Pageable con ordenación segura y un fallback por defecto.
     * 1. Filtra los criterios del usuario para usar solo los permitidos.
     * 2. Si, tras filtrar, no queda ningún criterio válido, usa el 'defaultSort' proporcionado.
     *
     * @param pageable       El Pageable original de la petición.
     * @param allowedColumns El conjunto de columnas por las que se permite ordenar.
     * @param defaultSort    El Sort que se aplicará si no hay criterios válidos tras el filtrado.
     * @return Un nuevo Pageable seguro.
     */
    public static Pageable getPageableWithSafeSort(final Pageable pageable, final Set<String> allowedColumns, final Sort defaultSort) {
        // Filtra los criterios de ordenación del usuario
        final List<Sort.Order> safeOrders = pageable.getSort().stream()
                .filter(order -> allowedColumns.contains(order.getProperty()))
                .toList();

        // Determina qué Sort usar
        Sort finalSort;
        if (safeOrders.isEmpty()) {
            // Si el usuario no proporcionó un 'sort' válido (o no proporcionó ninguno),
            // usamos el 'defaultSort'.
            finalSort = defaultSort;
        } else {
            // Si el usuario proporcionó al menos un 'sort' válido, lo usamos.
            finalSort = Sort.by(safeOrders);
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), finalSort);
    }

}
