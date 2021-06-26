package com.tamir.followear.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@ToString
public class LambdaErrorDTO {

    private String errorMessage;
    private String errorType;
    private List<String> stackTrace;

}
