package com.datashop.service;

import com.datashop.db.Jpa;
import com.datashop.domain.Cart;
import com.datashop.domain.CartItem;
import com.datashop.domain.Product;
import com.datashop.domain.PromoCode;
import com.datashop.domain.User;
import com.datashop.repository.CartRepository;
import com.datashop.repository.ProductRepository;
import com.datashop.repository.PromoRepository;
import com.datashop.repository.UserRepository;
import com.datashop.util.Uuids;
import com.datashop.web.Errors;
import com.datashop.web.dto.CartView;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class CartService {

    private final Jpa jpa;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final PromoRepository promoRepository;
    private final UserRepository userRepository;

    public CartView get(UUID userId) {
        return jpa.write(entityManager -> CartView.from(getOrCreateCart(entityManager, userId)));
    }

    public CartView addItem(UUID userId, String rawProductId, Integer quantity) {
        UUID productId = Uuids.parse(rawProductId);
        int amount = quantity == null ? 1 : quantity;
        validateQuantity(amount);
        return jpa.write(entityManager -> {
            Product product = productRepository.findById(entityManager, productId)
                    .orElseThrow(() -> Errors.notFound("Product not found"));
            Cart cart = cartRepository.findByUserId(entityManager, userId)
                    .orElseGet(() -> createCart(entityManager, userId));

            CartItem item = cartRepository.findItem(entityManager, cart.getId(), productId).orElse(null);
            int nextQuantity = amount + (item == null ? 0 : item.getQuantity());
            ensureStock(product, nextQuantity);

            if (item == null) {
                item = new CartItem();
                item.setCart(cart);
                item.setProduct(product);
                item.setQuantity(amount);
                cart.getItems().add(item);
                entityManager.persist(item);
            } else {
                item.setQuantity(nextQuantity);
            }
            cartRepository.save(entityManager, cart);
            return CartView.from(cart);
        });
    }

    public CartView updateItem(UUID userId, String rawItemId, Integer quantity) {
        UUID itemId = Uuids.parse(rawItemId);
        if (quantity == null) {
            throw Errors.badRequest("quantity is required (1-99)");
        }
        validateQuantity(quantity);
        return jpa.write(entityManager -> {
            CartItem item = ownedItem(entityManager, userId, itemId);
            if (item.getProduct() == null || item.getProduct().getDeletedAt() != null) {
                throw Errors.notFound("Cart item not found");
            }
            ensureStock(item.getProduct(), quantity);
            item.setQuantity(quantity);
            Cart cart = item.getCart();
            cartRepository.save(entityManager, cart);
            return CartView.from(cart);
        });
    }

    public CartView removeItem(UUID userId, String rawItemId) {
        UUID itemId = Uuids.parse(rawItemId);
        return jpa.write(entityManager -> {
            CartItem item = ownedItem(entityManager, userId, itemId);
            Cart cart = item.getCart();
            cart.getItems().remove(item);
            cartRepository.deleteItem(entityManager, item);
            return CartView.from(cart);
        });
    }

    public CartView applyPromo(UUID userId, String rawCode) {
        String code = rawCode == null ? "" : rawCode.trim();
        if (code.isEmpty()) {
            throw Errors.badRequest("code is required");
        }
        return jpa.write(entityManager -> {
            Cart cart = getOrCreateCart(entityManager, userId);
            PromoCode promo = promoRepository.findByCode(entityManager, code)
                    .orElseThrow(() -> Errors.notFound("Promo code not found"));
            if (!promo.isActive()) {
                throw Errors.badRequest("Promo code is inactive");
            }
            cart.setPromoCode(promo);
            cartRepository.save(entityManager, cart);
            return CartView.from(cart);
        });
    }

    public CartView removePromo(UUID userId) {
        return jpa.write(entityManager -> {
            Cart cart = getOrCreateCart(entityManager, userId);
            cart.setPromoCode(null);
            cartRepository.save(entityManager, cart);
            return CartView.from(cart);
        });
    }

    private Cart getOrCreateCart(EntityManager entityManager, UUID userId) {
        return cartRepository.findByUserId(entityManager, userId)
                .orElseGet(() -> createCart(entityManager, userId));
    }

    private Cart createCart(EntityManager entityManager, UUID userId) {
        User user = userRepository.findById(entityManager, userId)
                .orElseThrow(() -> Errors.unauthorized("User no longer exists"));
        return cartRepository.createFor(entityManager, user);
    }

    private void validateQuantity(int quantity) {
        if (quantity < 1 || quantity > 99) {
            throw Errors.badRequest("quantity must be between 1 and 99");
        }
    }

    private CartItem ownedItem(EntityManager entityManager, UUID userId, UUID itemId) {
        CartItem item = cartRepository.findItem(entityManager, itemId)
                .orElseThrow(() -> Errors.notFound("Cart item not found"));
        if (!item.getCart().getUser().getId().equals(userId)) {
            throw Errors.notFound("Cart item not found");
        }
        return item;
    }

    private void ensureStock(Product product, int quantity) {
        if (quantity > product.getStockQuantity()) {
            throw Errors.conflict("Insufficient stock for \"" + product.getTitle()
                    + "\": available " + product.getStockQuantity());
        }
    }
}
