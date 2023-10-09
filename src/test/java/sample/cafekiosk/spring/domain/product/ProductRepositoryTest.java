package sample.cafekiosk.spring.domain.product;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ProductRepositoryTest {
    
    @Autowired
    private ProductRepository productRepository;
    
    @DisplayName("원하는 판매상태를 가진 상품들을 조회한다.")
    @Test
    void findAllProductsWithStatus(){
        //given
        Product p1 = getProduct("001", ProductType.HANDMADE, ProductSellingStatus.SELLING, "아메리카노", 4000);
        Product p2 = getProduct("002", ProductType.HANDMADE, ProductSellingStatus.STOP_SELLING, "카페라떼", 4500);
        Product p3 = getProduct("003", ProductType.HANDMADE, ProductSellingStatus.HOLD, "카라멜 마끼아또", 6000);

        //상품 만들어 저장
        productRepository.saveAll(List.of(p1, p2, p3));


        //when
        List<Product> products = productRepository.findAllBySellingStatusIn(List.of(ProductSellingStatus.SELLING, ProductSellingStatus.HOLD));

        //then
        Assertions.assertThat(products).hasSize(2)
                .extracting("productNumber", "name", "sellingStatus")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("001", "아메리카노", ProductSellingStatus.SELLING),
                        Tuple.tuple("003", "카라멜 마끼아또", ProductSellingStatus.HOLD)
                );
    }

    @DisplayName("상품번호 리스트로 상품들을 조회한다.")
    @Test
    void findAllProductWithProductNumber(){
        //given
        Product p1 = getProduct("001", ProductType.HANDMADE, ProductSellingStatus.SELLING, "아메리카노", 4000);
        Product p2 = getProduct("002", ProductType.HANDMADE, ProductSellingStatus.STOP_SELLING, "카페라떼", 4500);
        Product p3 = getProduct("003", ProductType.HANDMADE, ProductSellingStatus.HOLD, "카라멜 마끼아또", 6000);

        productRepository.saveAll(List.of(p1, p2, p3));

        //when
        List<Product> products = productRepository.findAllByProductNumberIn(List.of("001", "003"));

        //then
        Assertions.assertThat(products).hasSize(2)
                .extracting("productNumber", "name", "sellingStatus")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("001", "아메리카노", ProductSellingStatus.SELLING),
                        Tuple.tuple("003", "카라멜 마끼아또", ProductSellingStatus.HOLD)
                );
    }

    @DisplayName("가장 마지막 상품의 상품번호를 읽어온다.")
    @Test
    void findLastestProductNumber(){
        //given
        Product p1 = getProduct("001", ProductType.HANDMADE, ProductSellingStatus.SELLING, "아메리카노", 4000);
        Product p2 = getProduct("002", ProductType.HANDMADE, ProductSellingStatus.STOP_SELLING, "카페라떼", 4500);
        Product p3 = getProduct("003", ProductType.HANDMADE, ProductSellingStatus.HOLD, "카라멜 마끼아또", 6000);
        productRepository.saveAll(List.of(p1, p2, p3));

        //when
        String latestProductNumber = productRepository.findLatestProductNumber();

        //then
        Assertions.assertThat(latestProductNumber).isEqualTo("003");
    }

    @DisplayName("가장 마지막으로 저장한 상품의 상품번호를 읽어올때, 상품이 하나도 없는 경우에는 null")
    @Test
    void test(){
        //given

        //when
        String latestProductNumber = productRepository.findLatestProductNumber();

        //then
        Assertions.assertThat(latestProductNumber).isNull();
    }

    private static Product getProduct(String productNum, ProductType productType, ProductSellingStatus productSellingStatus,
                                      String name, int price) {
        return Product.builder()
                .productNumber(productNum)
                .type(productType)
                .price(price)
                .sellingStatus(productSellingStatus)
                .name(name)
                .build();
    }
}