package chnu.edu.inno.controller;

import chnu.edu.inno.model.Item;
import chnu.edu.inno.model.dto.response.ApiResponse;
import chnu.edu.inno.model.dto.response.BaseMetaData;
import chnu.edu.inno.model.dto.response.PaginationMetaData;
import chnu.edu.inno.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BaseMetaData, Item>> getItemById(@PathVariable String id) {
        return toResponse(itemService.getItemByIdAsApiResponse(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<BaseMetaData, Item>> getAllItems() {
        return toResponse(itemService.getAllItemsAsApiResponse());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BaseMetaData, Item>> createItem(@RequestBody Item item) {
        return toResponse(itemService.createItemAsApiResponse(item));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<BaseMetaData, Item>> updateItem(@RequestBody Item item) {
        return toResponse(itemService.updateItemAsApiResponse(item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<BaseMetaData, Void>> deleteItem(@PathVariable String id) {
        return toResponse(itemService.deleteItemByIdAsApiResponse(id));
    }

    //----------------  More ---------------------------

    @GetMapping("/response/{id}")
    public ResponseEntity<ApiResponse<BaseMetaData, Item>> getItemByIdAsApiResponse(@PathVariable String id) {
        return toResponse(itemService.getItemByIdAsApiResponse(id));
    }

    @GetMapping("/page")
    @Operation(
            method = "GET",
            summary = "Get  ALL items, ASC, PAGEABLE. Default 5 items per page",
            description = "Fetch a PAGE of items  by page number and size "
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Success. Return page of items",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            type = "object",
                                            properties = {
                                                    @StringToClassMapItem(key = "meta", value = PaginationMetaData.class),
                                                    @StringToClassMapItem(key = "data", value = Item.class)
                                            }
                                    )
                            )
                    }
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid 'page' (negative / not a number) or 'size' (not in 1..100)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Requested page does not exist")
    })
    public ResponseEntity<ApiResponse<PaginationMetaData, Item>> getItemsPage(
            @Parameter(description = "Request parameter. Page of paginated result. Cannot be less than zero")
            @RequestParam(name = "page", defaultValue = "0", required = false) Integer page,
            @Parameter(description = "Request parameter. Size of paginated page result. Must be between 1 and 100")
            @RequestParam(name = "size", defaultValue = "5", required = false) Integer size
    ) {
        return toResponse(itemService.getPage(page, size));
    }

    // HTTP-статус відповіді береться з meta.code, тож тіло і статус завжди узгоджені
    private static <M extends BaseMetaData, D> ResponseEntity<ApiResponse<M, D>> toResponse(ApiResponse<M, D> response) {
        return ResponseEntity.status(response.getMeta().getCode()).body(response);
    }
}