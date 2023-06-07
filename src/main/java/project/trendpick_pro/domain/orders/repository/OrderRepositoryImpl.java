package project.trendpick_pro.domain.orders.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import project.trendpick_pro.domain.orders.entity.dto.request.OrderSearchCond;
import project.trendpick_pro.domain.orders.entity.dto.response.OrderResponse;
import project.trendpick_pro.domain.orders.entity.dto.response.QOrderResponse;

import java.util.List;

import static project.trendpick_pro.domain.delivery.entity.QDelivery.*;
import static project.trendpick_pro.domain.member.entity.QMember.*;
import static project.trendpick_pro.domain.orders.entity.QOrder.*;
import static project.trendpick_pro.domain.orders.entity.QOrderItem.*;
import static project.trendpick_pro.domain.product.entity.QProduct.*;

public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public OrderRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    //나의 주문목록
    @Override
    public Page<OrderResponse> findAllByMember(OrderSearchCond orderSearchCond, Pageable pageable) {
        List<OrderResponse> result = queryFactory
                .select(new QOrderResponse(
                        orderItem.product.id,
                        orderItem.product.file.fileName,
                        orderItem.product.brand.name,
                        orderItem.product.name,
                        orderItem.count,
                        orderItem.orderPrice,
                        orderItem.order.createdDate,
                        orderItem.order.status.stringValue(),
                        orderItem.order.delivery.state.stringValue())
                )
                .from(orderItem)
                .join(orderItem.order, order)
                .join(order.member, member)
                .on(member.id.eq(orderSearchCond.getMemberId()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderItem.order.createdDate.asc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(order.count())
                .from(order)
                .join(order.member, member)
                .on(member.id.eq(orderSearchCond.getMemberId()))
                ;
        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }
}
