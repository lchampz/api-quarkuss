package org.acme.controllers.v1;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.DTO.InsertAlunoDTO;
import org.acme.DTO.UpdateAlunoStatusDTO;
import org.acme.entities.Aluno;
import org.acme.entities.Matricula;
import org.acme.exceptions.ApiError;
import org.acme.repositories.AlunoRepository;
import org.acme.repositories.MatriculaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.acme.interceptors.Idempotent; // Certifique-se de que este interceptor está no seu projeto

@Path("v1/alunos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Alunos", description = "Gerenciamento de alunos")
@SecurityScheme(securitySchemeName = "apiKey", type = SecuritySchemeType.APIKEY, apiKeyName = "X-API-Key", in = SecuritySchemeIn.HEADER)
public class AlunoController {

    @Inject
    AlunoRepository alunoRepository;

    @Inject
    MatriculaRepository matriculaRepository;

    private void logRequest(String endpoint) {
        Log.info("[" + LocalDateTime.now() + "] Endpoint acessado: " + endpoint);
    }

    @GET
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Lista todos os alunos", description = "Retorna uma lista de todos os alunos cadastrados.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Lista de alunos retornada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = List.class))),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response getAllAlunos() {
        logRequest("/alunos");
        List<Aluno> alunos = alunoRepository.listAll();
        return Response.ok(alunos).build();
    }

    @GET
    @Path("/search")
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Busca alunos", description = "Busca alunos com base em critérios específicos (nome, idade). A busca por escola é feita via matrículas.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Busca realizada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = List.class))),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response searchAlunos(
            @QueryParam("nome") String nome,
            @QueryParam("idadeMin") Integer idadeMin,
            @QueryParam("idadeMax") Integer idadeMax, Long escolaId) { // Removido escolaId como QueryParam
        logRequest("/alunos/search");
        try {
            List<Aluno> alunos = Aluno.listAll();

            List<Map<String, Object>> result = alunos.stream()
                    .filter(aluno -> {
                        // Filtro por nome (case insensitive)
                        boolean matchNome = nome == null || nome.isEmpty() ||
                                aluno.getNome().toLowerCase().contains(nome.toLowerCase());

                        // Filtro por idade mínima
                        boolean matchIdadeMin = idadeMin == null ||
                                aluno.getIdade() >= idadeMin;

                        // Filtro por idade máxima
                        boolean matchIdadeMax = idadeMax == null ||
                                aluno.getIdade() <= idadeMax;

                        return matchNome && matchIdadeMin && matchIdadeMax;
                    })
                    .map(aluno -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", aluno.id);
                        map.put("nome", aluno.getNome());
                        map.put("idade", aluno.getIdade());
                        map.put("dataNascimento", aluno.getDataNascimento());
                        map.put("nomeResponsavel", aluno.getNomeResponsavel());
                        map.put("telefoneResponsavel", aluno.getTelefoneResponsavel());
                        map.put("emailResponsavel", aluno.getEmailResponsavel());
                        map.put("endereco", aluno.getEndereco());
                        map.put("observacoes", aluno.getObservacoes());
                        map.put("ativo", aluno.getAtivo());
                        // Não há mais escolaId ou escolaNome diretamente no Aluno
                        return map;
                    })
                    .collect(Collectors.toList());

            return Response.ok(result).build();

        } catch (Exception e) {
            Log.error("Erro ao buscar alunos: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro ao buscar alunos: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Transactional
    @Idempotent
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Adiciona um aluno", description = "Adiciona um novo aluno ao sistema. A associação com a escola é feita através de matrícula.")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Aluno criado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Aluno.class))),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response addAluno(@Valid InsertAlunoDTO dto) {
        logRequest("/alunos");

        Aluno aluno = new Aluno();
        aluno.setNome(dto.getNome());
        aluno.setIdade(dto.getIdade());
        aluno.setDataNascimento(dto.getDataNascimento());
        aluno.setNomeResponsavel(dto.getNomeResponsavel());
        aluno.setTelefoneResponsavel(dto.getTelefoneResponsavel());
        aluno.setEmailResponsavel(dto.getEmailResponsavel());
        aluno.setEndereco(dto.getEndereco());
        aluno.setObservacoes(dto.getObservacoes());
        aluno.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
        aluno.setDataCriacao(LocalDateTime.now());
        aluno.setDataAtualizacao(null);
        // Não há mais setEscola() diretamente no Aluno
        alunoRepository.persist(aluno);

        return Response.status(Response.Status.CREATED).entity(aluno).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Idempotent
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Atualiza um aluno", description = "Atualiza os dados de um aluno existente.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Aluno atualizado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Aluno.class))),
            @APIResponse(responseCode = "404", description = "Aluno não encontrado"),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response updateAluno(@PathParam("id") Long id, @Valid InsertAlunoDTO dto) {
        logRequest("/alunos/" + id);
        Aluno aluno = alunoRepository.findById(id);
        if (aluno == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError(404, "Not Found", "Aluno não encontrado", "/alunos/" + id))
                    .build();
        }
        aluno.setNome(dto.getNome());
        aluno.setIdade(dto.getIdade());
        aluno.setDataNascimento(dto.getDataNascimento());
        aluno.setNomeResponsavel(dto.getNomeResponsavel());
        aluno.setTelefoneResponsavel(dto.getTelefoneResponsavel());
        aluno.setEmailResponsavel(dto.getEmailResponsavel());
        aluno.setEndereco(dto.getEndereco());
        aluno.setObservacoes(dto.getObservacoes());
        if (dto.getAtivo() != null) {
            aluno.setAtivo(dto.getAtivo());
        }
        aluno.setDataAtualizacao(LocalDateTime.now());
        alunoRepository.persist(aluno);
        return Response.ok(aluno).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Idempotent
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Remove um aluno", description = "Remove um aluno existente do sistema e suas matrículas associadas.")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "Aluno excluído com sucesso"),
            @APIResponse(responseCode = "404", description = "Aluno não encontrado"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response deleteAluno(@PathParam("id") Long id) {
        logRequest("/alunos/" + id);
        Aluno aluno = alunoRepository.findById(id);
        if (aluno == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError(404, "Not Found", "Aluno não encontrado", "/alunos/" + id))
                    .build();
        }
        // Deletar matrículas associadas antes de deletar o aluno
        matriculaRepository.delete("aluno", aluno);
        alunoRepository.delete(aluno);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/matriculas")
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Lista matrículas de um aluno", description = "Retorna todas as matrículas associadas a um aluno específico.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Lista de matrículas retornada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = List.class))),
            @APIResponse(responseCode = "404", description = "Aluno não encontrado"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response getMatriculasPorAluno(@PathParam("id") Long id) {
        logRequest("/alunos/" + id + "/matriculas");
        Aluno aluno = alunoRepository.findById(id);
        if (aluno == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Aluno não encontrado.").build();
        }
        List<Matricula> matriculas = matriculaRepository.find("aluno", aluno).list();
        return Response.ok(matriculas).build();
    }

    @PATCH
    @Path("/{id}/status")
    @Transactional
    @Idempotent
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Atualiza o status de um aluno", description = "Atualiza o status (ativo/inativo) de um aluno existente.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Status do aluno atualizado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Aluno.class))),
            @APIResponse(responseCode = "404", description = "Aluno não encontrado"),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response updateAlunoStatus(@PathParam("id") Long id, @Valid UpdateAlunoStatusDTO dto) {
        logRequest("/alunos/" + id + "/status");
        Aluno aluno = alunoRepository.findById(id);
        if (aluno == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError(404, "Not Found", "Aluno não encontrado", "/alunos/" + id + "/status"))
                    .build();
        }
        aluno.setAtivo(dto.getAtivo());
        aluno.setDataAtualizacao(LocalDateTime.now());
        alunoRepository.persist(aluno);
        return Response.ok(aluno).build();
    }

    @HEAD
    @Path("/{id}")
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Verifica a existência de um aluno", description = "Verifica se um aluno existe no sistema.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Aluno encontrado"),
            @APIResponse(responseCode = "404", description = "Aluno não encontrado"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response checkAlunoExists(@PathParam("id") Long id) {
        logRequest("/alunos/" + id);
        Aluno aluno = alunoRepository.findById(id);
        if (aluno == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/media-idade")
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Calcula a média de idade dos alunos por escola", description = "Calcula a média de idade dos alunos cadastrados no sistema, agrupados por escola via matrículas.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Média de idade calculada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Map.class))),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response getMediaIdadeAlunos() {
        logRequest("/alunos/media-idade");
        List<Matricula> matriculas = matriculaRepository.listAll();

        Map<Long, Double> mediaPorEscola = matriculas.stream()
                .filter(m -> m.getAluno() != null && m.getEscola() != null)
                .collect(Collectors.groupingBy(
                        m -> m.getEscola().id,
                        Collectors.averagingDouble(m -> m.getAluno().getIdade())
                ));
        return Response.ok(mediaPorEscola).build();
    }

    @POST
    @Path("/lote")
    @Transactional
    @Idempotent
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Adiciona um lote de alunos", description = "Adiciona um lote de alunos ao sistema. A associação com a escola é feita através de matrícula.")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Lote de alunos criado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = List.class))),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response addAlunosLote(@Valid List<InsertAlunoDTO> alunos) {
        logRequest("/alunos/lote");
        List<Aluno> alunosCriados = alunos.stream()
                .map(dto -> {
                    Aluno aluno = new Aluno();
                    aluno.setNome(dto.getNome());
                    aluno.setIdade(dto.getIdade());
                    aluno.setDataNascimento(dto.getDataNascimento());
                    aluno.setNomeResponsavel(dto.getNomeResponsavel());
                    aluno.setTelefoneResponsavel(dto.getTelefoneResponsavel());
                    aluno.setEmailResponsavel(dto.getEmailResponsavel());
                    aluno.setEndereco(dto.getEndereco());
                    aluno.setObservacoes(dto.getObservacoes());
                    aluno.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
                    aluno.setDataCriacao(LocalDateTime.now());
                    aluno.setDataAtualizacao(null);
                    return aluno;
                })
                .collect(Collectors.toList());

        alunosCriados.forEach(alunoRepository::persist);
        return Response.status(Response.Status.CREATED).entity(alunosCriados).build();
    }
}