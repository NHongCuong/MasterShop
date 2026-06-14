package com.sportshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralImageDTO {
    private Long id;
    private String imageName;
    private String imageUrl;
    private Date createdAt;
    private Date updatedAt;
}
