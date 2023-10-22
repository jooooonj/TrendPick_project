package project.trendpick_pro.domain.recommend.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import project.trendpick_pro.IntegrationTestSupport;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.entity.MemberRoleType;
import project.trendpick_pro.domain.member.repository.MemberRepository;
import project.trendpick_pro.domain.product.entity.product.Product;
import project.trendpick_pro.domain.product.entity.product.ProductStatus;
import project.trendpick_pro.domain.product.entity.productOption.ProductOption;
import project.trendpick_pro.domain.product.entity.productOption.dto.ProductOptionSaveRequest;
import project.trendpick_pro.domain.product.repository.ProductRepository;
import project.trendpick_pro.domain.recommend.entity.Recommend;
import project.trendpick_pro.domain.tags.tag.entity.Tag;

import java.util.*;


class RecommendRepositoryTest extends IntegrationTestSupport {
    @Autowired
    RecommendRepository recommendRepository;
    @Autowired
    ProductRepository productRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("jpa saveAll 성능 측정")
    void saveAll(){
        Product product = productRepository.save(createProduct());
        Member member = memberRepository.save(createMember());
        List<Recommend> recommendList = new ArrayList<>();

        for(int i=0; i<3000; i++){
            Recommend recommend = Recommend.of(product);
            recommend.connectMember(member);
            recommendList.add(recommend);
        }

        long l = System.currentTimeMillis();
        recommendRepository.saveAll(recommendList);
        System.out.println("소요시간 : " + (System.currentTimeMillis() - l));
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
}