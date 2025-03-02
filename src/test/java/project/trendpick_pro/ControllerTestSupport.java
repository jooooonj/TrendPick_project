package project.trendpick_pro;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import project.trendpick_pro.domain.answer.service.AnswerService;
import project.trendpick_pro.domain.ask.controller.AskController;
import project.trendpick_pro.domain.ask.service.AskService;
import project.trendpick_pro.domain.coupon.controller.CouponCardController;
import project.trendpick_pro.domain.coupon.controller.CouponController;
import project.trendpick_pro.domain.coupon.service.CouponCardService;
import project.trendpick_pro.domain.coupon.service.CouponService;
import project.trendpick_pro.domain.member.controller.MemberController;
import project.trendpick_pro.domain.member.repository.MemberRepository;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.domain.orders.contoller.OrderController;
import project.trendpick_pro.domain.orders.service.OrderService;
import project.trendpick_pro.domain.product.service.ProductService;
import project.trendpick_pro.global.basedata.tagname.service.TagNameService;
import project.trendpick_pro.global.config.SecurityConfig;
import project.trendpick_pro.global.util.rq.Rq;

@Import(SecurityConfig.class)
@ActiveProfiles("test")
@WebMvcTest(
    controllers = {
        AskController.class,
        CouponCardController.class,
        CouponController.class,
        MemberController.class,
        OrderController.class
    }
)
public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected Rq rq;

    @MockBean
    protected TagNameService tagNameService;

    @MockBean
    protected ProductService productService;

    @MockBean
    protected MemberService memberService;

    @MockBean
    protected MemberRepository memberRepository;

    /**
     * AskControllerTest
     * */
    @MockBean
    protected AskService askService;

    @MockBean
    protected AnswerService answerService;

    /**
     *  CouponControllerTest
     * */
    @MockBean
    protected CouponService couponService;

    /**
     * CouponCardControllerTest
     * */
    @MockBean
    protected CouponCardService couponCardService;

    /**
     * OrderControllerTest
     * */
    @MockBean
    protected OrderService orderService;
}
