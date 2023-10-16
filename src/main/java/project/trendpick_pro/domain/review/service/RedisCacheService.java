package project.trendpick_pro.domain.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import project.trendpick_pro.domain.review.repository.ReviewRepository;

@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final ReviewRepository reviewRepository;
    private final RedisTemplate<String, Long> numberOfReviewsRedisTemplate;
    private final ListOperations<String, String> listOperations;

    public Long getTotalNumberOfReviewsByProductId(
            final Long productId,
            final String cacheKey
    ) {
        Long cachedCount = numberOfReviewsRedisTemplate.opsForValue().get(cacheKey);

        if (cachedCount != null) {
            return cachedCount;
        }

        long dbCount = reviewRepository.countByProduct_ProductId(productId);

        numberOfReviewsRedisTemplate.opsForValue().set(cacheKey, dbCount);

        return dbCount;
    }

    public void plusOneToTotalNumberOfReviewsByProductId(
            final Long productId,
            final String cacheKey
    ) {
        Long cachedCount = numberOfReviewsRedisTemplate.opsForValue().get(cacheKey);

        if (cachedCount != null) {
            numberOfReviewsRedisTemplate.opsForValue().set(cacheKey, cachedCount + 1);
        }

        long dbCount = reviewRepository.countByProduct_ProductId(productId);

        numberOfReviewsRedisTemplate.opsForValue().set(cacheKey, dbCount);
    }

    public void minusOneToTotalNumberOfReviewsByProductId(
            final Long productId,
            final String cacheKey
    ) {
        Long cachedCount = numberOfReviewsRedisTemplate.opsForValue().get(cacheKey);

        if (cachedCount != null) {
            numberOfReviewsRedisTemplate.opsForValue().set(cacheKey, cachedCount - 1);
        }

        long dbCount = reviewRepository.countByProduct_ProductId(productId);

        numberOfReviewsRedisTemplate.opsForValue().set(cacheKey, dbCount);
    }

    public double getAverageRatingByProductId(
            final Long productId,
            final String cacheKey
    ) {
        String averageRating = listOperations.index(cacheKey, 0);

        if (averageRating != null) {
            return Double.parseDouble(averageRating);
        }

        Double dbAverageRating = reviewRepository.findAverageRatingByProductId(productId);
        Long numberOfReviews = reviewRepository.countByProduct_ProductId(productId);

        listOperations.rightPushAll(cacheKey, String.valueOf(dbAverageRating),
                String.valueOf(numberOfReviews));

        return dbAverageRating;
    }

    public void updateAverageRatingByProductId(
            final Long productId,
            final String cacheKey,
            final double newRating
    ) {
        String averageRating = listOperations.index(cacheKey, 0);
        String totalNumberOfReviews = listOperations.index(cacheKey, 1);

        if (averageRating != null && totalNumberOfReviews != null) {
            double totalRating =
                    Double.parseDouble(averageRating) * Long.parseLong(totalNumberOfReviews);

            long updatedTotalNumberOfReviews = Long.parseLong(totalNumberOfReviews) + 1;

            double updatedAverageRating = Math.round(
                    (totalRating + newRating) / updatedTotalNumberOfReviews) * 100
                    / 100.0;

            listOperations.set(cacheKey, 0, String.valueOf(updatedAverageRating));
            listOperations.set(cacheKey, 1, String.valueOf(updatedTotalNumberOfReviews));
        }

        double dbAverageRating = reviewRepository.findAverageRatingByProductId(productId);
        long dbNumberOfReviews = reviewRepository.countByProduct_ProductId(productId);

        listOperations.rightPushAll(cacheKey, String.valueOf(dbAverageRating),
                String.valueOf(dbNumberOfReviews));
    }

    public void synchronizeNumberOfReview(
            final Long productId,
            final String cacheKey
    ) {
        long dbCount = reviewRepository.countByProduct_ProductId(productId);

        numberOfReviewsRedisTemplate.opsForValue().set(cacheKey, dbCount);
    }

    public void synchronizeAverageRating(
            final Long productId,
            final String cacheKey
    ) {
        String averageRating = listOperations.index(cacheKey, 0);
        String totalNumberOfReviews = listOperations.index(cacheKey, 1);

        if (averageRating != null && totalNumberOfReviews != null) {
            double dbAverageRating = reviewRepository.findAverageRatingByProductId(productId);
            long dbNumberOfReviews = reviewRepository.countByProduct_ProductId(productId);

            listOperations.set(cacheKey, 0, String.valueOf(dbAverageRating));
            listOperations.set(cacheKey, 1, String.valueOf(dbNumberOfReviews));

            return;
        }

        double dbAverageRating = reviewRepository.findAverageRatingByProductId(productId);
        long dbNumberOfReviews = reviewRepository.countByProduct_ProductId(productId);

        listOperations.rightPushAll(cacheKey, String.valueOf(dbAverageRating),
                String.valueOf(dbNumberOfReviews));
    }
}
