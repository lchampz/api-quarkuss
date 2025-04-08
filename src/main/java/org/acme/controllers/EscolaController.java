package org.acme.controllers;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.DTO.InsertEscolaDTO;
import org.acme.entities.Escola;
import org.acme.repositories.EscolaRepository;
import org.acme.repositories.MatriculaRepository;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/escolas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Escolas", description = "Gerenciamento de escolas")
public class EscolaController {

    @Inject
    EscolaRepository escolaRepository;

    @Inject
    MatriculaRepository matriculaRepository;

    private void logRequest(String endpoint) {
        Log.info("[" + java.time.LocalDateTime.now() + "] Endpoint acessado: " + endpoint);
    }

    @GET
    @Operation(summary = "Lista todas as escolas", description = "Retorna uma lista de todas as escolas cadastradas.")
    public Response getAllEscolas() {
        logRequest("/escolas");
        List<Escola> escolas = escolaRepository.listAll();
        return Response.ok(escolas).build();
    }

    @GET
    @Path("/disponiveis")
    @Operation(summary = "Lista escolas com capacidade disponível", description = "Retorna uma lista de escolas que ainda têm vagas disponíveis.")
    public Response getEscolasComVagas() {
        logRequest("/escolas/disponiveis");
        List<Escola> escolas = escolaRepository.listAll().stream()
                .filter(escola -> matriculaRepository.countByEscola(escola) < escola.capacidade)
                .toList();
        return Response.ok(escolas).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Adiciona uma escola", description = "Adiciona uma nova escola ao sistema.")
    public Response addEscola(InsertEscolaDTO item) {
        logRequest("/escolas");
        if (item.nome == null || item.nome.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("O nome da escola é obrigatório.")
                    .build();
        }
        if (item.capacidade <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("A capacidade da escola deve ser maior que zero.")
                    .build();
        }
        Escola escola = new Escola(item.nome, item.capacidade, null);
        escolaRepository.persist(escola);
        return Response.status(Response.Status.CREATED).entity(item).build();
    }

    @PUT
    @Path("/{id}/capacidade")
    @Transactional
    @Operation(summary = "Atualiza a capacidade de uma escola", description = "Atualiza a capacidade máxima de uma escola existente.")
    public Response atualizarCapacidadeEscola(@PathParam("id") Long id, @QueryParam("novaCapacidade") int novaCapacidade) {
        logRequest("/escolas/" + id + "/capacidade");
        Escola escola = escolaRepository.findById(id);
        if (escola == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Escola não encontrada.").build();
        }
        if (novaCapacidade < 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("A capacidade deve ser maior ou igual a zero.").build();
        }
        escola.capacidade = novaCapacidade;
        escolaRepository.persist(escola);
        return Response.ok(escola).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Remove uma escola", description = "Remove uma escola existente do sistema.")
    public Response deleteEscola(@PathParam("id") Long id) {
        logRequest("/escolas/" + id);
        Escola escola = escolaRepository.findById(id);
        if (escola == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Escola não encontrada.").build();
        }
        escolaRepository.delete(escola);
        return Response.noContent().build();
    }
}
