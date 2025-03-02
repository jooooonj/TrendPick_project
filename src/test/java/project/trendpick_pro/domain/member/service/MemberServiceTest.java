package project.trendpick_pro.domain.member.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import project.trendpick_pro.IntegrationTestSupport;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.entity.MemberRoleType;
import project.trendpick_pro.domain.member.entity.dto.response.MemberInfoResponse;
import project.trendpick_pro.domain.member.entity.form.JoinForm;
import project.trendpick_pro.domain.member.repository.MemberRepository;
import project.trendpick_pro.domain.recommend.repository.RecommendRepository;
import project.trendpick_pro.domain.recommend.service.impl.RecommendServiceImpl;
import project.trendpick_pro.domain.tags.favoritetag.repository.FavoriteTagRepository;
import project.trendpick_pro.domain.tags.tag.entity.dto.request.TagRequest;
import project.trendpick_pro.global.util.rsData.RsData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MemberServiceTest extends IntegrationTestSupport {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RecommendRepository recommendRepository;

    @Autowired
    private FavoriteTagRepository favoriteTagRepository;

    @InjectMocks
    private RecommendServiceImpl recommendService;


    @DisplayName("멤버 객체를 생성한다. 멤버의 타입이 MEMBER 라면, 추천객체를 생성한다.")
    @Test
    void join() throws Exception {//given
    }

    @DisplayName("입력된 선호태그들로 멤버의 선호태그를 수정한다.")
    @Test
    void modifyTag() throws Exception {
    }

    @DisplayName("멤버의 주소를 수정한다.")
    @Test
    void modifyAddress() throws Exception {
    }

}