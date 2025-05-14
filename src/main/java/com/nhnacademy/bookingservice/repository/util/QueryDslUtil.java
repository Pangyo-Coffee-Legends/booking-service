package com.nhnacademy.bookingservice.repository.util;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class QueryDslUtil {

    private QueryDslUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static OrderSpecifier<?> getSortColumn(Order order, Path<?> parent, String fieldName) {
        Path<Object> fieldPath = Expressions.path(Object.class, parent, fieldName);

        return new OrderSpecifier(order, fieldPath);
    }

    public static List<OrderSpecifier> getOrderSpecifiers(Pageable pageable, PathBuilder<?> entityPath) {
        return pageable.getSort().stream()
                .map(order -> {
                    Path<Object> path = entityPath.get(order.getProperty());
                    return order.isAscending() ? new OrderSpecifier(Order.ASC, path) : new OrderSpecifier(Order.DESC, path);
                })
                .toList();
    }
}
