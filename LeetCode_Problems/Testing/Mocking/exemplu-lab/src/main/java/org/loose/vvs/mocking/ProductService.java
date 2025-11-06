package org.loose.vvs.mocking;

import java.util.Comparator;

public class ProductService {

    private ProductProvider productProvider;

    public double computeAveragePricesOfPhones() {
        return productProvider.searchForProduct("phone").stream()
                .mapToInt(Product::getPrice)
                .average()
                .orElse(0.);
    }

    

    public void setProductProvider(ProductProvider productProvider) {
        this.productProvider = productProvider;
    }

    public double computeMinimumPriceOfAllProducts(String brand) {
        if (brand == null)
             throw new IllegalArgumentException();

        return productProvider.searchForProduct(brand).stream()
                .mapToDouble(Product::getPrice)
                .sorted()
                .findFirst()
                .orElse(0.);
    }

    public double computeMaximumRatingOfAllProducts() {
        return productProvider.getProducts().stream()
                .mapToDouble(Product::getRating)
                .max()
                .orElse(0.);
    }

    public Product findProductWithTheLongestTitleWithRating(double minRating, double maxRating) {
        if (minRating > maxRating) {
            throw new IllegalArgumentException("minRating cannot be greater than maxRating");
        }

        return productProvider.getProducts().stream()
                .filter(prod -> prod.getRating() >= minRating && prod.getRating() <= maxRating)
                .filter(prod -> prod.getTitle() != null)
                .max(Comparator.comparingInt(prod -> prod.getTitle().length()))
                .orElse(null);
    }
}
