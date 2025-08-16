package org.scoula.guest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.scoula.userPolicy.dto.SearchRequestDTO;
import org.scoula.userPolicy.dto.SearchResultDTO;
import org.scoula.userPolicy.service.UserPolicyService;
import org.scoula.guest.service.GuestPolicyService; // New import
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guestPolicy")
@RequiredArgsConstructor
@Api(
        tags = "비로그인 정책 조회 API",
        description = "비로그인 사용자를 위한 정책 조회 API",
        value = "GuestPolicyController"
)
public class GuestPolicyController {

    private final UserPolicyService userPolicyService;
    private final GuestPolicyService guestPolicyService; // New injection

    /**
     * 조건 없이 is_financial_support=1 정책을 조회수 순으로 topN 반환
     * GET: /api/guestPolicy/search/top-views/all?count=5
     */
    @ApiOperation(value = "조건 없이 is_financial_support=1 정책을 조회수 순으로 topN 반환", notes = "조건 없이 조회수 기준 상위 N개 정책 반환")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 요청이 처리되었습니다.", response = SearchResultDTO.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "서버에서 오류가 발생했습니다.")
    })
    @GetMapping("/search/top-views/all")
    public ResponseEntity<List<SearchResultDTO>> searchTopPoliciesByViewsAll(@RequestParam(defaultValue = "1000") int count) {
        List<SearchResultDTO> result = userPolicyService.searchTopPoliciesByViewsAll(count);
        if (result == null || result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 인기 검색어 조회 API
     * GET: /api/guestPolicy/popular-keywords
     */
    @ApiOperation(value = "인기 검색어 조회", notes = "인기 검색어 목록을 조회하는 API")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 요청이 처리되었습니다.", response = String.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "서버에서 오류가 발생했습니다.")
    })
    @GetMapping("/popular-keywords")
    public ResponseEntity<List<String>> getPopularKeywords() {
        List<String> popularKeywords = userPolicyService.getPopularKeywords(10);
        return ResponseEntity.ok(popularKeywords);
    }

    /**
     * 사용자 정책 조건에 맞는 정책 검색 API(검색 조건 포함, 비로그인)
     * POST: /api/guestPolicy/search
     */
    @ApiOperation(value = "사용자 정책 조건에 맞는 정책 검색(검색 조건 포함, 비로그인)", notes = "비로그인 사용자의 정책 조건에 맞는 정책을 검색하는 API")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "성공적으로 요청이 처리되었습니다.", response = SearchResultDTO.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "잘못된 요청입니다."),
            @ApiResponse(code = 500, message = "서버에서 오류가 발생했습니다.")
    })
    @PostMapping("/search")
    public ResponseEntity<List<SearchResultDTO>> searchFilteredPolicy(@RequestBody SearchRequestDTO searchRequestDTO) {
        // 비로그인 사용자는 username 없이 검색
        List<SearchResultDTO> searchResultDTO = guestPolicyService.searchGuestPolicies(searchRequestDTO);

        // 검색 결과가 있을 경우에만 검색어 저장 (인기 검색어만, 최근 검색어 저장 X)
        if (searchResultDTO != null && !searchResultDTO.isEmpty()) {
            if (searchRequestDTO.getSearchTexts() != null && !searchRequestDTO.getSearchTexts().isEmpty()) {
                List<String> searchTexts = searchRequestDTO.getSearchTexts();
                String searchText = String.join(" ", searchTexts).trim().replaceAll("\\s+", " ");
                userPolicyService.saveSearchText(searchText); // 인기 검색어용 (ZSet 증가)
            }
        } else {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(searchResultDTO);
    }
}

