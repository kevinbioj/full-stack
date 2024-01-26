package fr.fullstack.shopapp.controller;

import fr.fullstack.shopapp.model.Shop;
import fr.fullstack.shopapp.service.ShopService;
import fr.fullstack.shopapp.util.ErrorValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/shops")
public class ShopController {

  // TODO ADD PLAIN TEXT SEARCH FOR SHOP
  @Autowired
  private ShopService service;

  @Operation(description = "Create a shop")
  @PostMapping
  public ResponseEntity<Shop> createShop(@Valid @RequestBody Shop shop, Errors errors) {
    if (errors.hasErrors()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, ErrorValidation.getErrorValidationMessage(errors));
    }

    try {
      return ResponseEntity.ok(service.createShop(shop));
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @Operation(description = "Delete a shop by its id")
  @DeleteMapping("/{id}")
  public HttpStatus deleteShop(@PathVariable long id) {
    try {
      service.deleteShopById(id);
      return HttpStatus.NO_CONTENT;
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @Operation(description = "Get shops (sorting and filtering are possible)")
  @GetMapping
  @Parameters({
      @Parameter(name = "page",
          description = "Results page you want to retrieve (0..N)",
          example = "0"),
      @Parameter(name = "size",
          description = "Number of records per page", example = "5"),
  })
  public ResponseEntity<Page<Shop>> getAllShops(
      @Parameter(hidden = true) Pageable pageable,
      @Parameter(description = "To sort the shops. Possible values are 'name', 'nbProducts' and 'createdAt'",
          example = "name")
      @RequestParam(required = false) Optional<String> sortBy,
      @Parameter(description = "Define that the shops must be in vacations or not", example = "true")
      @RequestParam(required = false) Optional<Boolean> inVacations,
      @Parameter(description = "Define that the shops must be created after this date", example = "2022-11-15")
      @RequestParam(required = false) Optional<String> createdAfter,
      @Parameter(description = "Define that the shops must be created before this date", example = "2022-11-15")
      @RequestParam(required = false) Optional<String> createdBefore,
      @Parameter(description = "Termes de recherche", example = "Boutique du Madrillet")
      @RequestParam(required = false) Optional<String> search

  ) {
    return ResponseEntity.ok(
        service.getShopList(sortBy, inVacations, createdAfter, createdBefore, search, pageable)
    );
  }

  @GetMapping("/search")
  @Operation(description = "Search for shops across the application.")
  public ResponseEntity<List<Shop>> searchShops(@RequestParam String query,
      @RequestParam(required = false) Boolean inVacations, @RequestParam(required = false)
  LocalDate createdBefore, @RequestParam(required = false) LocalDate createdAfter) {
    var shops = service.searchShops(query, inVacations, createdBefore, createdAfter);
    return ResponseEntity.ok(shops);
  }

  @Operation(description = "Get a shop by id")
  @GetMapping("/{id}")
  public ResponseEntity<Shop> getShopById(@PathVariable long id) {
    try {
      return ResponseEntity.ok().body(service.getShopById(id));
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @Operation(description = "Update a shop")
  @PutMapping
  public ResponseEntity<Shop> updateShop(@Valid @RequestBody Shop shop, Errors errors) {
    if (errors.hasErrors()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, ErrorValidation.getErrorValidationMessage(errors));
    }

    try {
      return ResponseEntity.ok().body(service.updateShop(shop));
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }
}
