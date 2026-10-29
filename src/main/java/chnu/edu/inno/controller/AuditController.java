package chnu.edu.inno.controller;

import chnu.edu.inno.model.dto.response.ApiResponse;
import chnu.edu.inno.model.dto.response.BaseMetaData;
import chnu.edu.inno.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/audit/items")
public class AuditController {

    private final AuditService auditService;

    // ---------- по всіх Item ----------

    @GetMapping("/snapshots")
    @Operation(summary = "Audit by ALL items: snapshots")
    public ResponseEntity<ApiResponse<BaseMetaData, Object>> snapshotsAll(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(auditService.getSnapshots(null, limit));
    }

    @GetMapping("/changes")
    @Operation(summary = "Changes of ALL items")
    public ResponseEntity<ApiResponse<BaseMetaData, Object>> changesAll(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(auditService.getChanges(null, limit));
    }

    @GetMapping("/shadows")
    @Operation(summary = "Shadows of ALL items")
    public ResponseEntity<ApiResponse<BaseMetaData, AuditService.AuditShadowDto>> shadowsAll(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(auditService.getShadows(null, limit));
    }

    // ---------- по одному Item ----------

    @GetMapping("/{id}/snapshots")
    @Operation(summary = "Audit by item id: snapshots")
    public ResponseEntity<ApiResponse<BaseMetaData, Object>> snapshotsById(
            @PathVariable String id, @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(auditService.getSnapshots(id, limit));
    }

    @GetMapping("/{id}/changes")
    @Operation(summary = "Changes of ONE item")
    public ResponseEntity<ApiResponse<BaseMetaData, Object>> changesById(
            @PathVariable String id, @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(auditService.getChanges(id, limit));
    }

    @GetMapping("/{id}/shadows")
    @Operation(summary = "Shadows by item id")
    public ResponseEntity<ApiResponse<BaseMetaData, AuditService.AuditShadowDto>> shadowsById(
            @PathVariable String id, @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(auditService.getShadows(id, limit));
    }
}