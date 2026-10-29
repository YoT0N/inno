package chnu.edu.inno.service;

import chnu.edu.inno.exception.ApiException;
import chnu.edu.inno.exception.InvalidRequestException;
import chnu.edu.inno.exception.ItemNotFoundException;
import chnu.edu.inno.model.Item;
import chnu.edu.inno.model.dto.response.ApiResponse;
import chnu.edu.inno.model.dto.response.BaseMetaData;
import chnu.edu.inno.model.dto.response.PaginationMetaData;
import chnu.edu.inno.repository.ItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ItemService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ItemRepository itemRepository;

    @PostConstruct
    public void init() {
        itemRepository.deleteAll();
        List<Item> items = List.of(
                new Item("Milk", "000001", 1),
                new Item("Bread", "00002", 2),
                new Item("Sugar", "00003", 3),
                new Item("Salt", "00004", 1),
                new Item("Meat", "00005", 1),
                new Item("Chips", "00006", 3),
                new Item("Coca-Cola", "00007", 1),
                new Item("Hot dog", "00008", 1)
        );
        itemRepository.saveAll(items);
    }

    // ===================== Базові методи (кидають винятки) =====================

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Item getItemById(String id) {
        validateId(id);
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }

    public Item createItem(Item item) {
        validateItem(item);
        if (itemRepository.existsByCode(item.getCode())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Item with code '" + item.getCode() + "' already exists");
        }
        item.setId(null); // id генерує MongoDB, щоб випадково не перезаписати чужий запис
        return itemRepository.save(item);
    }

    public Item updateItem(Item item) {
        validateItem(item);
        validateId(item.getId());
        if (!itemRepository.existsById(item.getId())) {
            throw new ItemNotFoundException(item.getId());
        }
        return itemRepository.save(item);
    }

    public void deleteItemById(String id) {
        validateId(id);
        if (!itemRepository.existsById(id)) {
            throw new ItemNotFoundException(id);
        }
        itemRepository.deleteById(id);
    }

    // ===================== Методи, що повертають ApiResponse =====================

    public ApiResponse<BaseMetaData, Item> getItemByIdAsApiResponse(String id) {
        return new ApiResponse<>(new BaseMetaData(200, true), getItemById(id));
    }

    public ApiResponse<BaseMetaData, Item> getAllItemsAsApiResponse() {
        // Порожня колекція - це не помилка: 200 + порожній data
        return ApiResponse.<BaseMetaData, Item>builder()
                .meta(new BaseMetaData(200, true))
                .data(getAllItems())
                .build();
    }

    public ApiResponse<BaseMetaData, Item> createItemAsApiResponse(Item item) {
        return new ApiResponse<>(new BaseMetaData(201, true), createItem(item));
    }

    public ApiResponse<BaseMetaData, Item> updateItemAsApiResponse(Item item) {
        return new ApiResponse<>(new BaseMetaData(200, true), updateItem(item));
    }

    public ApiResponse<BaseMetaData, Void> deleteItemByIdAsApiResponse(String id) {
        deleteItemById(id);
        return new ApiResponse<BaseMetaData, Void>(new BaseMetaData(200, true));
    }

    public ApiResponse<PaginationMetaData, Item> getPage(Integer page, Integer size) {
        if (page == null || page < 0) {
            throw new InvalidRequestException("Parameter 'page' must not be negative, got: " + page);
        }
        if (size == null || size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidRequestException(
                    "Parameter 'size' must be between 1 and " + MAX_PAGE_SIZE + ", got: " + size);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Item> items = itemRepository.findAll(pageable);

        // Колекція не порожня, але такої сторінки немає
        if (items.getTotalElements() > 0 && page >= items.getTotalPages()) {
            throw new ApiException(HttpStatus.NOT_FOUND,
                    "Page " + page + " does not exist. Total pages: " + items.getTotalPages()
                            + " (page numbering starts from 0)");
        }

        PaginationMetaData metaData = new PaginationMetaData();
        metaData.setCode(200);
        metaData.setSuccess(true);
        metaData.setNumber(items.getNumber());
        metaData.setSize(items.getSize());
        metaData.setTotalElements(items.getTotalElements());
        metaData.setTotalPages(items.getTotalPages());
        metaData.setFirst(items.isFirst());
        metaData.setLast(items.isLast());

        return ApiResponse.<PaginationMetaData, Item>builder()
                .meta(metaData)
                .data(items.getContent())
                .build();
    }

    // ===================== Валідація =====================

    private void validateId(String id) {
        if (id == null || id.isBlank()) {
            throw new InvalidRequestException("Item id must not be empty");
        }
        if (!ObjectId.isValid(id)) {
            throw new InvalidRequestException("Invalid item id format: '" + id + "'");
        }
    }

    private void validateItem(Item item) {
        if (item == null) {
            throw new InvalidRequestException("Request body must not be empty");
        }
        if (item.getName() == null || item.getName().isBlank()) {
            throw new InvalidRequestException("Item name must not be empty");
        }
        if (item.getCode() == null || item.getCode().isBlank()) {
            throw new InvalidRequestException("Item code must not be empty");
        }
        if (item.getQuantity() < 0) {
            throw new InvalidRequestException("Item quantity must not be negative, got: " + item.getQuantity());
        }
    }
}