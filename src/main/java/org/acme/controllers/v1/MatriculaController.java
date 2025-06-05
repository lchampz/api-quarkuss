package org.acme.controllers.v1;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.DTO.InsertMatriculaDTO;
import org.acme.DTO.UpdateMatriculaStatusDTO;
import org.acme.entities.Aluno;
import org.acme.entities.Escola;
import org.acme.entities.Matricula;
import org.acme.repositories.AlunoRepository;
import org.acme.repositories.EscolaRepository;
import org.acme.repositories.MatriculaRepository;
import org.acme.exceptions.ApiError;
import org.acme.interceptors.Idempotent;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Path("v1/matriculas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Matrículas", description = "Gerenciamento de matrículas")
@SecurityScheme(securitySchemeName = "apiKey", type = SecuritySchemeType.APIKEY, apiKeyName = "X-API-Key", in = SecuritySchemeIn.HEADER)
public class MatriculaController {

    @Inject
    MatriculaRepository matriculaRepository;

    @Inject
    EscolaRepository escolaRepository;

    @Inject
    AlunoRepository alunoRepository;

    private void logRequest(String endpoint) {
        Log.info("[" + java.time.LocalDateTime.now() + "] Endpoint acessado: " + endpoint);
    }

    @GET
    @Operation(summary = "Lista todas as matrículas", description = "Retorna uma lista de todas as matrículas cadastradas.")
    @SecurityRequirement(name = "apiKey")
    public Response getAllMatriculas() {
        logRequest("/matriculas");
        List<Matricula> matriculas = matriculaRepository.listAll();
        return Response.ok(matriculas).build();
    }

    @POST
    @Transactional
    @Idempotent
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Adiciona uma matrícula", description = "Adiciona uma nova matrícula ao sistema.")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Matrícula criada com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = Matricula.class))),
        @APIResponse(responseCode = "400", description = "Dados inválidos"),
        @APIResponse(responseCode = "401", description = "Não autorizado"),
        @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response addMatricula(@Valid InsertMatriculaDTO dto) {
        logRequest("/matriculas");
        Escola escola = escolaRepository.findById(dto.getEscolaId());
        Aluno aluno = alunoRepository.findById(dto.getAlunoId());
        if (escola == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Escola não encontrada.").build();
        }
        if (aluno == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Aluno não encontrado.").build();
        }
        if (escola.getCapacidade() <= matriculaRepository.countByEscola(escola)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("A escola está lotada.").build();
        }
        Matricula matricula = new Matricula();
        matricula.setEscola(escola);
        matricula.setAluno(aluno);
        matricula.setDataInicio(dto.getDataInicio().atStartOfDay());
        matricula.setDataFim(dto.getDataFim().atStartOfDay());
        matricula.setObservacoes(dto.getObservacoes());
        matricula.setStatus(dto.getAtivo() != null ? Matricula.StatusMatricula.ATIVA : Matricula.StatusMatricula.CANCELADA);
        matriculaRepository.persist(matricula);
        return Response.status(Response.Status.CREATED).entity(matricula).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Cancela uma matrícula", description = "Remove uma matrícula existente com base no ID.")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "Matrícula excluída com sucesso"),
        @APIResponse(responseCode = "404", description = "Matrícula não encontrada"),
        @APIResponse(responseCode = "401", description = "Não autorizado"),
        @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response cancelarMatricula(@PathParam("id") Long id) {
        logRequest("/matriculas/" + id);
        Matricula matricula = matriculaRepository.findById(id);
        if (matricula == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Matrícula não encontrada.").build();
        }
        matricula.setStatus(Matricula.StatusMatricula.CANCELADA);
        matriculaRepository.persist(matricula);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{id}/status")
    @Transactional
    @Idempotent
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Atualiza o status de uma matrícula", description = "Atualiza o status (ativo/inativo) de uma matrícula existente.")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Matrícula atualizada com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = Matricula.class))),
        @APIResponse(responseCode = "404", description = "Matrícula não encontrada"),
        @APIResponse(responseCode = "400", description = "Dados inválidos"),
        @APIResponse(responseCode = "401", description = "Não autorizado"),
        @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response updateMatriculaStatus(@PathParam("id") Long id, @Valid UpdateMatriculaStatusDTO dto) {
        logRequest("/matriculas/" + id + "/status");
        Matricula matricula = matriculaRepository.findById(id);
        if (matricula == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError(404, "Not Found", "Matrícula não encontrada", "/matriculas/" + id + "/status"))
                    .build();
        }
        matricula.setStatus(dto.getAtivo() ? Matricula.StatusMatricula.ATIVA : Matricula.StatusMatricula.CANCELADA);
        matriculaRepository.persist(matricula);
        return Response.ok(matricula).build();
    }

    @PATCH
    @Path("/lote/status")
    @Transactional
    @Idempotent
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Atualiza o status de múltiplas matrículas", description = "Atualiza o status (ativo/inativo) de múltiplas matrículas.")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Status de matrículas atualizado com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = List.class))),
        @APIResponse(responseCode = "400", description = "Dados inválidos"),
        @APIResponse(responseCode = "401", description = "Não autorizado"),
        @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response updateMatriculasStatusLote(@QueryParam("ids") List<Long> ids, @Valid UpdateMatriculaStatusDTO dto) {
        logRequest("/matriculas/lote/status");
        List<Matricula> matriculasAtualizadas = ids.stream()
                .map(matriculaRepository::findById)
                .filter(Objects::nonNull)
                .peek(m -> m.setStatus(dto.getAtivo() ? Matricula.StatusMatricula.ATIVA : Matricula.StatusMatricula.CANCELADA))
                .collect(Collectors.toList());

        matriculasAtualizadas.forEach(matriculaRepository::persist);

        return Response.ok(matriculasAtualizadas).build();
    }
}