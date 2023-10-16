package project.trendpick_pro.domain.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import project.trendpick_pro.domain.review.repository.ReviewRepository;

@Service
@RequiredArgsConstructor
public class ReviewSchedulerService {

    private final RedisCacheService redisCacheService;
    private final ReviewRepository reviewRepository;

    private static final String AVERAGE_RATE_CACHE_KEY = "averageRating:Item:";
    private static final String REVIEW_COUNT_CACHE_KEY = "reviewCount:Item:";

    @Scheduled(cron = "0 0 * * 1 *")
    public void synchronizeAverageRating() {
        reviewRepository.findAll()
                .forEach(review -> redisCacheService.synchronizeAverageRating(
                        review.getProduct().getId(),
                        AVERAGE_RATE_CACHE_KEY + review.getProduct().getId()
                ));
    }

    @Scheduled(cron = "0 0 * * 1 *")
    public void synchronizeNumberOfReview() {
        reviewRepository.findAll()
                .forEach(
                        review -> redisCacheService.synchronizeNumberOfReview(
                                review.getProduct().getId(),
                                REVIEW_COUNT_CACHE_KEY + review.getProduct().getId()
                        )
                );
    }
}
