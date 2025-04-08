package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.DTO.InsertAlunoDTO;
import org.acme.DTO.InsertEscolaDTO;
import org.acme.DTO.InsertMatriculaDTO;
import org.acme.controllers.AlunoController;
import org.acme.controllers.EscolaController;
import org.acme.controllers.MatriculaController;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

@Tag(name = "Router", description = "Roteamento principal para Alunos, Escolas e Matrículas")
public class ApiRoutes {

    @Inject
    AlunoController alunoController;

    @Inject
    EscolaController escolaController;

    @Inject
    MatriculaController matriculaController;


    @Operation(summary = "Lista todos os alunos", description = "Retorna uma lista de todos os alunos cadastrados.")
    public Response getAllAlunos() {
        return alunoController.getAllAlunos();
    }

    @Operation(summary = "Adiciona um aluno", description = "Adiciona um novo aluno ao sistema.")
    public Response addAluno(InsertAlunoDTO item) {
        return alunoController.addAluno(item);
    }

    @Operation(summary = "Atualiza um aluno", description = "Atualiza os dados de um aluno existente.")
    public Response updateAluno(@PathParam("id") Long id, InsertAlunoDTO item) {
        return alunoController.updateAluno(id, item);
    }

    @Operation(summary = "Remove um aluno", description = "Remove um aluno existente do sistema.")
    public Response deleteAluno(@PathParam("id") Long id) {
        return alunoController.deleteAluno(id);
    }

    @Operation(summary = "Lista todas as escolas", description = "Retorna uma lista de todas as escolas cadastradas.")
    public Response getAllEscolas() {
        return escolaController.getAllEscolas();
    }

    @Operation(summary = "Adiciona uma escola", description = "Adiciona uma nova escola ao sistema.")
    public Response addEscola(InsertEscolaDTO item) {
        return escolaController.addEscola(item);
    }

    @Operation(summary = "Atualiza a capacidade de uma escola", description = "Atualiza a capacidade máxima de uma escola existente.")
    public Response atualizarCapacidadeEscola(@PathParam("id") Long id, @QueryParam("novaCapacidade") int novaCapacidade) {
        return escolaController.atualizarCapacidadeEscola(id, novaCapacidade);
    }

    @Operation(summary = "Remove uma escola", description = "Remove uma escola existente do sistema.")
    public Response deleteEscola(@PathParam("id") Long id) {
        return escolaController.deleteEscola(id);
    }

    @Operation(summary = "Lista todas as matrículas", description = "Retorna uma lista de todas as matrículas cadastradas.")
    public Response getAllMatriculas() {
        return matriculaController.getAllMatriculas();
    }

    @Operation(summary = "Adiciona uma matrícula", description = "Adiciona uma nova matrícula ao sistema.")
    public Response addMatricula(InsertMatriculaDTO item) throws Exception {
        return matriculaController.addMatricula(item);
    }

    @Operation(summary = "Cancela uma matrícula", description = "Remove uma matrícula existente com base no ID.")
    public Response cancelarMatricula(@PathParam("id") Long id) {
        return matriculaController.cancelarMatricula(id);
    }
}