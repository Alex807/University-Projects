package org.loose.vvs.mocking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final List<Product> someProducts = Arrays.asList(
            Product.builder().id(1).price(100).rating(3).build(),
            Product.builder().id(2).price(300).rating(4).title("Ciresarii").build()
    );

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService();
    }

    @Test
    void testAveragePriceForPhonesWithNoProducts() {
        productService.setProductProvider(new ProductProvider() {
            @Override
            public List<Product> getProducts() {
                return new ArrayList<>();
            }

            @Override
            public List<Product> searchForProduct(String query) {
                return new ArrayList<>();
            }
        });

        assertEquals(0, productService.computeAveragePricesOfPhones());
    }

    @Test
    void testAveragePriceForPhonesWithNoProductsWithMock(@Mock ProductProvider productProvider) {
        productService.setProductProvider(productProvider);

        assertEquals(0, productService.computeAveragePricesOfPhones());
    }

    @Test
    void testAveragePriceForPhones() {
        productService.setProductProvider(new MockProductProvider());

        assertEquals(200, productService.computeAveragePricesOfPhones());
    }

    @Test
    void testAveragePriceWithMockito(@Mock ProductProvider productProvider) {
        when(productProvider.searchForProduct(anyString())).thenReturn(someProducts);
        productService.setProductProvider(productProvider);
        assertEquals(200, productService.computeAveragePricesOfPhones());
        verify(productProvider, atLeastOnce()).searchForProduct("phone");
    }

    private static class MockProductProvider implements ProductProvider {

        @Override
        public List<Product> getProducts() {
            return someProducts;
        }

        @Override
        public List<Product> searchForProduct(String query) {
            return someProducts;
        }
    }

    @Test
    void givenBrandIsNull_whenComputeMinimumPriceOfAllProducts_thenThrowIllegalArgumentException(@Mock ProductProvider provider) {
        productService.setProductProvider(provider);

        assertThrows(IllegalArgumentException.class, () -> productService.computeMinimumPriceOfAllProducts(null));
        verify(provider, times(0)).searchForProduct("phone");
    }

    @Test
    void givenBrandHasNoProducts_whenComputeMinimumPriceOfAllProducts_thenReturnZero(@Mock ProductProvider provider) {
        productService.setProductProvider(provider);

        var result = productService.computeMinimumPriceOfAllProducts("phone");

        assertEquals(0, result);
        verify(provider, times(1)).searchForProduct("phone");
    }

    @Test
    void givenBrandHasProducts_whenComputeMinimumPriceOfAllProducts_thenReturnValue(@Mock ProductProvider provider) {
        when(provider.searchForProduct(anyString())).thenReturn(someProducts); //mocking

        productService.setProductProvider(provider);

        var result = productService.computeMinimumPriceOfAllProducts("phone");

        assertEquals(100, result);
        verify(provider, times(1)).searchForProduct(anyString());
    }

    @Test
    void givenProductsListIsEmpty_whenComputeMaximumRatingOfAllProducts_thenReturnZero(@Mock ProductProvider provider) {
        productService.setProductProvider(provider);

        var result = productService.computeMaximumRatingOfAllProducts();

        assertEquals(0, result);
        verify(provider, times(1)).getProducts();
    }

    @Test
    void givenProductsList_whenComputeMaximumRatingOfAllProducts_thenReturnValue(@Mock ProductProvider provider) {
        when(provider.getProducts()).thenReturn(someProducts);

        productService.setProductProvider(provider);

        var result = productService.computeMaximumRatingOfAllProducts();

        assertEquals(4, result);
        verify(provider, times(1)).getProducts();
    }

    @Test
    void givenMinAndMaxAreReversed_whenFindProductWithTheLongestTitleWithRating_thenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> productService.findProductWithTheLongestTitleWithRating(4, 2));
    }

    @Test
    void givenProductsHaveNullTitles_whenFindProductWithTheLongestTitleWithRating_thenReturnNull(@Mock ProductProvider provider) {
        when(provider.getProducts()).thenReturn(Arrays.asList(
                Product.builder().id(2).rating(2).build(),
                Product.builder().id(3).rating(6).build(),
                Product.builder().id(3).rating(0).build()
        ));

        productService.setProductProvider(provider);

        var result = productService.findProductWithTheLongestTitleWithRating(2, 6);

        assertNull(result);
        verify(provider, times(1)).getProducts();
    }

    @Test
    void givenProducts_whenFindProductWithTheLongestTitleWithRating_thenReturnNull(@Mock ProductProvider provider) {
        when(provider.getProducts()).thenReturn(someProducts);

        productService.setProductProvider(provider);

        var result = productService.findProductWithTheLongestTitleWithRating(2, 5);

        assertEquals(Product.builder().id(2).build(), result); //thats why we need to override equals in 'Product' class to can compare 2 objects
        verify(provider, times(1)).getProducts();
    }

    @Test
    void givenProductsAndRatingSurpassMax_whenFindProductWithTheLongestTitleWithRating_thenReturnNull(@Mock ProductProvider provider) {
        when(provider.getProducts()).thenReturn(someProducts);

        productService.setProductProvider(provider);

        var result = productService.findProductWithTheLongestTitleWithRating(2, 3);

        assertNull(result);
        verify(provider, times(1)).getProducts();
    }


}