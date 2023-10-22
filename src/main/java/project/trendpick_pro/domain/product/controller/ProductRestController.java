package project.trendpick_pro.domain.product.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.trendpick_pro.domain.ask.service.AskService;
import project.trendpick_pro.domain.brand.service.BrandService;
import project.trendpick_pro.domain.category.service.MainCategoryService;
import project.trendpick_pro.domain.category.service.SubCategoryService;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.domain.product.entity.product.dto.response.ProductListResponse;
import project.trendpick_pro.domain.product.service.ProductService;
import project.trendpick_pro.domain.recommend.service.RecommendService;
import project.trendpick_pro.domain.review.service.ReviewService;
import project.trendpick_pro.global.basedata.tagname.service.impl.TagNameServiceImpl;
import project.trendpick_pro.global.util.rq.Rq;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/trendpick/products")
public class ProductRestController {
    private final ProductService productService;
    private final RecommendService recommendService;
    private final MemberService memberService;

    private final TagNameServiceImpl tagNameServiceImpl;
    private final BrandService brandService;

    private final MainCategoryService mainCategoryService;
    private final SubCategoryService subCategoryService;

    private final ReviewService reviewService;
    private final AskService askService;

    private final Rq rq;

    @Value("${colors}")
    private List<String> colors;
    @Value("${sizes.tops}")
    private List<Integer> tops;

    @Value("${sizes.bottoms}")
    private List<Integer> bottoms;

    @Value("${sizes.shoes}")
    private List<Integer> shoes;

    @PreAuthorize("permitAll()")
    @GetMapping("/{productId}")
    public String showProduct(@PathVariable Long productId, Pageable pageable, Model model) {
        model.addAttribute("productResponse", productService.getProduct(productId));
        model.addAttribute("productReview", reviewService.getReviews(productId, pageable));
        model.addAttribute("productAsk", askService.findAsksByProduct(productId, 0));
        return "trendpick/products/detailpage";
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/list")
    public String showAllProduct(@RequestParam(value = "page", defaultValue = "0") int offset,
                                 @RequestParam(value = "main-category", defaultValue = "all") String mainCategory,
                                 @RequestParam(value = "sub-category", defaultValue = "전체") String subCategory,
                                 Pageable pageable, Model model, HttpSession session) {
        if (mainCategory.equals("recommend")) {
            mainCategory = "추천";
        } else if (mainCategory.equals("all")) {
            mainCategory = "전체";
        }
        if (mainCategory.equals("추천")) {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<Member> member = memberService.findByEmail(username);
            if (member.isEmpty()) {
                model.addAttribute("subCategoryName", "전체");
                model.addAttribute("mainCategoryName", "전체");
                model.addAttribute("productResponses", productService.getProducts(offset, mainCategory, subCategory));
                model.addAttribute("subCategories", subCategoryService.findAll(mainCategory));
            } else {
                model.addAttribute("subCategoryName", "전체");
                model.addAttribute("mainCategoryName", mainCategory);
                model.addAttribute("productResponses", recommendService.getFindAll(member.get(), offset));
                model.addAttribute("subCategories", subCategoryService.findAll(mainCategory));
            }
        } else if(mainCategory.equals("전체")){
            model.addAttribute("subCategoryName", subCategory);
            model.addAttribute("mainCategoryName", mainCategory);
            model.addAttribute("productResponses", productService.getProducts(offset, mainCategory, subCategory));
        } else {
            model.addAttribute("subCategoryName", subCategory);
            model.addAttribute("mainCategoryName", mainCategory);
            model.addAttribute("productResponses", productService.getProducts(offset, mainCategory, subCategory));
            model.addAttribute("subCategories", subCategoryService.findAll(mainCategory));
        }
        return "trendpick/products/list";
    }

    @GetMapping("/keyword")
    public String searchQuery(@RequestParam String keyword, @RequestParam(value = "page", defaultValue = "0") int offset,  Model model) {
        Page<ProductListResponse> products = productService.findAllByKeyword(keyword, offset);
        model.addAttribute("keyword", keyword);
        model.addAttribute("productResponses", products);
        return "trendpick/products/list";
    }

    private void readyHtml(Model model) {
        model.addAttribute("tags", tagNameServiceImpl.findAll());

        List<String> MainCategories = mainCategoryService.findAll();
        model.addAttribute("mainCategories", MainCategories);

        Map<String, List<String>> subCategoryList = new HashMap<>();
        for (String mainCategory : MainCategories) {
            subCategoryList.put(mainCategory, subCategoryService.findAll(mainCategory));
        }

        model.addAttribute("subCategoriesList", subCategoryList);
        model.addAttribute("brands", brandService.findByName(rq.getAdmin().getBrand()));
        model.addAttribute("colors", colors);
        model.addAttribute("tops", tops);
        model.addAttribute("bottoms", bottoms);
        model.addAttribute("shoes", shoes);
    }
}
