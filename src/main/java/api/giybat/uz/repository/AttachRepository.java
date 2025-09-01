package api.giybat.uz.repository;

import api.giybat.uz.entity.AttachEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachRepository extends CrudRepository<AttachEntity, String> {
}
