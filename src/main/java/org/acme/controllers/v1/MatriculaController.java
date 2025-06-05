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

import java.time.LocalDateTime; // Importar LocalDateTime
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
        Log.info("[" + LocalDateTime.now() + "] Endpoint acessado: " + endpoint);
    }

    @GET
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Lista todas as matrículas", description = "Retorna uma lista de todas as matrículas cadastradas.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Lista de matrículas retornada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = List.class))),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response getAllMatriculas() {
        logRequest("/matriculas");
        List<Matricula> matriculas = matriculaRepository.listAll();
        return Response.ok(matriculas).build();
    }

    @POST
    @Transactional
    @Idempotent
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Adiciona uma matrícula", description = "Adiciona uma nova matrícula ao sistema, verificando a capacidade da escola e o status do aluno.")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Matrícula criada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Matricula.class))),
            @APIResponse(responseCode = "400", description = "Dados inválidos ou escola lotada"),
            @APIResponse(responseCode = "404", description = "Escola ou Aluno não encontrado"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response addMatricula(@Valid InsertMatriculaDTO dto) {
        logRequest("/matriculas");
        Escola escola = escolaRepository.findById(dto.getEscolaId());
        Aluno aluno = alunoRepository.findById(dto.getAlunoId());

        if (escola == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError(404, "Not Found", "Escola não encontrada.", "/matriculas"))
                    .build();
        }
        if (aluno == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError(404, "Not Found", "Aluno não encontrado.", "/matriculas"))
                    .build();
        }

        // Verificar se a escola está ativa
        if (!escola.getAtivo()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError(400, "Bad Request", "Não é possível matricular em uma escola inativa.", "/matriculas"))
                    .build();
        }

        // Verificar se o aluno está ativo
        if (!aluno.getAtivo()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError(400, "Bad Request", "Não é possível matricular um aluno inativo.", "/matriculas"))
                    .build();
        }

        // Contar matrículas ativas na escola
        long matriculasAtivasNaEscola = matriculaRepository.count("escola = ?1 and status = 'ATIVA'", escola);

        // Verificar capacidade da escola
        if (escola.getCapacidade() <= matriculasAtivasNaEscola) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError(400, "Bad Request", "A escola atingiu sua capacidade máxima de matrículas ativas.", "/matriculas"))
                    .build();
        }

        // Verificar se o aluno já possui uma matrícula ATIVA na mesma escola
        boolean jaMatriculadoAtivo = matriculaRepository.find("aluno = ?1 and escola = ?2 and status = 'ATIVA'", aluno, escola)
                .firstResultOptional().isPresent();
        if (jaMatriculadoAtivo) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError(400, "Bad Request", "O aluno já possui uma matrícula ATIVA nesta escola.", "/matriculas"))
                    .build();
        }

        Matricula matricula = new Matricula();
        matricula.setEscola(escola);
        matricula.setAluno(aluno);
        matricula.setDataInicio(dto.getDataInicio().atStartOfDay());
        // Se dataFim não for fornecida, defina um valor padrão ou deixe nulo dependendo da regra de negócio
        matricula.setDataFim(dto.getDataFim() != null ? dto.getDataFim().atStartOfDay() : null);
        matricula.setObservacoes(dto.getObservacoes());
        // Por padrão, uma nova matrícula deve ser ATIVA se as validações passarem
        matricula.setStatus(Matricula.StatusMatricula.ATIVA);
        matricula.setDataCriacao(LocalDateTime.now()); // Adicionar data de criação

        matriculaRepository.persist(matricula);
        return Response.status(Response.Status.CREATED).entity(matricula).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Cancela uma matrícula (define status como CANCELADA)", description = "Altera o status de uma matrícula existente para CANCELADA com base no ID.")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "Matrícula cancelada com sucesso"),
            @APIResponse(responseCode = "404", description = "Matrícula não encontrada"),
            @APIResponse(responseCode = "401", description = "Não autorizado"),
            @APIResponse(responseCode = "429", description = "Limite de requisições excedido")
    })
    public Response cancelarMatricula(@PathParam("id") Long id) {
        logRequest("/matriculas/" + id);
        Matricula matricula = matriculaRepository.findById(id);
        if (matricula == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError(404, "Not Found", "Matrícula não encontrada.", "/matriculas/" + id))
                    .build();
        }
        if (matricula.getStatus() == Matricula.StatusMatricula.CANCELADA) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError(400, "Bad Request", "A matrícula já está cancelada.", "/matriculas/" + id))
                    .build();
        }
        matricula.setStatus(Matricula.StatusMatricula.CANCELADA);
        matricula.setDataAtualizacao(LocalDateTime.now()); // Atualizar data de atualização
        matriculaRepository.persist(matricula);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{id}/status")
    @Transactional
    @Idempotent
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Atualiza o status de uma matrícula", description = "Atualiza o status (ATIVA/CANCELADA) de uma matrícula existente.")
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

        // Validação adicional: não permitir ativar matrícula em escola lotada
        if (dto.getAtivo() && matricula.getStatus() == Matricula.StatusMatricula.CANCELADA) {
            long matriculasAtivasNaEscola = matriculaRepository.count("escola = ?1 and status = 'ATIVA'", matricula.getEscola());
            if (matricula.getEscola().getCapacidade() <= matriculasAtivasNaEscola) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ApiError(400, "Bad Request", "Não é possível reativar a matrícula: a escola está lotada.", "/matriculas/" + id + "/status"))
                        .build();
            }
            // Validação adicional: não permitir ativar matrícula se o aluno ou escola estiver inativo
            if (!matricula.getAluno().getAtivo()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ApiError(400, "Bad Request", "Não é possível reativar a matrícula: o aluno está inativo.", "/matriculas/" + id + "/status"))
                        .build();
            }
            if (!matricula.getEscola().getAtivo()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ApiError(400, "Bad Request", "Não é possível reativar a matrícula: a escola está inativa.", "/matriculas/" + id + "/status"))
                        .build();
            }
        }

        matricula.setStatus(dto.getAtivo() ? Matricula.StatusMatricula.ATIVA : Matricula.StatusMatricula.CANCELADA);
        matricula.setDataAtualizacao(LocalDateTime.now()); // Atualizar data de atualização
        matriculaRepository.persist(matricula);
        return Response.ok(matricula).build();
    }

    @PATCH
    @Path("/lote/status")
    @Transactional
    @Idempotent
    @SecurityRequirement(name = "apiKey")
    @Operation(summary = "Atualiza o status de múltiplas matrículas", description = "Atualiza o status (ATIVA/CANCELADA) de múltiplas matrículas. Validações de capacidade da escola e status de aluno/escola são aplicadas.")
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

        List<Matricula> matriculasAProcessar = ids.stream()
                .map(matriculaRepository::findById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (matriculasAProcessar.isEmpty() && !ids.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError(404, "Not Found", "Nenhuma das matrículas fornecidas foi encontrada.", "/matriculas/lote/status"))
                    .build();
        } else if (ids.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError(400, "Bad Request", "Nenhum ID de matrícula fornecido.", "/matriculas/lote/status"))
                    .build();
        }

        List<Matricula> matriculasAtualizadas = new java.util.ArrayList<>();
        StringBuilder erros = new StringBuilder();

        for (Matricula matricula : matriculasAProcessar) {
            boolean originalStatusAtivo = (matricula.getStatus() == Matricula.StatusMatricula.ATIVA);
            boolean newStatusAtivo = dto.getAtivo();

            // Lógica de validação para reativação
            if (!originalStatusAtivo && newStatusAtivo) { // Tentando reativar
                long matriculasAtivasNaEscola = matriculaRepository.count("escola = ?1 and status = 'ATIVA'", matricula.getEscola());
                if (matricula.getEscola().getCapacidade() <= matriculasAtivasNaEscola) {
                    erros.append(String.format("Matrícula ID %d: Não é possível reativar. Escola '%s' (ID: %d) está lotada. ",
                            matricula.id, matricula.getEscola().getNome(), matricula.getEscola().id));
                    continue;
                }
                if (!matricula.getAluno().getAtivo()) {
                    erros.append(String.format("Matrícula ID %d: Não é possível reativar. Aluno '%s' (ID: %d) está inativo. ",
                            matricula.id, matricula.getAluno().getNome(), matricula.getAluno().id));
                    continue;
                }
                if (!matricula.getEscola().getAtivo()) {
                    erros.append(String.format("Matrícula ID %d: Não é possível reativar. Escola '%s' (ID: %d) está inativa. ",
                            matricula.id, matricula.getEscola().getNome(), matricula.getEscola().id));
                    continue;
                }
            }

            // Se as validações passarem ou se for para CANCELAR
            matricula.setStatus(newStatusAtivo ? Matricula.StatusMatricula.ATIVA : Matricula.StatusMatricula.CANCELADA);
            matricula.setDataAtualizacao(LocalDateTime.now());
            matriculaRepository.persist(matricula);
            matriculasAtualizadas.add(matricula);
        }

        if (erros.length() > 0) {
            // Se houver erros, podemos retornar um 400 com os detalhes
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError(400, "Bad Request", "Algumas matrículas não puderam ser atualizadas: " + erros.toString(), "/matriculas/lote/status"))
                    .build();
        }

        return Response.ok(matriculasAtualizadas).build();
    }
}