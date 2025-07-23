package org.scoula.codef.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectedAccountVO {

    private Long id;
    private Long userId;
    private String connectedId;

    // Getter & Setter 생략
}