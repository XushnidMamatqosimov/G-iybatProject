package api.giybat.uz.service;

import api.giybat.uz.dto.AttachDTO;
import api.giybat.uz.entity.AttachEntity;
import api.giybat.uz.repository.AttachRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

@Service
public class AttachService {

    @Autowired
    private AttachRepository attachRepository;
    @Value("${attach.upload.folder}")
    private String folderName;
    @Value("${attach.url}")
    private String attachUrl;

    public AttachDTO upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        try {
            String pathFolder = getYmDString();
            String key = UUID.randomUUID().toString();
            String extension = getExtension(Objects.requireNonNull(file.getOriginalFilename()));

            // create folder if not exists
            File folder = new File(folderName + "/" + pathFolder);
            if (!folder.exists()) {
                boolean t = folder.mkdirs();
            }

            // save to system
            byte[] bytes = file.getBytes();
            Path path = Paths.get(folderName + "/" + pathFolder + "/" + key + "." + extension);
            Files.write(path, bytes);

            // save to db
            AttachEntity attachEntity = new AttachEntity();
            attachEntity.setId(key);
            attachEntity.setPath(path.toString());
            attachEntity.setSize(file.getSize());
            attachEntity.setOriginalName(file.getOriginalFilename());
            attachEntity.setExtension(extension);
            attachEntity.setVisible(true);
            attachRepository.save(attachEntity);


            return toDTO(attachEntity);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

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
        //dto.setUrl(at);
        return dto;
    }
}
