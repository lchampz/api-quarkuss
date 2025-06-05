package org.acme;


import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.acme.DTO.InsertAlunoDTO;
import org.acme.DTO.InsertEscolaDTO;
import org.acme.DTO.InsertMatriculaDTO;
import org.acme.DTO.UpdateAlunoStatusDTO;
import org.acme.DTO.UpdateMatriculaStatusDTO;
import org.acme.controllers.v1.AlunoController;
import org.acme.controllers.v1.EscolaController;
import org.acme.controllers.v1.MatriculaController;
import org.acme.entities.Aluno;
import org.acme.entities.Escola;
import org.acme.entities.Matricula;
import org.acme.interceptors.ApiKey;
import org.acme.interceptors.Idempotent;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

@Path("/v2")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RateLimit(value = 3, minSpacingUnit = ChronoUnit.SECONDS, minSpacing = 1)
@SecurityScheme(securitySchemeName = "apiKey", type = SecuritySchemeType.APIKEY, apiKeyName = "X-API-Key", in = SecuritySchemeIn.HEADER)
@Tag(name = "V2", description = "Nova versão da API com métodos HTTP especializados")
@ApiKey
public class ApiRoutesV2 {

    @Inject
    AlunoController alunoController;

    @Inject
    EscolaController escolaController;

    @Inject
    MatriculaController matriculaController;

    @Fallback
    public String fallback() {
        return "{\"erro\":\"Taxa de requisições excedida para a API de produtos.\"}";
    }

    @GET
    @Path("/alunos")
    @ApiKey
    @Operation(summary = "Lista todos os alunos", description = "Retorna uma lista de todos os alunos cadastrados.")
    public Response getAllAlunos() {
        return alunoController.getAllAlunos();
    }

    @POST
    @Path("/alunos")
    @Idempotent
    @Operation(summary = "Adiciona um aluno", description = "Adiciona um novo aluno ao sistema.")
    public Response addAluno(InsertAlunoDTO item) {
        return alunoController.addAluno(item);
    }

    @PATCH
    @Path("/alunos/{id}/status")
    @Operation(summary = "Atualiza status do aluno", description = "Atualiza apenas o status do aluno (ativo/inativo).")
    public Response updateAlunoStatus(@PathParam("id") Long id, UpdateAlunoStatusDTO status) {
        return alunoController.updateAlunoStatus(id, status);
    }

    @HEAD
    @Path("/alunos/{id}")
    @Operation(summary = "Verifica existência do aluno", description = "Verifica se um aluno existe sem retornar seus dados.")
    public Response checkAlunoExists(@PathParam("id") Long id) {
        return alunoController.checkAlunoExists(id);
    }

    // Métodos para Escolas
    @GET
    @Path("/escolas")
    @Operation(summary = "Lista todas as escolas", description = "Retorna uma lista de todas as escolas cadastradas.")
    public Response getAllEscolas() {
        return escolaController.getAllEscolas();
    }

    @POST
    @Path("/escolas")
    @Idempotent
    @Operation(summary = "Adiciona uma escola", description = "Adiciona uma nova escola ao sistema.")
    public Response addEscola(InsertEscolaDTO item) {
        return escolaController.addEscola(item);
    }

    @OPTIONS
    @Path("/escolas/{id}/capacidade")
    @Operation(summary = "Verifica operações disponíveis", description = "Retorna as operações disponíveis para gerenciar a capacidade da escola.")
    public Response getEscolaCapacidadeOptions(@PathParam("id") Long id) {
        return escolaController.getEscolaCapacidadeOptions(id);
    }

    @PATCH
    @Path("/escolas/{id}/capacidade")
    @Operation(summary = "Atualiza capacidade da escola", description = "Atualiza a capacidade máxima de uma escola existente.")
    public Response atualizarCapacidadeEscola(@PathParam("id") Long id, @QueryParam("novaCapacidade") int novaCapacidade) {
        return escolaController.atualizarCapacidadeEscola(id, novaCapacidade);
    }

    // Métodos para Matrículas
    @GET
    @Path("/matriculas")
    @Operation(summary = "Lista todas as matrículas", description = "Retorna uma lista de todas as matrículas cadastradas.")
    public Response getAllMatriculas() {
        return matriculaController.getAllMatriculas();
    }

    @POST
    @Path("/matriculas")
    @Idempotent
    @Operation(summary = "Adiciona uma matrícula", description = "Adiciona uma nova matrícula ao sistema.")
    public Response addMatricula(InsertMatriculaDTO item) {
        return matriculaController.addMatricula(item);
    }

    @PATCH
    @Path("/matriculas/{id}/status")
    @Operation(summary = "Atualiza status da matrícula", description = "Atualiza o status da matrícula (ativo/inativo).")
    public Response updateMatriculaStatus(@PathParam("id") Long id, UpdateMatriculaStatusDTO status) {
        return matriculaController.updateMatriculaStatus(id, status);
    }

    // Métodos de Relatórios e Estatísticas
    @GET
    @Path("/relatorios/escolas/{id}/ocupacao")
    @Operation(summary = "Relatório de ocupação", description = "Retorna estatísticas de ocupação da escola.")
    public Response getOcupacaoEscola(@PathParam("id") Long id) {
        return escolaController.getOcupacaoEscola(id);
    }

    @GET
    @Path("/relatorios/alunos/idade-media")
    @Operation(summary = "Média de idade", description = "Retorna a média de idade dos alunos por escola.")
    public Response getMediaIdadeAlunos() {
        return alunoController.getMediaIdadeAlunos();
    }

    // Métodos de Busca Avançada
    @GET
    @Path("/alunos/busca")
    @Operation(summary = "Busca avançada de alunos", description = "Realiza uma busca avançada de alunos com múltiplos critérios.")
    public Response searchAlunos(@QueryParam("nome") String nome,
                               @QueryParam("idadeMin") Integer idadeMin,
                               @QueryParam("idadeMax") Integer idadeMax,
                               @QueryParam("escola") Long escolaId) {
        return alunoController.searchAlunos(nome, idadeMin, idadeMax, escolaId);
    }

    // Métodos de Lote
    @POST
    @Path("/alunos/lote")
    @Operation(summary = "Adiciona alunos em lote", description = "Adiciona múltiplos alunos de uma vez.")
    public Response addAlunosLote(List<InsertAlunoDTO> alunos) {
        return alunoController.addAlunosLote(alunos);
    }

    @PATCH
    @Path("/matriculas/lote/status")
    @Operation(summary = "Atualiza status em lote", description = "Atualiza o status de múltiplas matrículas de uma vez.")
    public Response updateMatriculasStatusLote(@QueryParam("ids") List<Long> ids, UpdateMatriculaStatusDTO status) {
        return matriculaController.updateMatriculasStatusLote(ids, status);
    }

    // Novos métodos interessantes

    @GET
    @Path("/export/alunos/csv")
    @Produces("text/csv")
    @Operation(summary = "Exporta alunos para CSV", description = "Exporta a lista de alunos em formato CSV.")
    public Response exportAlunosToCsv() {
        Response response = alunoController.getAllAlunos();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            return response;
        }

        List<Aluno> alunos = (List<Aluno>) response.getEntity();
        
        StreamingOutput stream = output -> {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output))) {
                // Cabeçalho
                writer.write("ID,Nome,Idade,Data Nascimento,Nome Responsável,Telefone Responsável,Email Responsável,Endereço,Observações,Ativo,Escola ID\n");
                
                // Dados
                for (Aluno aluno : alunos) {
                    writer.write(String.format("%d,%s,%d,%s,%s,%s,%s,%s,%s,%b,%d\n",
                        aluno.getId(),
                        aluno.getNome(),
                        aluno.getIdade(),
                        aluno.getDataNascimento(),
                        aluno.getNomeResponsavel(),
                        aluno.getTelefoneResponsavel(),
                        aluno.getEmailResponsavel(),
                        aluno.getEndereco(),
                        aluno.getObservacoes(),
                        aluno.getAtivo(),
                        aluno.getEscola() != null ? aluno.getEscola().getId() : null
                    ));
                }
            }
        };

        return Response.ok(stream)
                .header("Content-Disposition", "attachment; filename=alunos_" + LocalDate.now() + ".csv")
                .build();
    }

    @GET
    @Path("/export/escolas/csv")
    @Produces("text/csv")
    @Operation(summary = "Exporta escolas para CSV", description = "Exporta a lista de escolas em formato CSV.")
    public Response exportEscolasToCsv() {
        Response response = escolaController.getAllEscolas();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            return response;
        }

        List<Escola> escolas = (List<Escola>) response.getEntity();

        StreamingOutput stream = output -> {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output))) {
                // Cabeçalho
                writer.write("ID,Nome,Capacidade,Endereço,Telefone,Email,Diretor,Ativo\n");

                // Dados
                for (Escola escola : escolas) {
                    writer.write(String.format("%d,%s,%d,%s,%s,%s,%s,%b\n",
                        escola.getId(),
                        escola.getNome(),
                        escola.getCapacidade(),
                        escola.getEndereco(),
                        escola.getTelefone(),
                        escola.getEmail(),
                        escola.getDiretor(),
                        escola.getAtivo()
                    ));
                }
            }
        };

        return Response.ok(stream)
                .header("Content-Disposition", "attachment; filename=escolas_" + LocalDate.now() + ".csv")
                .build();
    }

    @GET
    @Path("/relatorios/escolas/ranking")
    @Operation(summary = "Ranking de escolas", description = "Retorna um ranking das escolas por ocupação e média de idade dos alunos.")
    public Response getEscolasRanking() {
        Response response = escolaController.getAllEscolas();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            return response;
        }

        List<Escola> escolas = (List<Escola>) response.getEntity();
        List<Map<String, Object>> ranking = escolas.stream()
            .map(escola -> {
                Integer totalAlunos = escola.getAlunos().size();
                Double ocupacao = totalAlunos.doubleValue() / escola.getCapacidade() * 100;
                Double mediaIdade = escola.getAlunos().stream()
                    .mapToInt(Aluno::getIdade)
                    .average()
                    .orElse(0.0);

                Map<String, Object> map = new HashMap<>();
                map.put("escolaId", Long.valueOf(escola.id));
                map.put("escolaNome", escola.getNome());
                map.put("ocupacao", ocupacao);
                map.put("mediaIdade", mediaIdade);
                map.put("totalAlunos", totalAlunos);
                map.put("capacidade", Integer.valueOf(escola.getCapacidade()));
                return map;
            })
            .sorted((a, b) -> {
                Number ac = (Number) a.get("ocupacao");
                Number bc = (Number) b.get("ocupacao");
                return Integer.compare((Integer) ac, (Integer)bc);
            })
            .collect(Collectors.toList());

        return Response.ok(ranking).build();
    }

    @GET
    @Path("/relatorios/alunos/evasao")
    @Operation(summary = "Relatório de evasão", description = "Retorna estatísticas sobre evasão escolar por período.")
    public Response getRelatorioEvasao(
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
            LocalDate inicio = LocalDate.parse(dataInicio, formatter);
            LocalDate fim = LocalDate.parse(dataFim, formatter);

            Response response = matriculaController.getAllMatriculas();
            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                return response;
            }

            List<Matricula> matriculas = (List<Matricula>) response.getEntity();
            List<Map<String, Object>> evasao = matriculas.stream()
                .filter(m -> m.getDataFim() != null &&
                            m.getDataFim().isAfter(inicio.atStartOfDay()) &&
                            m.getDataFim().isBefore(fim.atStartOfDay()))
                .map(m -> Map.of(
                    "matriculaId", m.getId(),
                    "alunoId", m.getAluno(),
                    "alunoNome", m.getAluno().getNome(),
                    "escolaId", m.getEscola().getId(),
                    "escolaNome", m.getEscola().getNome(),
                    "dataInicio", m.getDataInicio(),
                    "dataFim", m.getDataFim(),
                    "motivo", m.getObservacoes()
                ))
                .collect(Collectors.toList());

            return Response.ok(evasao).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Formato de data inválido. Use o formato ISO (YYYY-MM-DD)")
                    .build();
        }
    }

    @GET
    @Path("/relatorios/escolas/crescimento")
    @Operation(summary = "Relatório de crescimento", description = "Retorna estatísticas de crescimento das escolas por período.")
    public Response getRelatorioCrescimento(
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
            LocalDate inicio = LocalDate.parse(dataInicio, formatter);
            LocalDate fim = LocalDate.parse(dataFim, formatter);

            Response response = escolaController.getAllEscolas();
            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                return response;
            }

            List<Escola> escolas = (List<Escola>) response.getEntity();
            List<Map<String, Object>> crescimento = escolas.stream()
                .map(escola -> {
                    long matriculasNovas = escola.getAlunos().stream()
                        .filter(a -> a.getDataCriacao().toLocalDate().isAfter(inicio) &&
                                   a.getDataCriacao().toLocalDate().isBefore(fim))
                        .count();

                    return Map.of(
                        "escolaId", escola.getId(),
                        "escolaNome", escola.getNome(),
                        "matriculasNovas", matriculasNovas,
                        "crescimentoPercentual", (double) matriculasNovas / escola.getCapacidade() * 100,
                        "periodoInicio", inicio,
                        "periodoFim", fim
                    );
                })
                .collect(Collectors.toList());

            return Response.ok(crescimento).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Formato de data inválido. Use o formato ISO (YYYY-MM-DD)")
                    .build();
        }
    }
} 