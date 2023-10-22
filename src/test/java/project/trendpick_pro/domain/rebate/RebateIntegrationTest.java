package project.trendpick_pro.domain.rebate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import project.trendpick_pro.IntegrationTestSupport;
import project.trendpick_pro.domain.brand.entity.Brand;
import project.trendpick_pro.domain.brand.repository.BrandRepository;
import project.trendpick_pro.domain.coupon.entity.Coupon;
import project.trendpick_pro.domain.coupon.entity.CouponCard;
import project.trendpick_pro.domain.coupon.entity.dto.request.StoreCouponSaveRequest;
import project.trendpick_pro.domain.coupon.repository.CouponCardRepository;
import project.trendpick_pro.domain.coupon.repository.CouponRepository;
import project.trendpick_pro.domain.delivery.entity.Delivery;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.entity.MemberRoleType;
import project.trendpick_pro.domain.member.repository.MemberRepository;
import project.trendpick_pro.domain.orders.entity.Order;
import project.trendpick_pro.domain.orders.entity.OrderItem;
import project.trendpick_pro.domain.orders.repository.OrderItemRepository;
import project.trendpick_pro.domain.orders.repository.OrderRepository;
import project.trendpick_pro.domain.orders.service.OrderService;
import project.trendpick_pro.domain.product.entity.product.Product;
import project.trendpick_pro.domain.product.entity.product.ProductStatus;
import project.trendpick_pro.domain.product.entity.productOption.ProductOption;
import project.trendpick_pro.domain.product.entity.productOption.QProductOption;
import project.trendpick_pro.domain.product.entity.productOption.dto.ProductOptionSaveRequest;
import project.trendpick_pro.domain.product.repository.ProductOptionRepository;
import project.trendpick_pro.domain.product.repository.ProductRepository;
import project.trendpick_pro.domain.rebate.entity.RebateOrderItem;
import project.trendpick_pro.domain.rebate.service.impl.RebateServiceImpl;
import project.trendpick_pro.domain.store.entity.Store;
import project.trendpick_pro.domain.tags.tag.entity.Tag;
import project.trendpick_pro.domain.tags.tag.repository.TagRepository;
import project.trendpick_pro.global.util.rsData.RsData;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class RebateIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private CouponCardRepository couponCardRepository;

    @Autowired
    private RebateServiceImpl rebateService;
    @Autowired
    private CouponRepository couponRepository;

    @Test
    @DisplayName("주문 목록을 가져와서 정산 데이터를 생성한다.")
    void convertToRebateData(){
        //given
        Brand brand = brandRepository.save(createBrand());
        Coupon coupon = couponRepository.save(createCoupon());
        CouponCard createdCouponCard = createCouponCard(coupon);
        Member member = memberRepository.save(createMember());
        createdCouponCard.connectMember(member);
        CouponCard couponCard = couponCardRepository.save(createdCouponCard);
        ProductOption createProductOption = createProductOption();
        createProductOption.connectBrand(brand);
        Product createdProduct = createProduct();
        createdProduct.connectProductOption(createProductOption);
        Product product = productRepository.save(createdProduct);

        List<OrderItem> createOrderItems = new ArrayList<>();
        for(int i=0; i<100; i++){
            OrderItem createOrderItem = OrderItem.of(product, 100, "L", "RED");
            createOrderItems.add(createOrderItem);
        }

        Order order = orderRepository.save(
                Order.createOrder(member, new Delivery(member.getAddress()), createOrderItems));
        //when

        long l = System.currentTimeMillis();
        List<RebateOrderItem> rebateOrderItems = rebateService.makeRebateOrderItem("테스트용 브랜드", "2023-10").getData();

        //then
        Assertions.assertThat(rebateOrderItems.size()).isEqualTo(rebateOrderItems.size());

    }

    private static ProductOption createProductOption(){
        ProductOptionSaveRequest request = ProductOptionSaveRequest.of(List.of("L"), List.of("RED"), 100, 10000, ProductStatus.SALE.getText());
        ProductOption productOption = ProductOption.of(request);
        return productOption;
    }
    private static Product createProduct() {
        Product product = Product
                .builder()
                .productCode("P" + UUID.randomUUID())
                .title("테스트용 상품 100개")
                .description("테스트용 상품 100개 테스트 진행합니다.")
                .stock(100)
                .build();
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

    private static Member createMember() {
        Member member = Member.builder()
                .email("TrendPick@email.com")
                .password("12345")
                .username("TrendPick")
                .phoneNumber("010-1234-5678")
                .role(MemberRoleType.MEMBER)
                .brand("Polo")
                .build();
        return member;
    }

    private static Brand createBrand() {
        Brand brand = new Brand("테스트용 브랜드");
        return brand;
    }

    private static Coupon createCoupon() {
        Coupon coupon = Coupon.builder()
                .name("테스트용 쿠폰")
                .limitCount(100)
                .limitIssueDate(10)
                .minimumPurchaseAmount(1000)
                .discountPercent(10)
                .build();
        coupon.assignPostIssueExpiration(10);
        return coupon;
    }

    private static CouponCard createCouponCard(Coupon coupon) {
        return new CouponCard(coupon);
    }




}
