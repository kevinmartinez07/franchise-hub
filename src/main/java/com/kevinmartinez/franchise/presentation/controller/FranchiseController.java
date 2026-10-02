package com.kevinmartinez.franchise.presentation.controller;

import java.util.List;

import com.kevinmartinez.franchise.application.usecase.franchise.commands.CreateFranchiseCommand;
import com.kevinmartinez.franchise.application.usecase.franchise.commands.RenameFranchiseCommand;
import com.kevinmartinez.franchise.application.usecase.franchise.commands.handler.CreateFranchiseHandler;
import com.kevinmartinez.franchise.application.usecase.franchise.commands.handler.RenameFranchiseHandler;
import com.kevinmartinez.franchise.application.usecase.franchise.dto.FranchiseDto;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.GetFranchiseByIdQuery;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.GetFranchisesQuery;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.handler.GetFranchisesHandler;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/franchises")
@SecurityRequirement(name = "bearerAuth")
public class FranchiseController {

    private final CreateFranchiseHandler createFranchiseHandler;
    private final RenameFranchiseHandler renameFranchiseHandler;
    private final GetFranchisesHandler getFranchisesHandler;

    public FranchiseController(
            CreateFranchiseHandler createFranchiseHandler,
            RenameFranchiseHandler renameFranchiseHandler,
            GetFranchisesHandler getFranchisesHandler) {
        this.createFranchiseHandler = createFranchiseHandler;
        this.renameFranchiseHandler = renameFranchiseHandler;
        this.getFranchisesHandler = getFranchisesHandler;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create franchise")
    public Mono<ApiResponse<FranchiseDto>> create(@Valid @RequestBody CreateFranchiseCommand command) {
        return createFranchiseHandler.handle(command)
                .map(response -> ApiResponse.success("Franquicia creada correctamente", response));
    }

    @GetMapping
    @Operation(summary = "Get all franchises")
    public Mono<ApiResponse<List<FranchiseDto>>> getAll() {
        return getFranchisesHandler.handle(new GetFranchisesQuery())
                .collectList()
                .map(response -> ApiResponse.success("Franquicias consultadas correctamente", response));
    }

    @GetMapping("/{franchiseId}")
    @Operation(summary = "Get franchise by id")
    public Mono<ApiResponse<FranchiseDto>> getById(@PathVariable String franchiseId) {
        return getFranchisesHandler.handle(new GetFranchiseByIdQuery(franchiseId))
                .map(response -> ApiResponse.success("Franquicia consultada correctamente", response));
    }

    @PatchMapping("/{franchiseId}/name")
    @Operation(summary = "Rename franchise")
    public Mono<ApiResponse<FranchiseDto>> rename(
            @PathVariable String franchiseId,
            @Valid @RequestBody UpdateNameRequest request) {
        return renameFranchiseHandler.handle(new RenameFranchiseCommand(franchiseId, request.name()))
                .map(response -> ApiResponse.success("Franquicia actualizada correctamente", response));
    }
}
