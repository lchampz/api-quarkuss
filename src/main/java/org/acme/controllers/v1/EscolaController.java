package org.acme.controllers.v1;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.DTO.InsertEscolaDTO;
import org.acme.DTO.UpdateEscolaStatusDTO;
import org.acme.entities.Escola;
import org.acme.repositories.EscolaRepository;
import org.acme.repositories.MatriculaRepository; // Importar o repositório de Matrícula
import org.acme.interceptors.Idempotent;
import org.acme.exceptions.ApiError;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime; // Importar LocalDateTime

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Path("v1/escolas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Escolas", description = "Gerenciamento de escolas")
@SecurityScheme(
        securitySchemeName = "apiKey",
        type = SecuritySchemeType.APIKEY,
        apiKeyName = "X-API-Key",
        in = SecuritySchemeIn.HEADER
)
public class EscolaController {

    @Inject
    EscolaRepository escolaRepository;

    @Inject
    MatriculaRepository matriculaRepository; // Injetar o repositório de Matrícula

    private void logRequest(String endpoint) {
        Log.info("[" + LocalDateTime.now() + "] Endpoint acessado: " + endpoint);
    }

    @GET
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Lista todas as escolas", description = "Retorna uma lista de todas as escolas cadastradas.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Lista de escolas retornada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = List.class))),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response getAllEscolas() {
        logRequest("/escolas");
        List<Escola> escolas = escolaRepository.listAll();
        return Response.ok(escolas).build();
    }

    @GET
    @Path("/disponiveis")
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Lista escolas com capacidade disponível", description = "Retorna uma lista de escolas que ainda têm vagas disponíveis com base nas matrículas ativas.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Lista de escolas disponíveis retornada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = List.class))),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response getEscolasComVagas() {
        logRequest("/escolas/disponiveis");
        List<Escola> escolas = escolaRepository.listAll();
        List<Escola> escolasDisponiveis = escolas.stream()
                .filter(escola -> matriculaRepository.count("escola = ?1 and status = 'ATIVA'", escola) < escola.getCapacidade())
                .toList();
        return Response.ok(escolasDisponiveis).build();
    }

    @POST
    @Transactional
    @Idempotent
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Adiciona uma escola", description = "Adiciona uma nova escola ao sistema.")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Escola criada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Escola.class))),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response addEscola(@Valid InsertEscolaDTO dto) {
        logRequest("/escolas");
        Escola escola = new Escola();
        escola.setNome(dto.getNome());
        escola.setCapacidade(dto.getCapacidade());
        escola.setEndereco(dto.getEndereco());
        escola.setTelefone(dto.getTelefone());
        escola.setEmail(dto.getEmail());
        escola.setDiretor(dto.getDiretor());
        escola.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
        escola.setDataCriacao(LocalDateTime.now()); // Adicionar data de criação
        escolaRepository.persist(escola);
        return Response.status(Response.Status.CREATED).entity(escola).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Idempotent
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Atualiza uma escola", description = "Atualiza os dados de uma escola existente.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Escola atualizada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Escola.class))),
            @APIResponse(responseCode = "404", description = "Escola não encontrada"),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response updateEscola(@PathParam("id") Long id, @Valid InsertEscolaDTO dto) {
        logRequest("/escolas/" + id);
        Escola escola = escolaRepository.findById(id);
        if (escola == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError(404, "Not Found", "Escola não encontrada", "/escolas/" + id))
                    .build();
        }
        escola.setNome(dto.getNome());
        escola.setCapacidade(dto.getCapacidade());
        escola.setEndereco(dto.getEndereco());
        escola.setTelefone(dto.getTelefone());
        escola.setEmail(dto.getEmail());
        escola.setDiretor(dto.getDiretor());
        if (dto.getAtivo() != null) {
            escola.setAtivo(dto.getAtivo());
        }
        escola.setDataAtualizacao(LocalDateTime.now()); // Atualizar data de atualização
        escolaRepository.persist(escola);
        return Response.ok(escola).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Idempotent
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Remove uma escola", description = "Remove uma escola existente do sistema e suas matrículas associadas.")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "Escola excluída com sucesso"),
            @APIResponse(responseCode = "404", description = "Escola não encontrada"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response deleteEscola(@PathParam("id") Long id) {
        logRequest("/escolas/" + id);
        Escola escola = escolaRepository.findById(id);
        if (escola == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError(404, "Not Found", "Escola não encontrada", "/escolas/" + id))
                    .build();
        }
        // Deletar matrículas associadas antes de deletar a escola
        matriculaRepository.delete("escola", escola);
        escolaRepository.delete(escola);
        return Response.noContent().build();
    }

    @OPTIONS
    @Path("/{id}/capacidade")
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Verifica operações disponíveis", description = "Retorna as operações disponíveis para gerenciar a capacidade da escola.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Operações disponíveis retornadas com sucesso"),
            @APIResponse(responseCode = "404", description = "Escola não encontrada"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response getEscolaCapacidadeOptions(@PathParam("id") Long id) {
        logRequest("/escolas/" + id + "/capacidade");
        Escola escola = escolaRepository.findById(id);
        if (escola == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        long alunosMatriculados = matriculaRepository.count("escola = ?1 and status = 'ATIVA'", escola); // Contar matrículas ativas

        return Response.ok()
                .header("Allow", "GET, PATCH, OPTIONS")
                .header("X-Capacidade-Atual", escola.getCapacidade())
                .header("X-Alunos-Matriculados", alunosMatriculados)
                .header("X-Vagas-Disponiveis", escola.getCapacidade() - alunosMatriculados)
                .build();
    }

    @PATCH
    @Path("/{id}/capacidade")
    @Transactional
    @Idempotent
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Atualiza capacidade da escola", description = "Atualiza a capacidade máxima de uma escola existente. Não permite reduzir a capacidade abaixo do número de alunos matriculados ativos.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Capacidade atualizada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Escola.class))),
            @APIResponse(responseCode = "404", description = "Escola não encontrada"),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response atualizarCapacidadeEscola(@PathParam("id") Long id, @QueryParam("novaCapacidade") int novaCapacidade) {
        logRequest("/escolas/" + id + "/capacidade");
        Escola escola = escolaRepository.findById(id);
        if (escola == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError(404, "Not Found", "Escola não encontrada", "/escolas/" + id + "/capacidade"))
                    .build();
        }
        if (novaCapacidade < 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("A capacidade deve ser maior ou igual a zero.").build();
        }

        long alunosMatriculados = matriculaRepository.count("escola = ?1 and status = 'ATIVA'", escola);
        if (novaCapacidade < alunosMatriculados) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("A nova capacidade (" + novaCapacidade + ") não pode ser menor que o número de alunos ativos matriculados (" + alunosMatriculados + ").")
                    .build();
        }

        escola.setCapacidade(novaCapacidade);
        escola.setDataAtualizacao(LocalDateTime.now()); // Atualizar data de atualização
        escolaRepository.persist(escola);
        return Response.ok(escola).build();
    }

    @GET
    @Path("/{id}/ocupacao")
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Relatório de ocupação", description = "Retorna estatísticas de ocupação da escola, considerando apenas matrículas ativas.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Relatório gerado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Map.class))),
            @APIResponse(responseCode = "404", description = "Escola não encontrada"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response getOcupacaoEscola(@PathParam("id") Long id) {
        logRequest("/escolas/" + id + "/ocupacao");
        Escola escola = escolaRepository.findById(id);
        if (escola == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError(404, "Not Found", "Escola não encontrada", "/escolas/" + id + "/ocupacao"))
                    .build();
        }

        long alunosMatriculadosAtivos = matriculaRepository.count("escola = ?1 and status = 'ATIVA'", escola); // Contar matrículas ativas

        Map<String, Object> ocupacao = new HashMap<>();
        ocupacao.put("escolaId", escola.id);
        ocupacao.put("escolaNome", escola.getNome());
        ocupacao.put("capacidade", escola.getCapacidade());
        ocupacao.put("alunosAtivosMatriculados", alunosMatriculadosAtivos);

        double ocupacaoPercentual = 0.0;
        if (escola.getCapacidade() > 0) {
            ocupacaoPercentual = (double) alunosMatriculadosAtivos / escola.getCapacidade() * 100;
        }
        ocupacao.put("ocupacaoPercentual", ocupacaoPercentual);
        ocupacao.put("vagasDisponiveis", escola.getCapacidade() - alunosMatriculadosAtivos);

        return Response.ok(ocupacao).build();
    }

    @PATCH
    @Path("/{id}/status")
    @Transactional
    @Idempotent
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Atualiza o status de uma escola", description = "Atualiza o status (ativo/inativo) de uma escola existente.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Status atualizado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Escola.class))),
            @APIResponse(responseCode = "404", description = "Escola não encontrada"),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response updateEscolaStatus(@PathParam("id") Long id, @Valid UpdateEscolaStatusDTO dto) {
        logRequest("/escolas/" + id + "/status");
        Escola escola = escolaRepository.findById(id);
        if (escola == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Escola não encontrada.")
                    .build();
        }
        escola.setAtivo(dto.getAtivo());
        escola.setDataAtualizacao(LocalDateTime.now()); // Atualizar data de atualização
        escolaRepository.persist(escola);
        return Response.ok(escola).build();
    }
}