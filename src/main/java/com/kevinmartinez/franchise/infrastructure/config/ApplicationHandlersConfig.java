package com.kevinmartinez.franchise.infrastructure.config;

import com.kevinmartinez.franchise.application.repository.BranchRepository;
import com.kevinmartinez.franchise.application.repository.FranchiseRepository;
import com.kevinmartinez.franchise.application.repository.ProductRepository;
import com.kevinmartinez.franchise.application.usecase.branch.commands.handler.AddBranchHandler;
import com.kevinmartinez.franchise.application.usecase.branch.commands.handler.RenameBranchHandler;
import com.kevinmartinez.franchise.application.usecase.branch.queries.handler.GetBranchesHandler;
import com.kevinmartinez.franchise.application.usecase.franchise.commands.handler.CreateFranchiseHandler;
import com.kevinmartinez.franchise.application.usecase.franchise.commands.handler.RenameFranchiseHandler;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.handler.GetMaxStockProductsByFranchiseHandler;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.handler.GetFranchisesHandler;
import com.kevinmartinez.franchise.application.usecase.product.commands.handler.AddProductHandler;
import com.kevinmartinez.franchise.application.usecase.product.commands.handler.DeleteProductHandler;
import com.kevinmartinez.franchise.application.usecase.product.commands.handler.RenameProductHandler;
import com.kevinmartinez.franchise.application.usecase.product.commands.handler.UpdateProductStockHandler;
import com.kevinmartinez.franchise.application.usecase.product.queries.handler.GetProductsHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationHandlersConfig {

    @Bean
    CreateFranchiseHandler createFranchiseHandler(FranchiseRepository repository) {
        return new CreateFranchiseHandler(repository);
    }

    @Bean
    RenameFranchiseHandler renameFranchiseHandler(FranchiseRepository repository) {
        return new RenameFranchiseHandler(repository);
    }

    @Bean
    GetFranchisesHandler getFranchisesHandler(FranchiseRepository repository) {
        return new GetFranchisesHandler(repository);
    }

    @Bean
    AddBranchHandler addBranchHandler(
            FranchiseRepository franchiseRepository,
            BranchRepository branchRepository) {
        return new AddBranchHandler(franchiseRepository, branchRepository);
    }

    @Bean
    RenameBranchHandler renameBranchHandler(BranchRepository repository) {
        return new RenameBranchHandler(repository);
    }

    @Bean
    GetBranchesHandler getBranchesHandler(
            BranchRepository branchRepository,
            FranchiseRepository franchiseRepository) {
        return new GetBranchesHandler(branchRepository, franchiseRepository);
    }

    @Bean
    AddProductHandler addProductHandler(
            BranchRepository branchRepository,
            ProductRepository productRepository) {
        return new AddProductHandler(branchRepository, productRepository);
    }

    @Bean
    DeleteProductHandler deleteProductHandler(ProductRepository repository) {
        return new DeleteProductHandler(repository);
    }

    @Bean
    UpdateProductStockHandler updateProductStockHandler(ProductRepository repository) {
        return new UpdateProductStockHandler(repository);
    }

    @Bean
    RenameProductHandler renameProductHandler(ProductRepository repository) {
        return new RenameProductHandler(repository);
    }

    @Bean
    GetProductsHandler getProductsHandler(
            ProductRepository productRepository,
            BranchRepository branchRepository) {
        return new GetProductsHandler(productRepository, branchRepository);
    }

    @Bean
    GetMaxStockProductsByFranchiseHandler getMaxStockProductsByFranchiseHandler(
            FranchiseRepository franchiseRepository,
            BranchRepository branchRepository,
            ProductRepository productRepository) {
        return new GetMaxStockProductsByFranchiseHandler(
                franchiseRepository,
                branchRepository,
                productRepository);
    }
}
