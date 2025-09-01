package api.giybat.uz.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AttachDTO {
    private String id;
    private String originName;
    private String extension;
    private Long size;
    private LocalDateTime createdDate;
    private String url;
}
