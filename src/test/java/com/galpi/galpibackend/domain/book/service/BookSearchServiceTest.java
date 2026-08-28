package com.galpi.galpibackend.domain.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.galpi.galpibackend.domain.book.client.KakaoBookClient;
import com.galpi.galpibackend.domain.book.client.KakaoBookResponse;
import com.galpi.galpibackend.domain.book.dto.BookSearchResponse;
import com.galpi.galpibackend.domain.book.dto.BookSearchResponse.BookItem;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookSearchServiceTest {

    @Mock
    private KakaoBookClient kakaoBookClient;

    @InjectMocks
    private BookSearchService bookSearchService;

    @Test
    @DisplayName("카카오 응답을 우리 형식으로 매핑하고, 0-based 페이지를 카카오 1-based로 변환한다")
    void search_mapsAndConvertsPage() {
        KakaoBookResponse.Document doc = new KakaoBookResponse.Document(
                "데미안",
                List.of("헤르만 헤세", "전영애"),
                "민음사",
                "https://cover.example/demian.jpg",
                "8937460440 9788937460449"
        );
        KakaoBookResponse kakaoResponse = new KakaoBookResponse(
                new KakaoBookResponse.Meta(true, 100, 100),
                List.of(doc)
        );
        given(kakaoBookClient.search("데미안", 1, 20)).willReturn(kakaoResponse);

        BookSearchResponse response = bookSearchService.search("데미안", 0, 20);

        // 카카오에는 page+1(=1)로 요청했는지 확인
        verify(kakaoBookClient).search("데미안", 1, 20);

        assertThat(response.page()).isEqualTo(0);
        // meta.is_end=true → 마지막 페이지이므로 hasNext=false
        assertThat(response.hasNext()).isFalse();
        assertThat(response.items()).hasSize(1);

        BookItem item = response.items().get(0);
        assertThat(item.title()).isEqualTo("데미안");
        assertThat(item.author()).isEqualTo("헤르만 헤세, 전영애");
        assertThat(item.publisher()).isEqualTo("민음사");
        assertThat(item.coverUrl()).isEqualTo("https://cover.example/demian.jpg");
        // ISBN은 13자리를 우선 추출
        assertThat(item.isbn()).isEqualTo("9788937460449");
    }

    @Test
    @DisplayName("meta.is_end가 false면 hasNext가 true다")
    void search_hasNextWhenNotEnd() {
        KakaoBookResponse kakaoResponse = new KakaoBookResponse(
                new KakaoBookResponse.Meta(false, 100, 100),
                List.of()
        );
        given(kakaoBookClient.search("소설", 2, 10)).willReturn(kakaoResponse);

        BookSearchResponse response = bookSearchService.search("소설", 1, 10);

        assertThat(response.hasNext()).isTrue();
        assertThat(response.items()).isEmpty();
    }
}
