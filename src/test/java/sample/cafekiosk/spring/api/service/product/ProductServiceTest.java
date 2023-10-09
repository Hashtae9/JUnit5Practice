package sample.cafekiosk.spring.api.service.product;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import sample.cafekiosk.spring.api.service.product.request.ProductCreateServiceRequest;
import sample.cafekiosk.spring.api.service.product.response.ProductResponse;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductSellingStatus;
import sample.cafekiosk.spring.domain.product.ProductType;

import javax.transaction.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ProductServiceTest {
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;

    @DisplayName("신규 상품을 등록, 상품번호는 가장 최근 상품의 상품번호에서 1증가")
    @Test
    void createProduct(){
        //given
        Product product1 = getProduct("001", ProductType.HANDMADE, ProductSellingStatus.SELLING, "아메리카노", 4000);
        productRepository.save(product1);
        ProductCreateServiceRequest request1 = ProductCreateServiceRequest.builder()
                .type(ProductType.HANDMADE)
                .sellingStatus(ProductSellingStatus.SELLING)
                .name("카푸치노")
                .price(4500).build();

        //when
        ProductResponse productResponse = productService.createProduct(request1);

        //then
        Assertions.assertThat(productResponse)
                .extracting("productNumber", "type", "name")
                .contains("002", ProductType.HANDMADE, "카푸치노");
    }

    @DisplayName("등록된 상품이 1개도 없는 경우 상품번호 001 확인")
    @Test
    void createProductWhenRepoIsEmpty(){
        //given
        ProductCreateServiceRequest request1 = ProductCreateServiceRequest.builder()
                .type(ProductType.HANDMADE)
                .sellingStatus(ProductSellingStatus.SELLING)
                .name("카푸치노")
                .price(4500).build();

        //when
        ProductResponse productResponse = productService.createProduct(request1);

        //then
        Assertions.assertThat(productResponse)
                .extracting("productNumber", "type", "name")
                .contains("001", ProductType.HANDMADE, "카푸치노");

        List<Product> products = productRepository.findAll();

        Assertions.assertThat(products).hasSize(1)
                .extracting("productNumber", "type", "name")
                .contains(Tuple.tuple("001", ProductType.HANDMADE, "카푸치노"));
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