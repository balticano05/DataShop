package com.datashop.service;

import com.datashop.cart.CartCalculator;
import com.datashop.cart.CartLine;
import com.datashop.db.Jpa;
import com.datashop.domain.Cart;
import com.datashop.domain.CartItem;
import com.datashop.domain.Order;
import com.datashop.domain.OrderItem;
import com.datashop.domain.OrderStatus;
import com.datashop.repository.CartRepository;
import com.datashop.repository.OrderRepository;
import com.datashop.repository.ProductRepository;
import com.datashop.util.Uuids;
import com.datashop.web.Errors;
import com.datashop.web.dto.OrderView;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class OrderService {

    private final Jpa jpa;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CatalogCache catalogCache;

    public OrderView checkout(UUID userId) {
        OrderView view = jpa.write(entityManager -> {
            Cart cart = cartRepository.findByUserId(entityManager, userId)
                    .orElseThrow(() -> Errors.badRequest("Cart is empty"));

            List<CartItem> items = cart.getItems().stream()
                    .filter(item -> item.getProduct() != null
                            && item.getProduct().getDeletedAt() == null)
                    .toList();
            if (items.isEmpty()) {
                throw Errors.badRequest("Cart is empty");
            }

            int discount = cart.getPromoCode() == null || !cart.getPromoCode().isActive()
                    ? 0
                    : cart.getPromoCode().getDiscountPercentage();
            List<CartLine> lines = items.stream()
                    .map(item -> new CartLine(item.getProduct().getPrice(), item.getQuantity()))
                    .toList();
            BigDecimal total = CartCalculator.calculate(lines, discount).getTotal();

            items.stream()
                    .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                    .forEach(item -> {
                        if (!productRepository.decrementStock(
                                entityManager, item.getProduct().getId(), item.getQuantity())) {
                            throw Errors.conflict("Insufficient stock for \""
                                    + item.getProduct().getTitle() + "\"");
                        }
                    });

            Order order = new Order();
            order.setUser(cart.getUser());
            order.setStatus(OrderStatus.PAID);
            order.setTotal(total);
            for (CartItem item : items) {
                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(item.getProduct());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setUnitPrice(item.getProduct().getPrice());
                orderItem.setTitleSnapshot(item.getProduct().getTitle());
                order.addItem(orderItem);
            }
            Order saved = orderRepository.save(entityManager, order);

            cart.getItems().clear();
            cart.setPromoCode(null);
            cartRepository.save(entityManager, cart);

            return OrderView.from(saved);
        });
        catalogCache.invalidateAll();
        return view;
    }

    public List<OrderView> list(UUID userId) {
        return jpa.read(entityManager -> orderRepository.findByUserId(entityManager, userId)
                .stream()
                .map(OrderView::from)
                .toList());
    }

    public OrderView get(UUID userId, String rawOrderId) {
        UUID orderId = Uuids.parse(rawOrderId);
        return jpa.read(entityManager -> orderRepository.findByIdAndUserId(entityManager, orderId, userId)
                .map(OrderView::from)
                .orElseThrow(() -> Errors.notFound("Order not found")));
    }

    public boolean hasPurchased(UUID userId, UUID productId) {
        return jpa.read(entityManager -> orderRepository.hasPaidProduct(entityManager, userId, productId));
    }
}
