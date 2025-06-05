package org.acme;

import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
import org.acme.exceptions.ApiError; // Importar ApiError
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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.HashMap;

@Path("/v2")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RateLimit(value = 3, minSpacingUnit = ChronoUnit.SECONDS, minSpacing = 5)
@SecurityScheme(securitySchemeName = "apiKey", type = SecuritySchemeType.APIKEY, apiKeyName = "X-API-Key", in = SecuritySchemeIn.HEADER)
@Tag(name = "V2", description = "Nova versão da API com métodos HTTP especializados e lógica de matrícula atualizada")

public class ApiRoutesV2 {

    @Inject
    AlunoController alunoController;

    @Inject
    EscolaController escolaController;

    @Inject
    MatriculaController matriculaController;

    @Fallback
    public String fallback() {
        return "{\"erro\":\"Taxa de requisições excedida. Tente novamente mais tarde.\"}";
    }

    @GET
    @Path("/alunos")
    @Operation(summary = "Lista todos os alunos", description = "Retorna uma lista de todos os alunos cadastrados.")
    public Response getAllAlunos() {
        return alunoController.getAllAlunos();
    }

    @POST
    @Path("/alunos")
    @Transactional
    @Idempotent
    @Operation(summary = "Adiciona um aluno", description = "Adiciona um novo aluno ao sistema.")
    public Response addAluno(InsertAlunoDTO item) {
        return alunoController.addAluno(item);
    }

    @PATCH
    @Path("/alunos/{id}/status")
    @Transactional
    @Operation(summary = "Atualiza status do aluno", description = "Atualiza apenas o status do aluno (ativo/inativo).")
    public Response updateAlunoStatus(@PathParam("id") Long id, UpdateAlunoStatusDTO status) {
        return alunoController.updateAlunoStatus(id, status);
    }

    @HEAD
    @Path("/alunos/{id}")
    @Transactional
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
    @Transactional
    @Operation(summary = "Adiciona uma escola", description = "Adiciona uma nova escola ao sistema.")
    public Response addEscola(InsertEscolaDTO item) {
        return escolaController.addEscola(item);
    }

    @OPTIONS
    @Path("/escolas/{id}/capacidade")
    @ApiKey
    @Operation(summary = "Verifica operações disponíveis  (Protegido por APIKEY)", description = "Retorna as operações disponíveis para gerenciar a capacidade da escola.")
    public Response getEscolaCapacidadeOptions(@PathParam("id") Long id) {
        return escolaController.getEscolaCapacidadeOptions(id);
    }

    @PATCH
    @Path("/escolas/{id}/capacidade")

    @Transactional
    @Operation(summary = "Atualiza capacidade da escola", description = "Atualiza a capacidade máxima de uma escola existente. Não permite reduzir a capacidade abaixo do número de alunos matriculados ativos.")
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
    @Transactional
    @Path("/matriculas")
    @Idempotent
    @Operation(summary = "Adiciona uma matrícula", description = "Adiciona uma nova matrícula ao sistema, validando vagas disponíveis na escola e o status do aluno/escola.")
    public Response addMatricula(InsertMatriculaDTO item) {
        // A lógica de validação de vagas, status de escola/aluno e duplicidade
        // foi movida para dentro do matriculaController.addMatricula para centralizar as regras.
        // Isso torna a chamada aqui mais limpa e consistente.
        return matriculaController.addMatricula(item);
    }

    @PATCH
    @Transactional
    @Path("/matriculas/{id}/status")
    @Operation(summary = "Atualiza status da matrícula", description = "Atualiza o status da matrícula (ATIVA, CANCELADA).")
    public Response updateMatriculaStatus(@PathParam("id") Long id, UpdateMatriculaStatusDTO status) {
        return matriculaController.updateMatriculaStatus(id, status);
    }

    // Métodos de Relatórios e Estatísticas
    @GET
    @Path("/relatorios/escolas/{id}/ocupacao")
    @Operation(summary = "Relatório de ocupação", description = "Retorna estatísticas de ocupação da escola (baseado em matrículas ativas).")
    public Response getOcupacaoEscola(@PathParam("id") Long id) {
        // Este método no EscolaController já foi ajustado para usar matrículas ativas.
        return escolaController.getOcupacaoEscola(id);
    }

    @GET
    @Path("/relatorios/alunos/idade-media")
    @Operation(summary = "Média de idade", description = "Retorna a média de idade dos alunos por escola (baseado em matrículas ativas).")
    public Response getMediaIdadeAlunos() {
        // A lógica para calcular a média de idade por escola, considerando matrículas ativas,
        // deve ser implementada/ajustada no AlunoController.
        // Por enquanto, apenas repassando a chamada.
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
        // O AlunoController.searchAlunos precisará ser ajustado para filtrar alunos
        // que tenham uma MATRICULA ATIVA na escola especificada, se o parâmetro escolaId for usado.
        return alunoController.searchAlunos(nome, idadeMin, idadeMax, escolaId);
    }

    // Métodos de Lote
    @POST
    @Transactional
    @Path("/alunos/lote")
    @Operation(summary = "Adiciona alunos em lote", description = "Adiciona múltiplos alunos de uma vez.")
    public Response addAlunosLote(List<InsertAlunoDTO> alunos) {
        return alunoController.addAlunosLote(alunos);
    }

    @PATCH
    @Transactional
    @Path("/matriculas/lote/status")
    @Operation(summary = "Atualiza status em lote", description = "Atualiza o status de múltiplas matrículas de uma vez. Validações de capacidade da escola e status de aluno/escola são aplicadas.")
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
                writer.write("ID,Nome,Idade,Data Nascimento,Nome Responsavel,Telefone Responsavel,Email Responsavel,Endereco,Observacoes,Ativo\n");

                for (Aluno aluno : alunos) {
                    writer.write(String.format("%s,%s,%d,%s,%s,%s,%s,%s,%s,%b\n",
                            aluno.id != null ? aluno.id.toString() : "", // PanacheEntity.id pode ser nulo se não persistido ainda
                            aluno.getNome(),
                            aluno.getIdade(),
                            aluno.getDataNascimento() != null ? aluno.getDataNascimento().format(DateTimeFormatter.ISO_LOCAL_DATE) : "",
                            aluno.getNomeResponsavel(),
                            aluno.getTelefoneResponsavel(),
                            aluno.getEmailResponsavel(),
                            aluno.getEndereco(),
                            aluno.getObservacoes(),
                            aluno.getAtivo()
                    ));
                }
                writer.flush();
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
                writer.write("ID,Nome,Capacidade,Endereco,Telefone,Email,Diretor,Ativo\n");
                for (Escola escola : escolas) {
                    writer.write(String.format("%s,%s,%d,%s,%s,%s,%s,%b\n",
                            escola.id != null ? escola.id.toString() : "", // PanacheEntity.id
                            escola.getNome(),
                            escola.getCapacidade(),
                            escola.getEndereco(),
                            escola.getTelefone(),
                            escola.getEmail(),
                            escola.getDiretor(),
                            escola.getAtivo()
                    ));
                }
                writer.flush();
            }
        };

        return Response.ok(stream)
                .header("Content-Disposition", "attachment; filename=escolas_" + LocalDate.now() + ".csv")
                .build();
    }

    @GET
    @Path("/relatorios/escolas/ranking")
    @Operation(summary = "Ranking de escolas", description = "Retorna um ranking das escolas por ocupação (matrículas ativas) e média de idade dos alunos.")
    public Response getEscolasRanking() {
        Response escolasResponse = escolaController.getAllEscolas(); // Obtém todas as escolas
        if (escolasResponse.getStatus() != Response.Status.OK.getStatusCode()) {
            return escolasResponse;
        }
        List<Escola> escolas = (List<Escola>) escolasResponse.getEntity();

        Response matriculasResponse = matriculaController.getAllMatriculas();
        if (matriculasResponse.getStatus() != Response.Status.OK.getStatusCode()) {
            return matriculasResponse;
        }
        List<Matricula> todasMatriculas = (List<Matricula>) matriculasResponse.getEntity();


        List<Map<String, Object>> ranking = escolas.stream()
                .map(escola -> {
                    // Filtrar matrículas ativas para a escola atual
                    List<Matricula> matriculasAtivasDaEscola = todasMatriculas.stream()
                            .filter(m -> m.getEscola() != null && m.getEscola().id.equals(escola.id) &&
                                    m.getStatus() == Matricula.StatusMatricula.ATIVA)
                            .toList();

                    long totalAlunosAtivos = matriculasAtivasDaEscola.size();

                    Double ocupacao = (escola.getCapacidade() > 0) ? (totalAlunosAtivos / (double) escola.getCapacidade() * 100) : 0.0;

                    Double mediaIdade = matriculasAtivasDaEscola.stream()
                            .filter(m -> m.getAluno() != null)
                            .map(Matricula::getAluno)
                            .filter(obj -> true)
                            .mapToInt(Aluno::getIdade)
                            .average()
                            .orElse(0.0);

                    Map<String, Object> map = new HashMap<>();
                    map.put("escolaId", escola.id != null ? escola.id.toString() : "");
                    map.put("escolaNome", escola.getNome());
                    map.put("ocupacaoPercentual", String.format("%.2f%%", ocupacao)); // Formata como porcentagem
                    map.put("mediaIdadeAlunosAtivos", String.format("%.2f", mediaIdade)); // Formata com 2 casas decimais
                    map.put("totalAlunosAtivos", totalAlunosAtivos);
                    map.put("capacidade", escola.getCapacidade());
                    map.put("vagasDisponiveis", escola.getCapacidade() - totalAlunosAtivos);
                    return map;
                })
                .sorted((a, b) -> {
                    // Para ordenar por ocupação, precisamos extrair o valor numérico antes do '%'
                    Double ocupacaoA = Double.parseDouble(((String) a.get("ocupacaoPercentual")).replace("%", ""));
                    Double ocupacaoB = Double.parseDouble(((String) b.get("ocupacaoPercentual")).replace("%", ""));
                    return Double.compare(ocupacaoB, ocupacaoA); // Ordem decrescente de ocupação
                })
                .collect(Collectors.toList());

        return Response.ok(ranking).build();
    }

    @GET
    @Path("/relatorios/alunos/evasao")
    @Operation(summary = "Relatório de evasão", description = "Retorna estatísticas sobre evasão escolar (matrículas canceladas) por período.")
    public Response getRelatorioEvasao(
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim) {

        try {
            if (dataInicio == null || dataFim == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ApiError(400, "Bad Request", "dataInicio e dataFim são obrigatórios.", "/v2/relatorios/alunos/evasao"))
                        .build();
            }
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
            LocalDate inicio = LocalDate.parse(dataInicio, formatter);
            LocalDate fim = LocalDate.parse(dataFim, formatter);

            if (inicio.isAfter(fim)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ApiError(400, "Bad Request", "dataInicio não pode ser posterior a dataFim.", "/v2/relatorios/alunos/evasao"))
                        .build();
            }

            Response matriculasResponse = matriculaController.getAllMatriculas();
            if (matriculasResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                return matriculasResponse;
            }

            List<Matricula> matriculas = (List<Matricula>) matriculasResponse.getEntity();
            List<Map<String, Object>> evasao = matriculas.stream()
                    .filter(m -> m.getDataFim() != null &&
                            m.getStatus() == Matricula.StatusMatricula.CANCELADA &&
                            !m.getDataFim().toLocalDate().isBefore(inicio) && // dataFim >= inicio
                            !m.getDataFim().toLocalDate().isAfter(fim))      // dataFim <= fim
                    .map(m -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("matriculaId", m.id != null ? m.id.toString() : "");
                        if (m.getAluno() != null) {
                            map.put("alunoId", m.getAluno().id != null ? m.getAluno().id.toString() : "");
                            map.put("alunoNome", m.getAluno().getNome());
                        }
                        if (m.getEscola() != null) {
                            map.put("escolaId", m.getEscola().id != null ? m.getEscola().id.toString() : "");
                            map.put("escolaNome", m.getEscola().getNome());
                        }
                        map.put("dataInicioMatricula", m.getDataInicio() != null ? m.getDataInicio().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
                        map.put("dataFimMatricula", m.getDataFim() != null ? m.getDataFim().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
                        map.put("statusMatricula", m.getStatus().toString());
                        map.put("observacoes", m.getObservacoes()); // Usando "observacoes" para o que seria o motivo da evasão
                        return map;
                    })
                    .collect(Collectors.toList());

            return Response.ok(evasao).build();
        } catch (java.time.format.DateTimeParseException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError(400, "Bad Request", "Formato de data inválido. Use o formato YYYY-MM-DD (ISO_LOCAL_DATE).", "/v2/relatorios/alunos/evasao"))
                    .build();
        } catch (Exception e) {
            e.printStackTrace(); // Para debug no servidor
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiError(500, "Internal Server Error", "Ocorreu um erro ao processar o relatório de evasão: " + e.getMessage(), "/v2/relatorios/alunos/evasao"))
                    .build();
        }
    }

    @GET
    @Path("/relatorios/escolas/crescimento")
    @Operation(summary = "Relatório de crescimento", description = "Retorna estatísticas de crescimento das escolas (novas matrículas ativas) por período.")
    public Response getRelatorioCrescimento(
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim) {

        try {
            if (dataInicio == null || dataFim == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ApiError(400, "Bad Request", "dataInicio e dataFim são obrigatórios.", "/v2/relatorios/escolas/crescimento"))
                        .build();
            }
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
            LocalDate inicio = LocalDate.parse(dataInicio, formatter);
            LocalDate fim = LocalDate.parse(dataFim, formatter);

            if (inicio.isAfter(fim)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ApiError(400, "Bad Request", "dataInicio não pode ser posterior a dataFim.", "/v2/relatorios/escolas/crescimento"))
                        .build();
            }

            Response escolasResponse = escolaController.getAllEscolas();
            if (escolasResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                return escolasResponse;
            }
            List<Escola> escolas = (List<Escola>) escolasResponse.getEntity();

            Response matriculasResponse = matriculaController.getAllMatriculas();
            if (matriculasResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                return matriculasResponse;
            }
            List<Matricula> todasMatriculas = (List<Matricula>) matriculasResponse.getEntity();


            List<Map<String, Object>> crescimento = escolas.stream()
                    .map(escola -> {
                        long matriculasNovasNoPeriodo = todasMatriculas.stream()
                                .filter(m -> m.getEscola() != null && m.getEscola().id.equals(escola.id) &&
                                        m.getStatus() == Matricula.StatusMatricula.ATIVA &&
                                        m.getDataInicio() != null &&
                                        !m.getDataInicio().toLocalDate().isBefore(inicio) && // dataInicio >= inicio
                                        !m.getDataInicio().toLocalDate().isAfter(fim))      // dataInicio <= fim
                                .count();

                        long totalAlunosAtivosAtualmente = todasMatriculas.stream()
                                .filter(m -> m.getEscola() != null && m.getEscola().id.equals(escola.id) &&
                                        m.getStatus() == Matricula.StatusMatricula.ATIVA)
                                .count();

                        double crescimentoPercentual = (escola.getCapacidade() > 0) ?
                                ((double) matriculasNovasNoPeriodo / escola.getCapacidade() * 100) : 0.0;

                        Map<String, Object> map = new HashMap<>();
                        map.put("escolaId", escola.id != null ? escola.id.toString() : "");
                        map.put("escolaNome", escola.getNome());
                        map.put("matriculasNovasNoPeriodo", matriculasNovasNoPeriodo);
                        map.put("totalAlunosAtivosAtualmente", totalAlunosAtivosAtualmente);
                        map.put("capacidade", escola.getCapacidade());
                        map.put("crescimentoPercentualSobreCapacidade", String.format("%.2f%%", crescimentoPercentual));
                        map.put("periodoInicio", inicio.toString());
                        map.put("periodoFim", fim.toString());
                        return map;
                    })
                    .collect(Collectors.toList());

            return Response.ok(crescimento).build();
        } catch (java.time.format.DateTimeParseException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError(400, "Bad Request", "Formato de data inválido. Use o formato YYYY-MM-DD (ISO_LOCAL_DATE).", "/v2/relatorios/escolas/crescimento"))
                    .build();
        } catch (Exception e) {
            e.printStackTrace(); // Para debug no servidor
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiError(500, "Internal Server Error", "Ocorreu um erro ao processar o relatório de crescimento: " + e.getMessage(), "/v2/relatorios/escolas/crescimento"))
                    .build();
        }
    }
}