package api.giybat.uz.service;

import api.giybat.uz.dto.AttachDTO;
import api.giybat.uz.entity.AttachEntity;
import api.giybat.uz.exp.AppBadException;
import api.giybat.uz.repository.AttachRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class AttachService {

    @Autowired
    private AttachRepository attachRepository;
    @Value("${attach.upload.folder}")
    private String folderName;
    @Value("${attach.url}")
    private String attachUrl;
    @Autowired
    private ResourceBundleService resourceBundleService;

    public AttachDTO upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File not found");
        }
        try {
            String pathFolder = getYmDString();  //2025/09/03
            String key = UUID.randomUUID().toString();
            String extension = getExtension(Objects.requireNonNull(file.getOriginalFilename()));

            // create folder if not exists
            File folder = new File(folderName + "/" + pathFolder);
            if (!folder.exists()) {
                boolean t = folder.mkdirs();
            }

            // save to system
            byte[] bytes = file.getBytes();
            if (bytes == null || bytes.length == 0) {
                throw new AppBadException("Uploaded file is empty");
            }

            Path path = Paths.get(folderName + "/" + pathFolder + "/" + key + "." + extension);
            Files.write(path, bytes);

            // save to db
            AttachEntity attachEntity = new AttachEntity();
            attachEntity.setId(key + "." + extension);
            attachEntity.setPath(pathFolder);
            attachEntity.setSize(file.getSize());
            attachEntity.setOriginalName(file.getOriginalFilename());
            attachEntity.setExtension(extension);
            attachEntity.setVisible(true);
            attachEntity.setCreatedDate(LocalDateTime.now());
            attachRepository.save(attachEntity);

            return toDTO(attachEntity);

        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Something went wrong");

    }

    public ResponseEntity<Resource> open(String id) {
        AttachEntity entity = getEntity(id);
        Path filePath = Paths.get(folderName + "/" + entity.getPath() + "/" + id).normalize();
        Resource resource = null;
        try {
            resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new RuntimeException("File not found: " + id);
            }
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

    }

    private String getYmDString() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int day = Calendar.getInstance().get(Calendar.DATE);
        return year + "/" + month + "/" + day;
    }

    private String getExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        return fileName.substring(lastIndexOf + 1);

    }

    private AttachDTO toDTO(AttachEntity attachEntity) {
        AttachDTO dto = new AttachDTO();
        dto.setId(attachEntity.getId());
        dto.setOriginName(attachEntity.getOriginalName());
        dto.setSize(attachEntity.getSize());
        dto.setExtension(attachEntity.getExtension());
        dto.setCreatedDate(attachEntity.getCreatedDate());
        dto.setUrl(openURL(attachEntity.getId()));
        return dto;
    }

    public AttachEntity getEntity(String id) {
        Optional<AttachEntity> optional = attachRepository.findById(id);
        if (optional.isEmpty()) {
            throw new AppBadException("File not found");
        }
        return optional.get();
    }


    public String openURL(String fileName) {
        return attachUrl + "/open/" + folderName + fileName;
        //  http://localhost:8080/attach/open/c7b35085-33ba-4ff8-a6e4-d1e1f39c783f.jpg

    }

    public boolean delete(String id) {
        AttachEntity attachEntity = getEntity(id);
        attachRepository.delete(id);
        File file = new File(folderName + "/" + attachEntity.getPath()+"/"+id);
        boolean b = false;
        if (file.exists()) {
            b = file.delete();
        }
        return b;

    }

    public AttachDTO attachDTO(String photoId) {
        if (photoId == null) {
            return null;
        }
        AttachDTO dto = new AttachDTO();
        dto.setId(photoId);
        dto.setUrl(openURL(photoId));
        return dto;

    }
}
