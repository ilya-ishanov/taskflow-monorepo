package taskflow.mongo.utils;


import org.bson.types.ObjectId;
import taskflow.exceptions.BadRequestException;

public class MongoIdUtils {
    public static ObjectId parse(String id) {
        try {
            return new ObjectId(id);
        }
        catch (IllegalArgumentException e) {
            throw new BadRequestException("Неверный формат ObjectId: " + id);
        }
    }
}
