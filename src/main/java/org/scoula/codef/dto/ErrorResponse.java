package org.scoula.codef.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private boolean success;

    private String code;

    private String message;
}
