package project.trendpick_pro.domain.tag.entity.dto.response;

import lombok.Getter;

@Getter
public class TagResponse {

    String tag;

    public TagResponse(String tag) {
        this.tag = tag;
    }
}
