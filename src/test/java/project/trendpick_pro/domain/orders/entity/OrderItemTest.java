package project.trendpick_pro.domain.orders.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.trendpick_pro.domain.product.entity.product.Product;
import project.trendpick_pro.domain.product.entity.productOption.ProductOption;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemTest {


    @DisplayName("주문이 취소되면 기존 상품의 재고가 주문수량만큼 증가한다.")
    @Test
    void cancelOrderAfterIncreaseStock() throws Exception {
        //given
        Product product = Product.builder()
                .title("nice shirt title")
                .description("nice shirt description")
                .build();

        ProductOption productOption = ProductOption.builder()
                .size(List.of("L", "XL"))
                .color(List.of("red", "blue"))
                .stock(10)
                .price(10000)
                .build();
        product.connectProductOption(productOption);

        OrderItem orderItem = OrderItem.of(product, 2, "L", "red");

        //when
        orderItem.cancel();

        //then
        assertThat(productOption.getStock()).isEqualTo(12);
    }
}