package com.kevinmartinez.franchise.presentation.controller;

import java.util.List;

import com.kevinmartinez.franchise.application.usecase.franchise.queries.GetMaxStockProductsByFranchiseQuery;
import com.kevinmartinez.franchise.application.usecase.franchise.dto.MaxStockProductDto;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.handler.GetMaxStockProductsByFranchiseHandler;
import com.kevinmartinez.franchise.application.usecase.product.commands.AddProductCommand;
import com.kevinmartinez.franchise.application.usecase.product.commands.DeleteProductCommand;
import com.kevinmartinez.franchise.application.usecase.product.commands.RenameProductCommand;
import com.kevinmartinez.franchise.application.usecase.product.commands.UpdateProductStockCommand;
import com.kevinmartinez.franchise.application.usecase.product.commands.handler.AddProductHandler;
import com.kevinmartinez.franchise.application.usecase.product.commands.handler.DeleteProductHandler;
import com.kevinmartinez.franchise.application.usecase.product.commands.handler.RenameProductHandler;
import com.kevinmartinez.franchise.application.usecase.product.commands.handler.UpdateProductStockHandler;
import com.kevinmartinez.franchise.application.usecase.product.dto.ProductDto;
import com.kevinmartinez.franchise.application.usecase.product.queries.GetProductByIdQuery;
import com.kevinmartinez.franchise.application.usecase.product.queries.GetProductsQuery;
import com.kevinmartinez.franchise.application.usecase.product.queries.handler.GetProductByIdHandler;
import com.kevinmartinez.franchise.application.usecase.product.queries.handler.GetProductsHandler;
import com.kevinmartinez.franchise.presentation.dto.request.AddProductRequest;
import com.kevinmartinez.franchise.presentation.dto.request.UpdateNameRequest;
import com.kevinmartinez.franchise.presentation.dto.request.UpdateStockRequest;
import com.kevinmartinez.franchise.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final AddProductHandler addProductHandler;
    private final DeleteProductHandler deleteProductHandler;
    private final UpdateProductStockHandler updateProductStockHandler;
    private final RenameProductHandler renameProductHandler;
    private final GetProductsHandler getProductsHandler;
    private final GetProductByIdHandler getProductByIdHandler;
    private final GetMaxStockProductsByFranchiseHandler maxStockHandler;

    public ProductController(
            AddProductHandler addProductHandler,
            DeleteProductHandler deleteProductHandler,
            UpdateProductStockHandler updateProductStockHandler,
            RenameProductHandler renameProductHandler,
            GetProductsHandler getProductsHandler,
            GetProductByIdHandler getProductByIdHandler,
            GetMaxStockProductsByFranchiseHandler maxStockHandler) {
        this.addProductHandler = addProductHandler;
        this.deleteProductHandler = deleteProductHandler;
        this.updateProductStockHandler = updateProductStockHandler;
        this.renameProductHandler = renameProductHandler;
        this.getProductsHandler = getProductsHandler;
        this.getProductByIdHandler = getProductByIdHandler;
        this.maxStockHandler = maxStockHandler;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create product")
    public Mono<ApiResponse<ProductDto>> create(@Valid @RequestBody AddProductRequest request) {
        return addProductHandler.handle(new AddProductCommand(
                        request.branchId(), request.name(), request.stock()))
                .map(response -> ApiResponse.success("Producto creado correctamente", response));
    }

    @GetMapping
    @Operation(summary = "Get products")
    public Mono<ApiResponse<List<ProductDto>>> getAll(
            @RequestParam(required = false) String branchId) {
        return getProductsHandler.handle(new GetProductsQuery(branchId))
                .collectList()
                .map(response -> ApiResponse.success("Productos consultados correctamente", response));
    }

    @GetMapping("/max-stock")
    @Operation(summary = "Get max stock product per branch")
    public Mono<ApiResponse<List<MaxStockProductDto>>> getMaxStockProducts(@RequestParam String franchiseId) {
        return maxStockHandler.handle(new GetMaxStockProductsByFranchiseQuery(franchiseId))
                .collectList()
                .map(response -> ApiResponse.success("Productos con mayor stock consultados correctamente", response));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by id")
    public Mono<ApiResponse<ProductDto>> getById(@PathVariable String productId) {
        return getProductByIdHandler.handle(new GetProductByIdQuery(productId))
                .map(response -> ApiResponse.success("Producto consultado correctamente", response));
    }

    @PatchMapping("/{productId}/stock")
    @Operation(summary = "Update product stock")
    public Mono<ApiResponse<ProductDto>> updateStock(
            @PathVariable String productId,
            @Valid @RequestBody UpdateStockRequest request) {
        return updateProductStockHandler.handle(new UpdateProductStockCommand(productId, request.stock()))
                .map(response -> ApiResponse.success("Stock actualizado correctamente", response));
    }

    @PatchMapping("/{productId}/name")
    @Operation(summary = "Rename product")
    public Mono<ApiResponse<ProductDto>> rename(
            @PathVariable String productId,
            @Valid @RequestBody UpdateNameRequest request) {
        return renameProductHandler.handle(new RenameProductCommand(productId, request.name()))
                .map(response -> ApiResponse.success("Producto actualizado correctamente", response));
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete product")
    public Mono<Void> delete(@PathVariable String productId) {
        return deleteProductHandler.handle(new DeleteProductCommand(productId));
    }
}
