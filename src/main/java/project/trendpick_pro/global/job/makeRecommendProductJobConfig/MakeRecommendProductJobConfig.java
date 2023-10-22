package project.trendpick_pro.global.job.makeRecommendProductJobConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.entity.MemberRoleType;
import project.trendpick_pro.domain.member.repository.MemberRepository;
import project.trendpick_pro.domain.product.entity.product.Product;
import project.trendpick_pro.domain.product.entity.product.dto.response.ProductByRecommended;
import project.trendpick_pro.domain.product.exception.ProductNotFoundException;
import project.trendpick_pro.domain.product.repository.ProductRepository;
import project.trendpick_pro.domain.product.service.ProductService;
import project.trendpick_pro.domain.recommend.entity.Recommend;
import project.trendpick_pro.domain.recommend.repository.JdbcRecommendRepository;
import project.trendpick_pro.domain.recommend.repository.RecommendRepository;
import project.trendpick_pro.domain.tags.favoritetag.entity.FavoriteTag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MakeRecommendProductJobConfig {
    private final MemberRepository memberRepository;
    private final JdbcRecommendRepository jdbcRecommendRepository;
    private final RecommendRepository recommendRepository;

    @Bean
    public Job makeRecommendProductJob(JobRepository jobRepository, Step makeRecommendProductStep1) {
        return new JobBuilder("makeRecommendProductJob", jobRepository)
                .start(makeRecommendProductStep1)
                .build();
    }

    @Bean
    @JobScope
    public Step makeRecommendProductStep1(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Member> memberReader,
            ItemProcessor<Member, List<Recommend>> memberToRecommendsProcessor
    ) {
        return new StepBuilder("makeRecommendProductStep1", jobRepository)
                .<Member, List<Recommend>>chunk(10, transactionManager)
                .reader(memberReader)
                .processor(memberToRecommendsProcessor)
                .writer(new ItemWriter<List<Recommend>>() {
                    @Override
                    public void write(Chunk<? extends List<Recommend>> chunk) throws Exception {
                        for (List<Recommend> recommends : chunk.getItems()) {
                            jdbcRecommendRepository.batchInsert(recommends);
                        }
                    }
                })
                .build();
    }

    @StepScope
    @Bean
    public RepositoryItemReader<Member> memberReader(
            @Value("#{jobParameters['fromDate']}") LocalDateTime fromDate,
            @Value("#{jobParameters['toDate']}") LocalDateTime toDate
    ) {


        return new RepositoryItemReaderBuilder<Member>()
                .name("memberReader")
                .repository(memberRepository)
                .methodName("findAllByRoleAndRecentlyAccessDateBetween")
                .pageSize(10)
                .arguments(Arrays.asList(MemberRoleType.MEMBER, fromDate, toDate))
                .sorts(Collections.singletonMap("id", Sort.Direction.DESC))
                .build();
    }

    @StepScope
    @Bean
    public ItemProcessor<Member, List<Recommend>> memberToRecommendsProcessor(ProductRepository productRepository) {

        return new ItemProcessor<Member, List<Recommend>>() {
            @Override
            public List<Recommend> process(Member member) throws Exception {
                recommendRepository.deleteAllByMemberEmail(member.getEmail());

                List<ProductByRecommended> tags = productRepository.findRecommendProduct(member.getEmail());
                Set<FavoriteTag> memberTags = member.getTags();

                Map<String, List<Long>> productIdListByTagName = classifyProductsByTag(tags);
                Map<Long, ProductByRecommended> recommendProductByProductId = classifyTagsByProductId(tags);

                updateMemberTagScores(memberTags, productIdListByTagName, recommendProductByProductId);

                List<ProductByRecommended> recommendProductList = sortRecommendProductsByScore(recommendProductByProductId);

                List<Recommend> recommends = convertProductToRecommend(member, recommendProductList, productRepository);

                return recommends;
            }
        };
    }

    //productRecommend -> Recommend
    private  List<Recommend> convertProductToRecommend(Member member, List<ProductByRecommended> recommendProductList, ProductRepository productRepository) {
        List<Product> products = new ArrayList<>();
        for (ProductByRecommended recommendProduct : recommendProductList) {
            products.add(productRepository.findById(recommendProduct.getProductId()).orElseThrow(
                    () -> new ProductNotFoundException("존재하지 않는 상품입니다.")
            ));
        }

        List<Recommend> recommends = new ArrayList<>();
        for (Product product : products) {
            Recommend recommend = Recommend.of(product);
            recommend.connectMember(member);
            recommends.add(recommend);
        }
        return recommends;
    }

    //점수를 기준으로 추천 상품을 순위별로 정렬하여 반환
    private  List<ProductByRecommended> sortRecommendProductsByScore(Map<Long, ProductByRecommended> recommendProductByProductId) {
        return new ArrayList<>(recommendProductByProductId.values()).stream()
                .sorted(Comparator.comparing(ProductByRecommended::getTotalScore).reversed())
                .toList();
    }

    //우리가 반환하려고 하는 추천상품이 점수가 몇점인지 갱신하는 코드이다.
    private static void updateMemberTagScores(Set<FavoriteTag> memberTags, Map<String, List<Long>> productIdListByTagName, Map<Long, ProductByRecommended> recommendProductByProductId) {
        for (FavoriteTag memberTag : memberTags) {
            if (productIdListByTagName.containsKey(memberTag.getName())) {
                List<Long> productIdList = productIdListByTagName.get(memberTag.getName());
                for (Long id : productIdList) {
                    recommendProductByProductId.get(id).plusTotalScore(memberTag.getScore());
                }
            }
        }
    }

    //마찬가지로 같은 상품을 가르키고 있지만 태그명은 제각각일 것이다. 우리가 뽑아내길 원하는 것은 추천상품이다. 즉 같은 상품이 중복되면 안된다.
    //그래서 상품Id에 대한 중복을 없애서 하나에 몰아넣는 코드이다.
    private static Map<Long, ProductByRecommended> classifyTagsByProductId(List<ProductByRecommended> tags) {
        Map<Long, ProductByRecommended> recommendProductByProductId = new HashMap<>();
        for (ProductByRecommended response : tags) {
            if (recommendProductByProductId.containsKey(response.getProductId()))
                continue;
            recommendProductByProductId.put(response.getProductId(), response);
        }
        return recommendProductByProductId;
    }

    //태그명에 따라 가지고 있는 product_id
    // : 멤버 태그명에 따라 해당 상품에 점수를 부여해야 하기 때문에
    private Map<String, List<Long>> classifyProductsByTag(List<ProductByRecommended> tags) {
        Map<String, List<Long>> productIdListByTagName = new HashMap<>();
        for (ProductByRecommended tag : tags) {
            if (!productIdListByTagName.containsKey(tag.getTagName())) {
                productIdListByTagName.put(tag.getTagName(), new ArrayList<>());
            }
            productIdListByTagName.get(tag.getTagName()).add(tag.getProductId());
        }
        return productIdListByTagName;
    }
}
