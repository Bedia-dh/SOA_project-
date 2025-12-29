package com.example.resource;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.example.model.Person;
import com.example.util.JPAUtil;

@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class PersonResource {

    private static final Logger LOGGER = Logger.getLogger(PersonResource.class.getName());

    @PreDestroy
    public void shutdown() {
        JPAUtil.close();
    }

    @GET
    public List<Person> getAllPersons() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createNamedQuery("Person.findAll", Person.class).getResultList();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/{id}")
    public Response getPersonById(@PathParam("id") int id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Person person = em.find(Person.class, id);
            if (person == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(person).build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/search")
    public List<Person> getPersonByName(@QueryParam("name") String name) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            if (name == null || name.trim().isEmpty()) {
                return em.createNamedQuery("Person.findAll", Person.class).getResultList();
            }
            TypedQuery<Person> query = em.createNamedQuery("Person.searchByName", Person.class);
            query.setParameter("name", name.trim());
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @POST
    public Response addPerson(Person person) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(person);
            tx.commit();
            return Response.status(Response.Status.CREATED).entity(person).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create person", e);
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new WebApplicationException("Persistence error: " + e.getMessage(),
                Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            em.close();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updatePerson(@PathParam("id") int id, Person person) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Person existing = em.find(Person.class, id);
            if (existing == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            tx.begin();
            existing.setName(person.getName());
            existing.setAge(person.getAge());
            tx.commit();
            return Response.ok(existing).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update person with id " + id, e);
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new WebApplicationException("Persistence error: " + e.getMessage(),
                Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            em.close();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deletePerson(@PathParam("id") int id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Person existing = em.find(Person.class, id);
            if (existing == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            tx.begin();
            em.remove(existing);
            tx.commit();
            return Response.noContent().build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to delete person with id " + id, e);
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new WebApplicationException("Persistence error: " + e.getMessage(),
                Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            em.close();
        }
    }
}

