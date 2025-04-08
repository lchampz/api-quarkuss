package org.acme.controllers;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.DTO.InsertAlunoDTO;
import org.acme.DTO.InsertMatriculaDTO;
import org.acme.entities.Aluno;
import org.acme.entities.Escola;
import org.acme.entities.Matricula;
import org.acme.repositories.AlunoRepository;
import org.acme.repositories.EscolaRepository;
import org.acme.repositories.MatriculaRepository;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/alunos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Alunos", description = "Gerenciamento de alunos")
public class AlunoController {

    @Inject
    AlunoRepository alunoRepository;

    @Inject
    MatriculaRepository matriculaRepository;

    private void logRequest(String endpoint) {
        Log.info("[" + java.time.LocalDateTime.now() + "] Endpoint acessado: " + endpoint);
    }

    @GET
    @Operation(summary = "Lista todos os alunos", description = "Retorna uma lista de todos os alunos cadastrados.")
    public Response getAllAlunos() {
        logRequest("/alunos");
        List<Aluno> alunos = alunoRepository.listAll();
        return Response.ok(alunos).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Busca alunos por nome", description = "Retorna uma lista de alunos que correspondem ao nome fornecido.")
    public Response searchAlunos(@QueryParam("nome") String nome) {
        logRequest("/alunos/search");
        List<Aluno> alunos = alunoRepository.find("nome like ?1", "%" + nome + "%").list();
        return Response.ok(alunos).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Adiciona um aluno", description = "Adiciona um novo aluno ao sistema.")
    public Response addAluno(InsertAlunoDTO item) {
        logRequest("/alunos");
        if (item.nome == null || item.nome.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("O nome do aluno é obrigatório.")
                    .build();
        }
        Aluno aluno = new Aluno(item.nome, null);
        alunoRepository.persist(aluno);
        return Response.status(Response.Status.CREATED).entity(item).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Atualiza um aluno", description = "Atualiza os dados de um aluno existente.")
    public Response updateAluno(@PathParam("id") Long id, InsertAlunoDTO item) {
        logRequest("/alunos/" + id);
        Aluno aluno = alunoRepository.findById(id);
        if (aluno == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Aluno não encontrado.").build();
        }
        aluno.nome = item.nome;
        alunoRepository.persist(aluno);
        return Response.ok(aluno).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Remove um aluno", description = "Remove um aluno existente do sistema.")
    public Response deleteAluno(@PathParam("id") Long id) {
        logRequest("/alunos/" + id);
        Aluno aluno = alunoRepository.findById(id);
        if (aluno == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Aluno não encontrado.").build();
        }
        alunoRepository.delete(aluno);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/matriculas")
    @Operation(summary = "Lista matrículas de um aluno", description = "Retorna todas as matrículas associadas a um aluno específico.")
    public Response getMatriculasPorAluno(@PathParam("id") Long id) {
        logRequest("/alunos/" + id + "/matriculas");
        Aluno aluno = alunoRepository.findById(id);
        if (aluno == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Aluno não encontrado.").build();
        }
        List<Matricula> matriculas = matriculaRepository.find("aluno", aluno).list();
        return Response.ok(matriculas).build();
    }
}