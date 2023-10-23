package project.trendpick_pro.domain.orders;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import project.trendpick_pro.IntegrationTestSupport;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.entity.MemberRoleType;
import project.trendpick_pro.domain.member.repository.MemberRepository;
import project.trendpick_pro.domain.orders.repository.OrderItemRepository;
import project.trendpick_pro.domain.orders.repository.OrderRepository;
import project.trendpick_pro.domain.orders.service.OrderService;
import project.trendpick_pro.domain.product.entity.product.Product;
import project.trendpick_pro.domain.product.entity.product.ProductStatus;
import project.trendpick_pro.domain.product.entity.productOption.ProductOption;
import project.trendpick_pro.domain.product.entity.productOption.dto.ProductOptionSaveRequest;
import project.trendpick_pro.domain.product.exception.ProductStockOutException;
import project.trendpick_pro.domain.product.repository.ProductRepository;
import project.trendpick_pro.domain.tags.tag.entity.Tag;
import project.trendpick_pro.domain.tags.tag.repository.TagRepository;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("재고 차감 동시성 테스트")
    void concurrencyTest() throws InterruptedException {

        Product testProduct = productRepository.save(createProduct());
        Member testMember = memberRepository.save(createMember1());

        int threadCount = 100;
        //멀티스레드 이용 ExecutorService : 비동기를 단순하게 처리할 수 있또록 해주는 java api
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        //다른 스레드에서 수행이 완료될 때 까지 대기할 수 있도록 도와주는 API - 요청이 끝날때 까지 기다림
        CountDownLatch latch = new CountDownLatch(threadCount);


        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                            orderService.productToOrder(testMember, testProduct.getId(), 1, "L", "RED");
                            latch.countDown();
                    }
            );
        }

        latch.await();

        Product findProduct = productRepository.findById(testProduct.getId()).get();
        Assertions.assertThat(findProduct.getStock()).isEqualTo(0);

    }

    @Test
    @DisplayName("재고 차감 동시성 테스트 실패 _ 재고부족 예외 발생")
    void concurrencyTest_fail() throws InterruptedException {

        Product testProduct = productRepository.save(createProduct());
        Member testMember = memberRepository.save(createMember2());

        int threadCount = 100;
        //멀티스레드 이용 ExecutorService : 비동기를 단순하게 처리할 수 있또록 해주는 java api
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        //다른 스레드에서 수행이 완료될 때 까지 대기할 수 있도록 도와주는 API - 요청이 끝날때 까지 기다림
        CountDownLatch latch = new CountDownLatch(threadCount);

        //100개 모두 감소
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                        orderService.productToOrder(testMember, testProduct.getId(), 1, "L", "RED");
                        latch.countDown();
                    }
            );
        }

        latch.await();

        Assertions.assertThatThrownBy(() -> orderService.productToOrder(testMember, testProduct.getId(), 1, "L", "RED"))
                .isInstanceOf(ProductStockOutException .class)
                .hasMessage("상품의 재고가 부족하여 주문이 불가능합니다.");

    }

    private static Product createProduct() {
        ProductOptionSaveRequest request = ProductOptionSaveRequest.of(List.of("L"), List.of("RED"), 100, 10000, ProductStatus.SALE.getText());
        ProductOption productOption = ProductOption.of(request);
        Product product = Product
                .builder()
                .productCode("P" + UUID.randomUUID())
                .title("테스트용 상품 100개")
                .description("테스트용 상품 100개 테스트 진행합니다.")
                .stock(100)
                .build();
        product.connectProductOption(productOption);
        Set<Tag> tags = new LinkedHashSet<>();
        for (int i = 1; i <= (Math.random() * 13)+5; i++) {
            while (true) {
                Tag tag = new Tag("태그이름");
                if (tags.add(tag)) {
                    break;
                }
            }
        }
        product.updateTags(tags);
        return product;
    }

    private static Member createMember1() {
        Member member = Member.builder()
                .email("TrendPick1234@email.com")
                .password("12345")
                .username("TrendPick")
                .phoneNumber("010-1234-5678")
                .role(MemberRoleType.MEMBER)
                .brand("Polo")
                .build();
        return member;
    }

    private static Member createMember2() {
        Member member = Member.builder()
                .email("TrendPick123456@email.com")
                .password("12345")
                .username("TrendPick")
                .phoneNumber("010-1234-5678")
                .role(MemberRoleType.MEMBER)
                .brand("Polo")
                .build();
        return member;
    }
}
