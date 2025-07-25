package org.scoula.codef.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.codef.domain.ConnectedAccountVO;

public interface ConnectedAccountMapper {

    // connectedId 조회
    ConnectedAccountVO findConnectedIdByUserId(@Param("userId") Long userId);

    // connectedId 저장
    void insertConnectedAccount(@Param("userId") Long userId, @Param("connectedId") String connectedId);

    Long findIdByLoginId(String loginId);
}
