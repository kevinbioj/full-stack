package fr.fullstack.shopapp.service;

import fr.fullstack.shopapp.model.OpeningHoursShop;
import fr.fullstack.shopapp.model.Product;
import fr.fullstack.shopapp.model.Shop;
import fr.fullstack.shopapp.repository.ShopRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.hibernate.search.mapper.orm.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopService {

  @PersistenceContext
  private EntityManager em;

  @Autowired
  private ShopRepository shopRepository;

  @Transactional
  public Shop createShop(Shop shop) throws Exception {
    // check if no conflit for hours
    var listHours = shop.getOpeningHours();
    for (var hour : listHours) {
      if (listHours.stream().anyMatch(h -> inInterval(h, hour))) {
        throw new Exception("Les heures d'ouvertures sont en conflit.");
      }
    }
    try {
      Shop newShop = shopRepository.save(shop);
      // Refresh the entity after the save. Otherwise, @Formula does not work.
      em.flush();
      em.refresh(newShop);
      return newShop;
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  @Transactional
  public void deleteShopById(long id) throws Exception {
    try {
      Shop shop = getShop(id);
      // delete nested relations with products
      deleteNestedRelations(shop);
      shopRepository.deleteById(id);
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  public Shop getShopById(long id) throws Exception {
    try {
      return getShop(id);
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  public Page<Shop> getShopList(
      Optional<String> sortBy,
      Optional<Boolean> inVacations,
      Optional<String> createdBefore,
      Optional<String> createdAfter,
      Optional<String> search,
      Pageable pageable
  ) {
    // SORT
    if (sortBy.isPresent()) {
      switch (sortBy.get()) {
        case "name":
          return shopRepository.findByOrderByNameAsc(pageable);
        case "createdAt":
          return shopRepository.findByOrderByCreatedAtAsc(pageable);
        default:
          return shopRepository.findByOrderByNbProductsAsc(pageable);
      }
    }

    // NAME
    if (search.isPresent()) {
      return shopRepository.findByNameContainingIgnoreCaseOrderByIdAsc(pageable, search.get());
    }

    // FILTERS
    Page<Shop> shopList = getShopListWithFilter(inVacations, createdBefore, createdAfter, pageable);
    if (shopList != null) {
      return shopList;
    }

    // NONE
    return shopRepository.findByOrderByIdAsc(pageable);
  }

  @Transactional
  public Shop updateShop(Shop shop) throws Exception {
    try {
      getShop(shop.getId());
      return this.createShop(shop);
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  /**
   * Searches across shops based on a search query and various query parameters.
   *
   * @param query         The search query to match shops against.
   * @param inVacations   Whether to filter shops based on their vacation status (optional).
   * @param createdAfter  Keep shops created after the specified date (optional).
   * @param createdBefore Keep shops created before the specified date (optional).
   * @return The shops that match these criteria.
   */
  public List<Shop> searchShops(String query, Boolean inVacations, LocalDate createdAfter,
      LocalDate createdBefore) {
    var result = Search.session(em)
        .search(Shop.class)
        .where(f -> f.match().field("name").matching(query))
        .fetchAll();
    return result.hits().stream().filter(shop -> {
      boolean shouldKeep = true;
      if (inVacations != null) {
        shouldKeep = inVacations.equals(shop.getInVacations());
      }
      if (createdAfter != null) {
        shouldKeep &=
            createdAfter.isBefore(shop.getCreatedAt()) || createdAfter.isEqual(shop.getCreatedAt());
      }
      if (createdBefore != null) {
        shouldKeep &= createdBefore.isAfter(shop.getCreatedAt()) || createdBefore.isEqual(
            shop.getCreatedAt());
      }
      return shouldKeep;
    }).toList();
  }

  private void deleteNestedRelations(Shop shop) {
    List<Product> products = shop.getProducts();
    for (int i = 0; i < products.size(); i++) {
      Product product = products.get(i);
      product.setShop(null);
      em.merge(product);
      em.flush();
    }
  }

  private Shop getShop(Long id) throws Exception {
    Optional<Shop> shop = shopRepository.findById(id);
    if (!shop.isPresent()) {
      throw new Exception("Shop with id " + id + " not found");
    }
    return shop.get();
  }

  private Page<Shop> getShopListWithFilter(
      Optional<Boolean> inVacations,
      Optional<String> createdAfter,
      Optional<String> createdBefore,
      Pageable pageable
  ) {
    if (inVacations.isPresent() && createdBefore.isPresent() && createdAfter.isPresent()) {
      return shopRepository.findByInVacationsAndCreatedAtGreaterThanAndCreatedAtLessThan(
          inVacations.get(),
          LocalDate.parse(createdAfter.get()),
          LocalDate.parse(createdBefore.get()),
          pageable
      );
    }

    if (inVacations.isPresent() && createdBefore.isPresent()) {
      return shopRepository.findByInVacationsAndCreatedAtLessThan(
          inVacations.get(), LocalDate.parse(createdBefore.get()), pageable
      );
    }

    if (inVacations.isPresent() && createdAfter.isPresent()) {
      return shopRepository.findByInVacationsAndCreatedAtGreaterThan(
          inVacations.get(), LocalDate.parse(createdAfter.get()), pageable
      );
    }

    if (inVacations.isPresent()) {
      return shopRepository.findByInVacations(inVacations.get(), pageable);
    }

    if (createdBefore.isPresent() && createdAfter.isPresent()) {
      return shopRepository.findByCreatedAtBetween(
          LocalDate.parse(createdAfter.get()), LocalDate.parse(createdBefore.get()), pageable
      );
    }

    if (createdBefore.isPresent()) {
      return shopRepository.findByCreatedAtLessThan(
          LocalDate.parse(createdBefore.get()), pageable
      );
    }

    if (createdAfter.isPresent()) {
      return shopRepository.findByCreatedAtGreaterThan(
          LocalDate.parse(createdAfter.get()), pageable
      );
    }

    return null;
  }

  private boolean inInterval(OpeningHoursShop h1, OpeningHoursShop h2) {
    // on compare la même référence de l'objet
    if (h1.equals(h2)) {
      return false;
    }
    if (h1.getDay() == h2.getDay()) {
      if (h1.getOpenAt() == h2.getOpenAt() || h1.getCloseAt() == h2.getCloseAt()) {
        return true;
      }
      if (h1.getOpenAt().isBefore(h2.getOpenAt()) && h1.getCloseAt().isAfter(h2.getCloseAt())) {
        return true;
      }
      if (h2.getOpenAt().isBefore(h1.getOpenAt()) && h2.getCloseAt().isAfter(h2.getCloseAt())) {
        return true;
      }
      if (h1.getOpenAt().isBefore(h2.getOpenAt()) && h1.getCloseAt().isAfter(h2.getOpenAt())) {
        return true;
      }
      return h2.getOpenAt().isBefore(h1.getOpenAt()) && h2.getCloseAt().isAfter(h1.getOpenAt());
    }
    return false;
  }
}
