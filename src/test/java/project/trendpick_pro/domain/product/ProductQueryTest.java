package project.trendpick_pro.domain.product;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import project.trendpick_pro.IntegrationTestSupport;
import project.trendpick_pro.domain.brand.entity.Brand;
import project.trendpick_pro.domain.brand.repository.BrandRepository;
import project.trendpick_pro.domain.category.entity.MainCategory;
import project.trendpick_pro.domain.category.entity.SubCategory;
import project.trendpick_pro.domain.category.repository.MainCategoryRepository;
import project.trendpick_pro.domain.category.repository.SubCategoryRepository;
import project.trendpick_pro.domain.common.file.CommonFile;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.entity.MemberRoleType;
import project.trendpick_pro.domain.member.repository.MemberRepository;
import project.trendpick_pro.domain.product.entity.product.Product;
import project.trendpick_pro.domain.product.entity.product.ProductStatus;
import project.trendpick_pro.domain.product.entity.product.dto.response.ProductResponse;
import project.trendpick_pro.domain.product.entity.productOption.ProductOption;
import project.trendpick_pro.domain.product.entity.productOption.dto.ProductOptionSaveRequest;
import project.trendpick_pro.domain.product.repository.ProductOptionRepository;
import project.trendpick_pro.domain.product.repository.ProductRepository;
import project.trendpick_pro.domain.product.service.impl.ProductServiceImpl;
import project.trendpick_pro.domain.tags.tag.entity.Tag;
import project.trendpick_pro.domain.tags.tag.repository.TagRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ProductQueryTest extends IntegrationTestSupport {

    @Autowired
    ProductServiceImpl productService;
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
    private MainCategoryRepository mainCategoryRepository;
    @Autowired
    private SubCategoryRepository subCategoryRepository;

    //JPA N+1 이슈 테스트

    @Test
    @DisplayName("product 조회 후 직접 DTO 변환")
    void getProductV1(){
        //given
        MainCategory mainCategory = mainCategoryRepository.save(createMainCategory());
        SubCategory subCategory = subCategoryRepository.save(createMainCategory(mainCategory));
        CommonFile file = createCommonFile();
        Brand brand = brandRepository.save(createBrand());
        Member member = memberRepository.save(createMember());
        ProductOption createProductOption = createProductOption();
        createProductOption.connectBrand(brand);
        createProductOption.settingConnection(brand,mainCategory, subCategory, file, ProductStatus.SALE);
        Product createdProduct = createProduct();
        createdProduct.connectProductOption(createProductOption);
        Product product = productRepository.save(createdProduct);

        //상품 1
        //상품 - 상품 옵션 1 , 상품 - 태그 2
        //상품 옵션 - (카테고리 1, 브랜드 1, 파일 1)

        //when
        System.out.println("QUERY===============================================================");
        System.out.println(product.getId());
        ProductResponse productResponse = productService.getProduct(product.getId());
        System.out.println("QUERY===============================================================");

        Assertions.assertThat(productResponse.getId()).isEqualTo(product.getId());
        Assertions.assertThat(productResponse.getTags().size()).isEqualTo(2);
    }


    private static MainCategory createMainCategory(){
        return new MainCategory("메인카테고리");
    }

    private static SubCategory createMainCategory(MainCategory mainCategory){
        return new SubCategory("서브카테고리", mainCategory);
    }

    private static CommonFile createCommonFile(){
        return CommonFile.builder()
                .fileName("테스트용 파일이름입니다.")
                .build();
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
                .title("테스트용 상품")
                .description("테스트용 상품입니다")
                .stock(100)
                .build();
        Set<Tag> tags = new LinkedHashSet<>();
        for (int i = 1; i <= 10; i++) {
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
}
