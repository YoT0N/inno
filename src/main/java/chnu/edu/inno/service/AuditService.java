package chnu.edu.inno.service;

import chnu.edu.inno.exception.ApiException;
import chnu.edu.inno.exception.InvalidRequestException;
import chnu.edu.inno.model.Item;
import chnu.edu.inno.model.dto.response.ApiResponse;
import chnu.edu.inno.model.dto.response.BaseMetaData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.javers.core.Javers;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.Change;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.javers.shadow.Shadow;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@AllArgsConstructor
public class AuditService {

    private static final int MAX_LIMIT = 500;

    private final Javers javers;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public record AuditShadowDto(String commitId, String author, LocalDateTime commitDate, Item item) {
    }

    // Snapshots
    public ApiResponse<BaseMetaData, Object> getSnapshots(String itemId, int limit) {
        List<CdoSnapshot> snapshots = javers.findSnapshots(buildQuery(itemId, limit));
        return toResponse(itemId, toPlainList(snapshots));
    }

    // Changes
    public ApiResponse<BaseMetaData, Object> getChanges(String itemId, int limit) {
        List<Change> changes = javers.findChanges(buildQuery(itemId, limit));
        return toResponse(itemId, toPlainList(changes));
    }

    // Shadows
    public ApiResponse<BaseMetaData, AuditShadowDto> getShadows(String itemId, int limit) {
        List<Shadow<Item>> shadows = javers.findShadows(buildQuery(itemId, limit));
        List<AuditShadowDto> dtos = shadows.stream().map(s -> {
            CommitMetadata cm = s.getCommitMetadata();
            return new AuditShadowDto(String.valueOf(cm.getId()), cm.getAuthor(), cm.getCommitDate(), s.get());
        }).toList();
        return toResponse(itemId, dtos);
    }

    // helpers
    private JqlQuery buildQuery(String itemId, int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new InvalidRequestException("Parameter 'limit' must be between 1 and " + MAX_LIMIT + ", got: " + limit);
        }
        QueryBuilder qb;
        if (itemId == null) {
            qb = QueryBuilder.byClass(Item.class);
        } else {
            if (itemId.isBlank() || !ObjectId.isValid(itemId)) {
                throw new InvalidRequestException("Invalid item id format: '" + itemId + "'");
            }
            qb = QueryBuilder.byInstanceId(itemId, Item.class);
        }
        return qb.limit(limit).build();
    }

    private List<Object> toPlainList(Object javersResult) {
        try {
            String json = javers.getJsonConverter().toJson(javersResult);
            return objectMapper.readValue(json, new TypeReference<List<Object>>() { });
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot convert audit data to JSON");
        }
    }

    private <D> ApiResponse<BaseMetaData, D> toResponse(String itemId, List<D> data) {
        // для конкретного item порожня історія = такого запису ніколи не було
        if (itemId != null && data.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "No audit records found for item '" + itemId + "'");
        }
        return ApiResponse.<BaseMetaData, D>builder()
                .meta(new BaseMetaData(200, true))
                .data(data)
                .build();
    }
}