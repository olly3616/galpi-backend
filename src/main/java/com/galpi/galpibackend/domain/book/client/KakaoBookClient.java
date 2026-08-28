package com.galpi.galpibackend.domain.book.client;

import com.galpi.galpibackend.global.config.KakaoProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KakaoBookClient {

    private static final String BASE_URL = "https://dapi.kakao.com";
    private static final String SEARCH_PATH = "/v3/search/book";

    private final RestClient restClient;
    private final String restApiKey;

    public KakaoBookClient(KakaoProperties kakaoProperties) {
        this.restClient = RestClient.create(BASE_URL);
        this.restApiKey = kakaoProperties.restApiKey();
    }

    /**
     * @param query 검색어
     * @param page  카카오 기준 1-based 페이지 (1~50)
     * @param size  페이지당 결과 수 (1~50)
     */
    public KakaoBookResponse search(String query, int page, int size) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path(SEARCH_PATH)
                        .queryParam("query", query)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .header("Authorization", "KakaoAK " + restApiKey)
                .retrieve()
                .body(KakaoBookResponse.class);
    }
}
