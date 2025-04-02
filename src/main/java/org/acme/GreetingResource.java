package org.acme;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.acme.entities.Aluno;
import org.acme.entities.Escola;
import org.acme.repositories.AlunoRepository;
import org.acme.repositories.EscolaRepository;

import java.util.List;

@Path("/")
public class GreetingResource {

    @Inject
    AlunoRepository alunoRepository;
    @Inject
    EscolaRepository escolaRepository;

    private void log() {
        Log.infof("Called on %s", Thread.currentThread());
    }

    @GET
    @Path("/alunos")
    public List<Aluno> getAllAlunos() {
        log();
        return alunoRepository.listAll();
    }

    @GET
    @Path("/escolas")
    public List<Escola> getAllEscolas() {
        log();
        return escolaRepository.listAll();
    }

    @POST
    @Transactional
    @Path("/escola")
    public Response addEscola(Escola item) {
        log();
        escolaRepository.addEscola(item);
        return Response.status(Response.Status.CREATED).entity(item).build();
    }

    @POST
    @Transactional
    @Path("/aluno")
    public Response addAluno(Aluno item) {
        log();
        alunoRepository.addAluno(item);
        return Response.status(Response.Status.CREATED).entity(item).build();
    }


//    @POST
//    @Transactional
//    public Response create(@Valid Todo item) {
//        log();
//        item.persist();
//        return Response.status(Status.CREATED).entity(item).build();
//    }
//
//    @PATCH
//    @Path("/{id}")
//    @Transactional
//    public Response update(@Valid Todo todo, @PathParam("id") Long id) {
//        log();
//        Todo entity = Todo.findById(id);
//        entity.id = id;
//        entity.completed = todo.completed;
//        entity.order = todo.order;
//        entity.title = todo.title;
//        entity.url = todo.url;
//        return Response.ok(entity).build();
//    }
//
//    @DELETE
//    @Transactional
//    public Response deleteCompleted() {
//        log();
//        Todo.deleteCompleted();
//        return Response.noContent().build();
//    }
//
//    @DELETE
//    @Transactional
//    @Path("/{id}")
//    public Response deleteOne(@PathParam("id") Long id) {
//        log();
//        Todo entity = Todo.findById(id);
//        if (entity == null) {
//            throw new WebApplicationException("Todo with id of " + id + " does not exist.",
//                    Status.NOT_FOUND);
//        }
//        entity.delete();
//        return Response.noContent().build();
//    }

}
