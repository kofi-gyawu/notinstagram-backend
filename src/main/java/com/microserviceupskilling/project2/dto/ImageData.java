package com.microserviceupskilling.project2.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageData {
    public String id;
    public String url;
    public String owner;
    public String status;
}
