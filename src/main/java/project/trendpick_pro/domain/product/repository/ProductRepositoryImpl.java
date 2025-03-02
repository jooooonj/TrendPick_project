package project.trendpick_pro.domain.product.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import project.trendpick_pro.domain.product.entity.product.dto.request.ProductSearchCond;
import project.trendpick_pro.domain.product.entity.product.dto.response.*;

import java.util.List;

import static project.trendpick_pro.domain.brand.entity.QBrand.brand;
import static project.trendpick_pro.domain.category.entity.QMainCategory.mainCategory;
import static project.trendpick_pro.domain.category.entity.QSubCategory.subCategory;
import static project.trendpick_pro.domain.common.file.QCommonFile.commonFile;
import static project.trendpick_pro.domain.product.entity.product.QProduct.product;
import static project.trendpick_pro.domain.product.entity.productOption.QProductOption.productOption;
import static project.trendpick_pro.domain.tags.favoritetag.entity.QFavoriteTag.favoriteTag;
import static project.trendpick_pro.domain.tags.tag.entity.QTag.tag;

public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ProductRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<ProductListResponse> getProductResponseWithQuerydsl(ProductSearchCond cond, Pageable pageable) {
        List<ProductListResponse> result = queryFactory
                .select(new QProductListResponse(
                        product.id,
                        product.title,
                        brand.name,
                        commonFile.fileName,
                        productOption.price,
                        product.discountRate,
                        product.discountedPrice
                        )
                )
                .from(product)
                .innerJoin(product.productOption, productOption)
                .innerJoin(productOption.mainCategory, mainCategory)
                .innerJoin(productOption.subCategory, subCategory)
                .innerJoin(productOption.brand, brand)
                .innerJoin(productOption.file, commonFile)
                .where(
                        mainCategoryEq(cond),
                        subCategoryEq(cond)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = queryFactory
                .select(product.count())
                .from(product)
                .innerJoin(product.productOption, productOption)
                .innerJoin(productOption.mainCategory, mainCategory)
                .innerJoin(productOption.subCategory, subCategory)
                .innerJoin(productOption.brand, brand)
                .innerJoin(productOption.file, commonFile)
                .where(
                        mainCategoryEq(cond),
                        subCategoryEq(cond)
                );

        return PageableExecutionUtils.getPage(result, pageable, count::fetchOne);
    }

    @Override
    public List<ProductByRecommended> findRecommendProduct(String email) {
        return queryFactory
                .select(new QProductByRecommended(
                        tag.product.id,
                        tag.name
                ))
                .from(tag)
                .where(tag.name.in(
                    JPAExpressions.select(favoriteTag.name)
                        .from(favoriteTag)
                        .where(favoriteTag.member.email.eq(email))
                    )
                )
                .distinct()
                .fetch();
    }

    @Override
    public Page<ProductListResponseBySeller> findAllBySeller(String brand, Pageable pageable) {
        List<ProductListResponseBySeller> list = queryFactory
                .select(new QProductListResponseBySeller(
                        product.id,
                        product.title,
                        commonFile.fileName,
                        productOption.price,
                        productOption.stock,
                        product.createdDate,
                        product.saleCount,
                        product.rateAvg,
                        product.reviewCount,
                        product.askCount,
                        product.discountRate,
                        product.discountedPrice
                ))
                .from(product)
                .innerJoin(product.productOption, productOption)
                .innerJoin(productOption.file, commonFile)
                .where(productOption.brand.name.eq(brand))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(product.createdDate.desc())
                .fetch();

        JPAQuery<Long> count = queryFactory
                .select(product.count())
                .from(product)
                .innerJoin(product.productOption.file, commonFile)
                .where(product.productOption.brand.name.eq(brand));

        return PageableExecutionUtils.getPage(list, pageable, count::fetchOne);
    }

    @Override
    public Page<ProductListResponse> findAllByKeyword(ProductSearchCond cond, Pageable pageable) {
        List<ProductListResponse> result = queryFactory
                .select(new QProductListResponse(
                        product.id,
                        product.title,
                        brand.name,
                        commonFile.fileName,
                        productOption.price,
                        product.discountRate,
                        product.discountedPrice
                        )
                )
                .from(product)
                .innerJoin(product.productOption, productOption)
                .innerJoin(productOption.mainCategory, mainCategory)
                .innerJoin(productOption.subCategory, subCategory)
                .innerJoin(productOption.brand, brand)
                .innerJoin(productOption.file, commonFile)
                .where(
                        product.title.contains(cond.getKeyword())
                    .or(brand.name.contains(cond.getKeyword()))
                    .or(mainCategory.name.contains(cond.getKeyword()))
                    .or(subCategory.name.contains(cond.getKeyword()))
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = queryFactory
                .select(product.count())
                .from(product)
                .innerJoin(product.productOption, productOption)
                .innerJoin(productOption.mainCategory, mainCategory)
                .innerJoin(productOption.subCategory, subCategory)
                .innerJoin(productOption.brand, brand)
                .innerJoin(productOption.file, commonFile)
                .where(
                        product.title.contains(cond.getKeyword())
                                .or(brand.name.contains(cond.getKeyword()))
                                .or(mainCategory.name.contains(cond.getKeyword()))
                                .or(subCategory.name.contains(cond.getKeyword()))
                );

        return PageableExecutionUtils.getPage(result, pageable, count::fetchOne);
    }

    private static BooleanExpression mainCategoryEq(ProductSearchCond cond) {
        if (cond.getMainCategory().equals("전체")) {
            return null;
        } else {
            return mainCategory.name.eq(cond.getMainCategory());
        }
    }

    private static BooleanExpression subCategoryEq(ProductSearchCond cond) {
        if (cond.getSubCategory().equals("전체")) {
            return null;
        } else {
            return subCategory.name.eq(cond.getSubCategory());
        }
    }

    private static OrderSpecifier<?>
    orderSelector(Integer sortCode) {
        return switch (sortCode) {
            case 2 -> product.id.asc();
            case 3 -> product.rateAvg.desc();
            case 4 -> product.rateAvg.asc();
            case 5 -> product.reviewCount.desc();
            default -> product.id.desc();
        };
    }
}