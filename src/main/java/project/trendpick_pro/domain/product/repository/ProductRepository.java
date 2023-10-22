package project.trendpick_pro.domain.product.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.trendpick_pro.domain.product.entity.product.Product;
import project.trendpick_pro.domain.product.entity.product.dto.request.ProductSearchCond;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    @Modifying
    @Query("UPDATE Product p SET p.productOption.price = :newPrice WHERE p.id = :productId")
    void updatePrice(@Param("productId") Long productId, @Param("newPrice") int newPrice);
    Page<Product> findAll(Pageable pageable);

    @Query("SELECT p FROM Product p JOIN FETCH p.productOption.brand WHERE p.id = :productId")
    Product findByIdWithBrand(@Param("productId") Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<Product> findByIdWithPessimisticLock(@Param("productId") Long productId);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<Product> findByIdWithOptimisticLock(@Param("productId") Long productId);

    @Query("SELECT distinct p FROM Product p " +
            "JOIN FETCH p.productOption po " +
            "JOIN FETCH p.tags " +
            "JOIN FETCH po.mainCategory " +
            "JOIN FETCH po.subCategory " +
            "JOIN FETCH po.brand " +
            "JOIN FETCH po.file f " +
            "WHERE p.id = :productId")
    Optional<Product> findByIdToConvertDto(@Param("productId") Long productId);

    @Query("SELECT distinct p FROM Product p " +
            "JOIN FETCH p.productOption po " +
            "JOIN FETCH p.tags " +
            "JOIN FETCH po.brand " +
            "JOIN FETCH po.file f " +
            "WHERE p.id = :productId")
    Page<Product> findProductsToProductListDto(ProductSearchCond cond, Pageable pageable);
}
