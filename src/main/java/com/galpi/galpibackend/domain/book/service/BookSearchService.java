package com.galpi.galpibackend.domain.book.service;

import com.galpi.galpibackend.domain.book.client.KakaoBookClient;
import com.galpi.galpibackend.domain.book.client.KakaoBookResponse;
import com.galpi.galpibackend.domain.book.dto.BookSearchResponse;
import com.galpi.galpibackend.domain.book.dto.BookSearchResponse.BookItem;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BookSearchService {

    private final KakaoBookClient kakaoBookClient;

    public BookSearchService(KakaoBookClient kakaoBookClient) {
        this.kakaoBookClient = kakaoBookClient;
    }

    public BookSearchResponse search(String query, int page, int size) {
        // 우리 API는 0-based, 카카오는 1-based
        KakaoBookResponse kakaoResponse = kakaoBookClient.search(query, page + 1, size);

        List<BookItem> items = kakaoResponse.documents().stream()
                .map(this::toBookItem)
                .toList();

        boolean hasNext = !kakaoResponse.meta().isEnd();
        return new BookSearchResponse(items, page, hasNext);
    }

    private BookItem toBookItem(KakaoBookResponse.Document doc) {
        return new BookItem(
                doc.title(),
                joinAuthors(doc.authors()),
                doc.publisher(),
                doc.thumbnail(),
                extractIsbn13(doc.isbn())
        );
    }

    private String joinAuthors(List<String> authors) {
        if (authors == null || authors.isEmpty()) {
            return "";
        }
        return String.join(", ", authors);
    }

    /**
     * 카카오 isbn 필드는 "isbn10 isbn13" 형태로 공백 구분되어 올 수 있다.
     * 13자리(있으면)를 우선 사용한다.
     */
    private String extractIsbn13(String isbn) {
        if (!StringUtils.hasText(isbn)) {
            return "";
        }
        String[] parts = isbn.trim().split("\\s+");
        for (String part : parts) {
            if (part.length() == 13) {
                return part;
            }
        }
        return parts[parts.length - 1];
    }
}
