package com.kevinmartinez.franchise.presentation.controller;

import java.util.List;

import com.kevinmartinez.franchise.application.usecase.branch.commands.AddBranchCommand;
import com.kevinmartinez.franchise.application.usecase.branch.commands.RenameBranchCommand;
import com.kevinmartinez.franchise.application.usecase.branch.commands.handler.AddBranchHandler;
import com.kevinmartinez.franchise.application.usecase.branch.commands.handler.RenameBranchHandler;
import com.kevinmartinez.franchise.application.usecase.branch.dto.BranchDto;
import com.kevinmartinez.franchise.application.usecase.branch.queries.GetBranchByIdQuery;
import com.kevinmartinez.franchise.application.usecase.branch.queries.GetBranchesQuery;
import com.kevinmartinez.franchise.application.usecase.branch.queries.handler.GetBranchByIdHandler;
import com.kevinmartinez.franchise.application.usecase.branch.queries.handler.GetBranchesHandler;
import com.kevinmartinez.franchise.presentation.dto.request.UpdateNameRequest;
import com.kevinmartinez.franchise.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/branches")
@SecurityRequirement(name = "bearerAuth")
public class BranchController {

    private final AddBranchHandler addBranchHandler;
    private final RenameBranchHandler renameBranchHandler;
    private final GetBranchesHandler getBranchesHandler;
    private final GetBranchByIdHandler getBranchByIdHandler;

    public BranchController(
            AddBranchHandler addBranchHandler,
            RenameBranchHandler renameBranchHandler,
            GetBranchesHandler getBranchesHandler,
            GetBranchByIdHandler getBranchByIdHandler) {
        this.addBranchHandler = addBranchHandler;
        this.renameBranchHandler = renameBranchHandler;
        this.getBranchesHandler = getBranchesHandler;
        this.getBranchByIdHandler = getBranchByIdHandler;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create branch")
    public Mono<ApiResponse<BranchDto>> create(@Valid @RequestBody AddBranchCommand command) {
        return addBranchHandler.handle(command)
                .map(response -> ApiResponse.success("Sucursal creada correctamente", response));
    }

    @GetMapping
    @Operation(summary = "Get branches")
    public Mono<ApiResponse<List<BranchDto>>> getAll(
            @RequestParam(required = false) String franchiseId) {
        return getBranchesHandler.handle(new GetBranchesQuery(franchiseId))
                .collectList()
                .map(response -> ApiResponse.success("Sucursales consultadas correctamente", response));
    }

    @GetMapping("/{branchId}")
    @Operation(summary = "Get branch by id")
    public Mono<ApiResponse<BranchDto>> getById(@PathVariable String branchId) {
        return getBranchByIdHandler.handle(new GetBranchByIdQuery(branchId))
                .map(response -> ApiResponse.success("Sucursal consultada correctamente", response));
    }

    @PatchMapping("/{branchId}/name")
    @Operation(summary = "Rename branch")
    public Mono<ApiResponse<BranchDto>> rename(
            @PathVariable String branchId,
            @Valid @RequestBody UpdateNameRequest request) {
        return renameBranchHandler.handle(new RenameBranchCommand(branchId, request.name()))
                .map(response -> ApiResponse.success("Sucursal actualizada correctamente", response));
    }
}
