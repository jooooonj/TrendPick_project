package project.trendpick_pro.domain.review.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.trendpick_pro.domain.common.file.CommonFile;
import project.trendpick_pro.domain.common.file.FileTranslator;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.product.entity.product.Product;
import project.trendpick_pro.domain.product.service.ProductService;
import project.trendpick_pro.domain.review.entity.Review;
import project.trendpick_pro.domain.review.entity.dto.request.ReviewSaveRequest;
import project.trendpick_pro.domain.review.entity.dto.response.ReviewProductResponse;
import project.trendpick_pro.domain.review.entity.dto.response.ReviewResponse;
import project.trendpick_pro.domain.review.repository.ReviewRepository;
import project.trendpick_pro.domain.review.service.RedisCacheService;
import project.trendpick_pro.domain.review.service.ReviewService;
import project.trendpick_pro.global.util.rq.Rq;
import project.trendpick_pro.global.util.rsData.RsData;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final FileTranslator fileTranslator;
    private final Rq rq;
    private final ProductService productService;
    private final RedisCacheService redisCacheService;
    private final AmazonS3 amazonS3;
    private static final String REVIEW_COUNT_CACHE_KEY = "reviewCount:Item:";
    private static final String AVERAGE_RATE_CACHE_KEY = "averageRating:Item:";

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Transactional
    public RsData<ReviewResponse> create(Member actor, Long productId, ReviewSaveRequest reviewSaveRequest, MultipartFile requestMainFile, List<MultipartFile> requestSubFiles) throws Exception {
        Product product = productService.findById(productId);

        CommonFile mainFile = fileTranslator.saveFile(requestMainFile);
        List<CommonFile> subFiles = fileTranslator.saveFiles(requestSubFiles);

        for (CommonFile subFile : subFiles) {
            mainFile.connectFile(subFile);
        }

        Review review = Review.of(reviewSaveRequest, actor, product, mainFile);
        product.addReview(review.getRating()); //상품 리뷰수, 상품 평균 평점을 계산해서 저장
        Review savedReview = reviewRepository.save(review);

        String cacheKey = REVIEW_COUNT_CACHE_KEY + product.getId();
        redisCacheService.plusOneToTotalNumberOfReviewsByProductId(product.getId(), cacheKey);
        redisCacheService.updateAverageRatingByProductId(product.getId(), cacheKey,
                savedReview.getRating());

        return RsData.of("S-1", "리뷰 등록이 완료되었습니다.", ReviewResponse.of(review));
    }

    @Transactional
    public RsData<ReviewResponse> modify(Long reviewId, ReviewSaveRequest reviewSaveRequest, MultipartFile requestMainFile, List<MultipartFile> requestSubFiles) throws IOException {
        Review review = reviewRepository.findById(reviewId).orElseThrow();

        review.getFile().deleteFile(amazonS3, bucket);
        review.disconnectFile();

        CommonFile mainFile = fileTranslator.saveFile(requestMainFile);
        List<CommonFile> subFiles = fileTranslator.saveFiles(requestSubFiles);

        for (CommonFile subFile : subFiles) {
            mainFile.connectFile(subFile);
        }

        review.update(reviewSaveRequest, mainFile);
        return RsData.of("S-1", "리뷰 수정이 완료되었습니다.", ReviewResponse.of(review));
    }

    @Transactional
    public void delete(Long reviewId) {
        rq.getAdmin();
        Review review = reviewRepository.findById(reviewId).orElseThrow();
        review.getFile().deleteFile(amazonS3, bucket);
        reviewRepository.delete(review);

        String cacheKey = REVIEW_COUNT_CACHE_KEY + review.getProduct().getId();
        redisCacheService.minusOneToTotalNumberOfReviewsByProductId(review.getProduct().getId(), cacheKey);
    }

    public ReviewResponse getReview(Long productId) {
        Review review = reviewRepository.findById(productId).orElseThrow();
        return ReviewResponse.of(review);
    }

    public Page<ReviewProductResponse> getReviews(Long productId, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), 6);
        return reviewRepository.findAllByProductId(productId, pageable);
    }

    @Transactional(readOnly = true)
    public Long findTotalReviewsByProduct(
            final Long productId
    ) {
        Product product = productService.findById(productId);

        String cacheKey = REVIEW_COUNT_CACHE_KEY + product.getId();
        return redisCacheService.getTotalNumberOfReviewsByProductId(product.getId(), cacheKey);
    }

    @Transactional(readOnly = true)
    public Double findAverageRatingByProduct(
            final Long productId
    ) {
        Product product = productService.findById(productId);

        String cacheKey = AVERAGE_RATE_CACHE_KEY + product.getId();
        return redisCacheService.getAverageRatingByProductId(productId, cacheKey);
    }

    public Review findById(Long id) {
        return reviewRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Page<ReviewResponse> showAll(Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), 6);
        Page<Review> reviewPage = reviewRepository.findAll(pageable);
        return reviewPage.map(ReviewResponse::of);
    }

    @Transactional
    public Page<ReviewResponse> showOwnReview(String writer, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), 6);
        Page<Review> reviewPage = reviewRepository.findByWriter(writer, pageable);
        return reviewPage.map(ReviewResponse::of);
    }
}
