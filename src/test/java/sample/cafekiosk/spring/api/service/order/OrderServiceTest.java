package sample.cafekiosk.spring.api.service.order;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import sample.cafekiosk.spring.api.controller.order.request.OrderCreateRequest;
import sample.cafekiosk.spring.api.service.order.request.OrderCreateServiceRequest;
import sample.cafekiosk.spring.api.service.order.response.OrderResponse;
import sample.cafekiosk.spring.domain.order.OrderRepository;
import sample.cafekiosk.spring.domain.orderproduct.OrderProductRepository;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductSellingStatus;
import sample.cafekiosk.spring.domain.product.ProductType;
import sample.cafekiosk.spring.domain.stock.Stock;
import sample.cafekiosk.spring.domain.stock.StockRepository;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class OrderServiceTest {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderProductRepository orderProductRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private OrderService orderService;

    @DisplayName("주문 번호 리스트를 받아 주문을 생성")
    @Test
    void test(){
        //given
        LocalDateTime registeredTime = LocalDateTime.now();

        Product p1 = getProduct("001", ProductType.HANDMADE, ProductSellingStatus.SELLING, "아메리카노", 4000);
        Product p2 = getProduct("002", ProductType.HANDMADE, ProductSellingStatus.SELLING, "아메리카노", 5000);
        Product p3 = getProduct("003", ProductType.HANDMADE, ProductSellingStatus.SELLING, "아메리카노", 6000);
        productRepository.saveAll(List.of(p1, p2, p3));

        OrderCreateServiceRequest request = OrderCreateServiceRequest.builder()
                .productNumbers(List.of("001", "003")).build();

        //when
        OrderResponse orderResponse = orderService.createOrder(request, registeredTime);

        //then
        assertThat(orderResponse.getId()).isNotNull();
        assertThat(orderResponse)
                .extracting("registeredDateTime", "totalPrice")
                .contains(registeredTime, 10000);
        assertThat(orderResponse.getProducts()).hasSize(2)
                .extracting("productNumber", "price")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("001", 4000),
                        Tuple.tuple("003", 6000)
                );
    }

    @DisplayName("중복되는 상품번호 리스트로 주문을 생성")
    @Test
    void createOrderWithDuplicateProductNumbers(){
        //given
        LocalDateTime registeredTime = LocalDateTime.now();

        Product p1 = getProduct("001", ProductType.HANDMADE, ProductSellingStatus.SELLING, "아메리카노", 4000);
        Product p2 = getProduct("002", ProductType.HANDMADE, ProductSellingStatus.SELLING, "카푸치노", 5000);
        Product p3 = getProduct("003", ProductType.HANDMADE, ProductSellingStatus.SELLING, "카라멜 마끼아또", 6000);
        productRepository.saveAll(List.of(p1, p2, p3));

        OrderCreateServiceRequest request = OrderCreateServiceRequest.builder()
                .productNumbers(List.of("001", "001", "002"))
                .build();

        //when
        OrderResponse orderResponse = orderService.createOrder(request, registeredTime);

        //then
        assertThat(orderResponse.getId()).isNotNull();
        assertThat(orderResponse)
                .extracting("registeredDateTime", "totalPrice")
                .contains(registeredTime, 13000);
        assertThat(orderResponse.getProducts()).hasSize(3)
                .extracting("productNumber", "name")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("001", "아메리카노"),
                        Tuple.tuple("001", "아메리카노"),
                        Tuple.tuple("002", "카푸치노")
                );
    }

    @DisplayName("재고와 관련된 상품이 포함되어 있는 주문번호 리스트를 받아 주문을 생성")
    @Test
    void createOrderWithStock(){
        //given
        LocalDateTime registerTime = LocalDateTime.now();
        Product p1 = getProduct("001", ProductType.BOTTLE, ProductSellingStatus.SELLING, "아메리카노", 4000);
        Product p2 = getProduct("002", ProductType.BAKERY, ProductSellingStatus.SELLING, "카푸치노", 5000);
        Product p3 = getProduct("003", ProductType.HANDMADE, ProductSellingStatus.SELLING, "카라멜 마끼아또", 6000);
        productRepository.saveAll(List.of(p1, p2, p3));

        Stock s1 = Stock.create("001", 2);
        Stock s2 = Stock.create("002", 2);
        stockRepository.saveAll(List.of(s1, s2));

        OrderCreateServiceRequest request = OrderCreateServiceRequest.builder()
                .productNumbers(List.of("001", "002", "001", "003"))
                .build();

        //when
        OrderResponse orderResponse = orderService.createOrder(request, registerTime);

        // then
        assertThat(orderResponse.getId()).isNotNull();
        assertThat(orderResponse)
                .extracting("registeredDateTime", "totalPrice")
                .contains(registerTime, 19000);
        assertThat(orderResponse.getProducts()).hasSize(4)
                .extracting("productNumber", "price")
                .containsExactlyInAnyOrder(
                        tuple("001", 4000),
                        tuple("001", 4000),
                        tuple("002", 5000),
                        tuple("003", 6000)
                );

        List<Stock> stocks = stockRepository.findAll();
        assertThat(stocks).hasSize(2)
                .extracting("productNumber", "quantity")
                .containsExactlyInAnyOrder(
                        tuple("001", 0),
                        tuple("002", 1)
                );

    }

    @DisplayName("재고가 부족한 상품으로 주문을 생성하려는 경우 예외가 발생")
    @Test
    void createOrderWithNoStock(){
        // given
        LocalDateTime registeredDateTime = LocalDateTime.now();

        Product p1 = getProduct("001", ProductType.BOTTLE, ProductSellingStatus.SELLING, "아메리카노", 4000);
        Product p2 = getProduct("002", ProductType.BAKERY, ProductSellingStatus.SELLING, "카푸치노", 5000);
        Product p3 = getProduct("003", ProductType.HANDMADE, ProductSellingStatus.SELLING, "카라멜 마끼아또", 6000);
        productRepository.saveAll(List.of(p1, p2, p3));


        Stock stock1 = Stock.create("001", 1);
        Stock stock2 = Stock.create("002", 2);
        //stock1.deductQuantity(1); // todo
        stockRepository.saveAll(List.of(stock1, stock2));

        OrderCreateServiceRequest request = OrderCreateServiceRequest.builder()
                .productNumbers(List.of("001", "001", "002", "003"))
                .build();

        // when // then
        assertThatThrownBy(() -> orderService.createOrder(request, registeredDateTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("재고가 부족한 상품이 있습니다.");

    }
    private static Product getProduct(String productNumber, ProductType productType, ProductSellingStatus status,
                                      String name, int price) {
        return Product.builder()
                .productNumber(productNumber)
                .type(productType)
                .sellingStatus(status)
                .name(name)
                .price(price).build();
    }
}