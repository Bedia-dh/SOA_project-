package com.example.resource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.example.db.DatabaseConnection;
import com.example.model.Person;

@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {

    // GET all persons
    @GET
    public List<Person> getAllPersons() {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT id, name, age FROM persons";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Person p = new Person(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("age")
                );
                persons.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new WebApplicationException("Database error: " + e.getMessage(), 
                Response.Status.INTERNAL_SERVER_ERROR);
        }
        return persons;
    }

    // GET person by ID
    @GET
    @Path("/{id}")
    public Response getPersonById(@PathParam("id") int id) {
        String sql = "SELECT id, name, age FROM persons WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Person p = new Person(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("age")
                );
                return Response.ok(p).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new WebApplicationException("Database error: " + e.getMessage(), 
                Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    // GET person by name (search)
    @GET
    @Path("/search")
    public List<Person> getPersonByName(@QueryParam("name") String name) {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT id, name, age FROM persons WHERE name LIKE ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Person p = new Person(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("age")
                );
                persons.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new WebApplicationException("Database error: " + e.getMessage(), 
                Response.Status.INTERNAL_SERVER_ERROR);
        }
        return persons;
    }

    // POST - add person
    @POST
    public Response addPerson(Person person) {
        String sql = "INSERT INTO persons (name, age) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, person.getName());
            pstmt.setInt(2, person.getAge());
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                person.setId(rs.getInt(1));
            }
            
            return Response.status(Response.Status.CREATED).entity(person).build();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new WebApplicationException("Database error: " + e.getMessage(), 
                Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    // PUT - update person
    @PUT
    @Path("/{id}")
    public Response updatePerson(@PathParam("id") int id, Person person) {
        String sql = "UPDATE persons SET name = ?, age = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, person.getName());
            pstmt.setInt(2, person.getAge());
            pstmt.setInt(3, id);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                person.setId(id);
                return Response.ok(person).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new WebApplicationException("Database error: " + e.getMessage(), 
                Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    // DELETE - remove person
    @DELETE
    @Path("/{id}")
    public Response deletePerson(@PathParam("id") int id) {
        String sql = "DELETE FROM persons WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new WebApplicationException("Database error: " + e.getMessage(), 
                Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}

