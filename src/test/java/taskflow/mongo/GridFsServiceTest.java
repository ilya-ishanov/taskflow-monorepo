package taskflow.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
public class GridFsServiceTest {
    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Test
    void upload_validFile_storesSuccessfully() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Test content".getBytes());
        DBObject metadata = new BasicDBObject();
        metadata.put("taskId", 1L);

        ObjectId fileId = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(),
                file.getContentType(), metadata);

        assertThat(fileId).isNotNull();
    }

    @Test
    void delete_byId_removesFileAndMetadata() throws IOException {
        // 1. Загружаем файл
        MockMultipartFile file = new MockMultipartFile(
                "file", "to-delete.txt", "text/plain", "To be deleted".getBytes());
        DBObject metadata = new BasicDBObject();
        metadata.put("taskId", 2L);

        ObjectId fileId = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(),
                file.getContentType(), metadata);

        // 2. Удаляем по _id
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(fileId)));

        // 3. Пытаемся найти снова
        GridFsResource resource = gridFsTemplate.getResource(fileId.toHexString());

        // 4. Проверяем, что файл не существует (не найден)
        assertThat(resource.exists()).isFalse();
    }
}
