package project.trendpick_pro.domain.product.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.trendpick_pro.domain.ask.service.impl.AskServiceImpl;
import project.trendpick_pro.domain.brand.service.BrandService;
import project.trendpick_pro.domain.category.service.MainCategoryService;
import project.trendpick_pro.domain.category.service.SubCategoryService;
import project.trendpick_pro.domain.review.service.impl.ReviewServiceImpl;
import project.trendpick_pro.global.basedata.tagname.service.impl.TagNameServiceImpl;
import project.trendpick_pro.global.util.rq.Rq;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.domain.product.entity.dto.ProductRequest;
import project.trendpick_pro.domain.product.entity.product.dto.response.ProductListResponse;
import project.trendpick_pro.domain.product.entity.product.dto.response.ProductListResponseBySeller;
import project.trendpick_pro.domain.product.service.ProductService;
import project.trendpick_pro.domain.recommend.service.RecommendService;
import project.trendpick_pro.global.util.rsData.RsData;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("trendpick/products")
public class ProductController {

    private final ProductService productService;
    private final RecommendService recommendService;
    private final MemberService memberService;

    private final TagNameServiceImpl tagNameServiceImpl;
    private final BrandService brandService;

    private final MainCategoryService mainCategoryService;
    private final SubCategoryService subCategoryService;

    private final ReviewServiceImpl reviewService;
    private final AskServiceImpl askService;

    private final Rq rq;

    @Value("${colors}")
    private List<String> colors;
    @Value("${sizes.tops}")
    private List<Integer> tops;

    @Value("${sizes.bottoms}")
    private List<Integer> bottoms;

    @Value("${sizes.shoes}")
    private List<Integer> shoes;

    @PreAuthorize("hasAuthority({'ADMIN', 'BRAND_ADMIN'})")
    @GetMapping("/register")
    public String registerProduct(@ModelAttribute("ProductRequest") ProductRequest productRequest, Model model) {
        readyHtml(model);
        return "trendpick/products/register";
    }

    @PreAuthorize("hasAuthority({'ADMIN', 'BRAND_ADMIN'})")
    @PostMapping("/register")
    public String register(@ModelAttribute @Valid ProductRequest productRequest,
                           @RequestParam("mainFile") MultipartFile mainFile,
                           @RequestParam("subFiles") List<MultipartFile> subFiles) throws IOException, ExecutionException, InterruptedException {
        RsData<Long> id = productService.register(productRequest, mainFile, subFiles);
        return rq.redirectWithMsg("/trendpick/products/" + id.getData(), id.getMsg());
    }

    @GetMapping("/edit/{productId}")
    public String modifyBefore(@PathVariable Long productId, @ModelAttribute("ProductRequest") ProductRequest productRequest, Model model) {
        readyHtml(model);
        model.addAttribute("originProduct", productService.findById(productId));

        return "trendpick/products/modify";
    }

    @PreAuthorize("hasAuthority({'ADMIN', 'BRAND_ADMIN'})")
    @PostMapping("/edit/{productId}")
    public String modifyProduct(@PathVariable Long productId, @Valid ProductRequest productRequest, @RequestParam("mainFile") MultipartFile mainFile,
                                @RequestParam("subFiles") List<MultipartFile> subFiles) throws IOException {
        RsData<Long> id = productService.modify(productId, productRequest, mainFile, subFiles);
        return rq.redirectWithMsg("/trendpick/products/" + id.getData(), id.getMsg());
    }

    @PreAuthorize("hasAuthority({'ADMIN', 'BRAND_ADMIN'})")
    @DeleteMapping("/{productId}")
    public String deleteProduct(@PathVariable Long productId) {
        productService.delete(productId);
        String category = URLEncoder.encode("상의", StandardCharsets.UTF_8);
        return "redirect:/trendpick/products/list?main-category=" + category;

    }

    @PreAuthorize("permitAll()")
    @GetMapping("/{productId}")
    public String showProduct(@PathVariable Long productId, Pageable pageable, Model model) {
        model.addAttribute("productResponse", productService.getProduct(productId));
        model.addAttribute("productReview", reviewService.getReviews(productId, pageable));
        model.addAttribute("productAsk", askService.findAsksByProduct(productId, 0));
        model.addAttribute("reviewCount", reviewService.findTotalReviewsByProduct(productId));
        model.addAttribute("reviewAverageRating", reviewService.findAverageRatingByProduct(productId));

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

    @PreAuthorize("hasAuthority({'ADMIN', 'BRAND_ADMIN'})")
    @GetMapping("/admin/list")
    public String showAllProductBySeller(@RequestParam("page") int offset, Model model) {
        Page<ProductListResponseBySeller> products =
                productService.findProductsBySeller(rq.getAdmin(), offset).getData();
        model.addAttribute("products", products);
        return "trendpick/admin/products";
    }

    @GetMapping("/keyword")
    public String searchQuery(@RequestParam String keyword, @RequestParam(value = "page", defaultValue = "0") int offset,  Model model) {
        Page<ProductListResponse> products = productService.findAllByKeyword(keyword, offset);
        model.addAttribute("keyword", keyword);
        model.addAttribute("productResponses", products);
        return "trendpick/products/list";
    }

    @PostMapping("/admin/discount/{productId}")
    public String applyDiscount(@PathVariable Long productId, @RequestParam double discountRate, Model model) {
        productService.applyDiscount(productId, discountRate);
        return "redirect:/trendpick/products/admin/list?page=0";
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
