package chnu.edu.inno.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Item extends AuditMetadata {
    @Id
    private String id;
    private String name;
    private String code;
    private int quantity;

    public Item(String name, String code, int quantity) {
        this.name = name;
        this.code = code;
        this.quantity = quantity;
    }

    // null-safe: раніше getId().equals(...) падало з NPE для об'єктів без id
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item item)) return false;
        return id != null && id.equals(item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}