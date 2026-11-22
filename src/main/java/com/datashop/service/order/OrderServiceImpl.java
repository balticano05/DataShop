package com.datashop.service.order;

import com.datashop.db.Jpa;
import com.datashop.domain.cart.Cart;
import com.datashop.domain.cart.CartItem;
import com.datashop.domain.cart.CartLine;
import com.datashop.domain.order.Order;
import com.datashop.domain.order.OrderItem;
import com.datashop.domain.order.OrderStatus;
import com.datashop.domain.promo.PromoCode;
import com.datashop.repository.CartRepository;
import com.datashop.repository.OrderRepository;
import com.datashop.repository.ProductRepository;
import com.datashop.service.catalog.CatalogCache;
import com.datashop.util.Constants;
import com.datashop.util.cart.CartCalculator;
import com.datashop.util.id.Uuids;
import com.datashop.util.web.Errors;
import com.datashop.web.dto.order.OrderView;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final Jpa jpa;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CatalogCache catalogCache;

    @Override
    public OrderView checkout(UUID userId) {
        OrderView view = jpa.write(entityManager -> {
            Cart cart = cartRepository.findByUserId(entityManager, userId)
                    .orElseThrow(() -> Errors.badRequest(Constants.Messages.CART_EMPTY));

            List<CartItem> items = cart.getItems().stream()
                    .filter(item -> Optional.ofNullable(item.getProduct())
                            .filter(product -> product.getDeletedAt() == null)
                            .isPresent())
                    .toList();
            if (items.isEmpty()) {
                throw Errors.badRequest(Constants.Messages.CART_EMPTY);
            }
            int discount = Optional.ofNullable(cart.getPromoCode())
                    .filter(PromoCode::isActive)
                    .map(PromoCode::getDiscountPercentage)
                    .orElse(Constants.Cart.MIN_DISCOUNT_PERCENT);
            List<CartLine> lines = items.stream()
                    .map(item -> new CartLine(item.getProduct().getPrice(), item.getQuantity()))
                    .toList();
            BigDecimal total = CartCalculator.calculate(lines, discount).total();

            items.stream()
                    .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                    .forEach(item -> {
                        if (!productRepository.decrementStock(
                                entityManager, item.getProduct().getId(), item.getQuantity())) {
                            throw Errors.conflict(Constants.Messages.INSUFFICIENT_STOCK_PREFIX
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

    @Override
    public List<OrderView> list(UUID userId) {
        return jpa.read(entityManager -> orderRepository.findByUserId(entityManager, userId)
                .stream()
                .map(OrderView::from)
                .toList());
    }

    @Override
    public OrderView get(UUID userId, String rawOrderId) {
        UUID orderId = Uuids.parse(rawOrderId);
        return jpa.read(entityManager -> orderRepository.findByIdAndUserId(entityManager, orderId, userId)
                .map(OrderView::from)
                .orElseThrow(() -> Errors.notFound(Constants.Messages.ORDER_NOT_FOUND)));
    }

    @Override
    public boolean hasPurchased(UUID userId, UUID productId) {
        return jpa.read(entityManager -> orderRepository.hasPaidProduct(entityManager, userId, productId));
    }
}
