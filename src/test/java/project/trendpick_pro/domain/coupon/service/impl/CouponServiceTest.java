package project.trendpick_pro.domain.coupon.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import project.trendpick_pro.IntegrationTestSupport;
import project.trendpick_pro.domain.coupon.entity.Coupon;
import project.trendpick_pro.domain.coupon.entity.dto.request.StoreCouponSaveRequest;
import project.trendpick_pro.domain.coupon.entity.dto.response.CouponResponse;
import project.trendpick_pro.domain.coupon.repository.CouponRepository;
import project.trendpick_pro.domain.coupon.service.CouponService;
import project.trendpick_pro.domain.product.repository.ProductRepository;
import project.trendpick_pro.domain.product.service.ProductService;
import project.trendpick_pro.domain.store.entity.Store;
import project.trendpick_pro.domain.store.repository.StoreRepository;
import project.trendpick_pro.global.util.rsData.RsData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class CouponServiceTest extends IntegrationTestSupport {

    @Autowired
    private CouponService couponService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @DisplayName("브랜드에서 쿠폰을 발급한다.")
    @Test
    void createCoupon() throws Exception {
        //given
        String storeName = "polo";
        storeRepository.save(new Store(storeName));

        StoreCouponSaveRequest request = StoreCouponSaveRequest.builder()
                .name("coupon")
                .limitCount(100)
                .limitIssueDate(10)
                .minimumPurchaseAmount(5000)
                .discountPercent(50)
                .expirationType("ISSUE_AFTER_DATE")
                .issueAfterDate(21)
                .build();

        //when
        RsData<String> couponId = couponService.createCoupon(storeName, request);

        //then
        Coupon coupon = couponRepository.findById(Long.parseLong(couponId.getData())).get();
        assertThat(coupon).isNotNull()
                .extracting("name", "limitIssueDate", "minimumPurchaseAmount", "discountPercent")
                .contains("[polo] coupon", 10, 5000, 50);
    }

    private static Coupon createCoupon(String storeName) {
        Coupon coupon = Coupon.builder()
                .name("[" + storeName + "]" + " " + "couponName")
                .limitCount(100)
                .limitIssueDate(10)
                .minimumPurchaseAmount(1000)
                .discountPercent(10)
                .build();
        coupon.assignPostIssueExpiration(10);
        return coupon;
    }
}