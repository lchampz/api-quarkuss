package org.acme.controllers;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.DTO.InsertMatriculaDTO;
import org.acme.entities.Escola;
import org.acme.entities.Matricula;
import org.acme.repositories.EscolaRepository;
import org.acme.repositories.MatriculaRepository;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/matriculas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Matrículas", description = "Gerenciamento de matrículas")
public class MatriculaController {

    @Inject
    MatriculaRepository matriculaRepository;

    @Inject
    EscolaRepository escolaRepository;

    private void logRequest(String endpoint) {
        Log.info("[" + java.time.LocalDateTime.now() + "] Endpoint acessado: " + endpoint);
    }

    @GET
    @Operation(summary = "Lista todas as matrículas", description = "Retorna uma lista de todas as matrículas cadastradas.")
    public Response getAllMatriculas() {
        logRequest("/matriculas");
        List<Matricula> matriculas = matriculaRepository.listAll();
        return Response.ok(matriculas).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Adiciona uma matrícula", description = "Adiciona uma nova matrícula ao sistema.")
    public Response addMatricula(InsertMatriculaDTO item) throws Exception {
        logRequest("/matriculas");
        Escola escola = escolaRepository.findById(item.escola);
        if (escola == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Escola não encontrada.").build();
        }
        if (escola.capacidade <= matriculaRepository.countByEscola(escola)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("A escola está lotada.").build();
        }
        Matricula matricula = new Matricula(item.curso, escola, null);
        matriculaRepository.persist(matricula);
        return Response.status(Response.Status.CREATED).entity(item).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Cancela uma matrícula", description = "Remove uma matrícula existente com base no ID.")
    public Response cancelarMatricula(@PathParam("id") Long id) {
        logRequest("/matriculas/" + id);
        Matricula matricula = matriculaRepository.findById(id);
        if (matricula == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Matrícula não encontrada.").build();
        }
        matriculaRepository.delete(matricula);
        return Response.noContent().build();
    }
}