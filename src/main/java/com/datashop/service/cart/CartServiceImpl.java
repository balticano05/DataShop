package com.datashop.service.cart;

import com.datashop.db.Jpa;
import com.datashop.domain.cart.Cart;
import com.datashop.domain.cart.CartItem;
import com.datashop.domain.product.Product;
import com.datashop.domain.promo.PromoCode;
import com.datashop.domain.user.User;
import com.datashop.repository.CartRepository;
import com.datashop.repository.ProductRepository;
import com.datashop.repository.PromoRepository;
import com.datashop.repository.UserRepository;
import com.datashop.util.Constants;
import com.datashop.util.id.Uuids;
import com.datashop.util.web.Errors;
import com.datashop.web.dto.cart.CartView;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final Jpa jpa;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final PromoRepository promoRepository;
    private final UserRepository userRepository;

    @Override
    public CartView get(UUID userId) {
        return jpa.write(entityManager -> CartView.from(getOrCreateCart(entityManager, userId)));
    }

    @Override
    public CartView addItem(UUID userId, String rawProductId, Integer quantity) {
        UUID productId = Uuids.parse(rawProductId);
        int amount = Optional.ofNullable(quantity).orElse(1);
        validateQuantity(amount);
        return jpa.write(entityManager -> {
            Product product = productRepository.findById(entityManager, productId)
                    .orElseThrow(() -> Errors.notFound(Constants.Messages.PRODUCT_NOT_FOUND));
            Cart cart = cartRepository.findByUserId(entityManager, userId)
                    .orElseGet(() -> createCart(entityManager, userId));

            CartItem item = cartRepository.findItem(entityManager, cart.getId(), productId).orElse(null);
            int nextQuantity = Optional.ofNullable(item).map(CartItem::getQuantity).orElse(0) + amount;
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

    @Override
    public CartView updateItem(UUID userId, String rawItemId, Integer quantity) {
        UUID itemId = Uuids.parse(rawItemId);
        if (quantity == null) {
            throw Errors.badRequest(Constants.Messages.QUANTITY_REQUIRED);
        }
        validateQuantity(quantity);
        return jpa.write(entityManager -> {
            CartItem item = ownedItem(entityManager, userId, itemId);
            Product product = Optional.ofNullable(item.getProduct())
                    .filter(value -> value.getDeletedAt() == null)
                    .orElseThrow(() -> Errors.notFound(Constants.Messages.CART_ITEM_NOT_FOUND));
            ensureStock(product, quantity);
            item.setQuantity(quantity);
            Cart cart = item.getCart();
            cartRepository.save(entityManager, cart);
            return CartView.from(cart);
        });
    }

    @Override
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

    @Override
    public CartView applyPromo(UUID userId, String rawCode) {
        String code = Optional.ofNullable(rawCode).map(String::trim).orElse("");
        if (code.isEmpty()) {
            throw Errors.badRequest(Constants.Messages.CODE_REQUIRED);
        }
        return jpa.write(entityManager -> {
            Cart cart = getOrCreateCart(entityManager, userId);
            PromoCode promo = promoRepository.findByCode(entityManager, code)
                    .orElseThrow(() -> Errors.notFound(Constants.Messages.PROMO_NOT_FOUND));
            if (!promo.isActive()) {
                throw Errors.badRequest(Constants.Messages.PROMO_INACTIVE);
            }
            cart.setPromoCode(promo);
            cartRepository.save(entityManager, cart);
            return CartView.from(cart);
        });
    }

    @Override
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
                .orElseThrow(() -> Errors.unauthorized(Constants.Messages.USER_NOT_FOUND));
        return cartRepository.createFor(entityManager, user);
    }

    private void validateQuantity(int quantity) {
        if (quantity < Constants.Validation.MIN_CART_QUANTITY
                || quantity > Constants.Validation.MAX_CART_QUANTITY) {
            throw Errors.badRequest(Constants.Messages.QUANTITY_RANGE);
        }
    }

    private CartItem ownedItem(EntityManager entityManager, UUID userId, UUID itemId) {
        CartItem item = cartRepository.findItem(entityManager, itemId)
                .orElseThrow(() -> Errors.notFound(Constants.Messages.CART_ITEM_NOT_FOUND));
        if (!item.getCart().getUser().getId().equals(userId)) {
            throw Errors.notFound(Constants.Messages.CART_ITEM_NOT_FOUND);
        }
        return item;
    }

    private void ensureStock(Product product, int quantity) {
        if (quantity > product.getStockQuantity()) {
            throw Errors.conflict(Constants.Messages.INSUFFICIENT_STOCK_PREFIX + product.getTitle()
                    + "\": available " + product.getStockQuantity());
        }
    }
}
