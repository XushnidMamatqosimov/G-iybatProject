package api.giybat.uz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "attach")
@Getter
@Setter
public class AttachEntity {
    @Id
    private String id;
    @Column(name = "path")
    private String path;
    @Column(name = "extension")
    private String extension;
    @Column(name = "origin_name")
    private String originalName;
    @Column(name = "size")
    private Long size;
    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();
    private Boolean visible = true;
}
