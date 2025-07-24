package org.scoula.codef.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.codef.domain.UserAccountVO;

public interface UserAccountMapper {
    // 계좌 존재 여부 확인 (중복방지)
    int existsAccount(@Param("userId") Long userId, @Param("accountNumber") String accountNumber);

    // 계좌 정보 저장
    int insertUserAccount(UserAccountVO vo);

    Long findIdByUserIdAndAccountNumber(@Param("userId") Long userId, @Param("accountNumber") String accountNumber);
}