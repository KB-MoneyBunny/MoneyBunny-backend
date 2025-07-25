package org.scoula.codef.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.codef.domain.UserCardVO;

import java.util.List;

public interface UserCardMapper {
    // 1. 카드 등록
    int insertUserCard(UserCardVO card);

    // 2. 카드 PK(ID) 찾기
    Long findIdByUserIdAndCardNumber(@Param("userId") Long userId, @Param("cardMaskedNumber") String cardMaskedNumber);

    // 3. 중복 카드 존재 여부
    int existsCard(@Param("userId") Long userId, @Param("cardMaskedNumber") String cardMaskedNumber);

    // (선택) 카드 리스트 전체 조회
    List<UserCardVO> findUserCards(Long userId);
}
