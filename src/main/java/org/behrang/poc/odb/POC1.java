package org.behrang.poc.odb;

import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

import static java.util.Arrays.asList;

/**
 * Proof-of-Concept: Create a simple graph with 2 vertices and an edge between them
 *
 * <pre>
 *     [DIR:"src/main/java/org/behrang/poc/odb"]
 *       ---hashChild-->
 *         [FILE:"src/main/java/org/behrang/poc/odb/POC1.java"]
 * </pre>
 *
 * @author Behrang Saeedzadeh
 */
public class POC1 {

    public static void main(String[] args) {
        // For plocal user/pass should always be admin/admin
        OrientGraphNoTx graph = new OrientGraphNoTx("plocal:databases/poc1", "admin", "admin");

        // Exception will occur if we want to create a vertex type that already exists
        asList("DIR", "FILE").forEach(type -> {
            if (graph.getVertexType(type) == null) {
                graph.createVertexType(type);
            }
        });

        // Remove all vertices and edges from the database
        graph.getVertices().forEach(graph::removeVertex);
        graph.getEdges().forEach(graph::removeEdge);

        // A sample directory and a file within it
        String dirPath = "src/main/java/org/behrang/poc/odb";
        String filePath = "src/main/java/org/behrang/poc/odb/POC1.java";

        // Create vertices
        Vertex dirVertex = graph.addVertex("class:DIR");
        dirVertex.setProperty("path", dirPath);

        Vertex fileVertex = graph.addVertex(filePath);
        fileVertex.setProperty("path", filePath);

        // Create the DIR->hasChild->FILE path
        graph.addEdge("class:hasChild", dirVertex, fileVertex, null);

        // Query the database
        Iterable<Vertex> result = graph.command(new OCommandSQL("SELECT * FROM V")).execute();
        result.forEach(v -> System.out.println((String) v.getProperty("path")));
    }

}
