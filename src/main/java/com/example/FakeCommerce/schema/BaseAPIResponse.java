package com.example.FakeCommerce.schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BaseAPIResponse {
    
    private String message;
    private Object data;
    private String status;
    private String error;

    public static BaseAPIResponse success(Object data) {
        return BaseAPIResponse.builder()
            .message("Success")
            .data(data)
            .status("success")
            .build();
    }
    
    public static BaseAPIResponse error(String error) {
        return BaseAPIResponse.builder()
            .message("Error")
            .error(error)
            .status("error")
            .build();
    }


}
