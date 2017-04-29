package org.behrang.poc.odb;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.util.Arrays.asList;

/**
 * Proof-of-Concept: Traverse a directory recursively and save it as a graph.
 *
 * Start from {@link POC1}.
 *
 * @author Behrang Saeedzadeh
 */
public class POC2 {

    public static void main(String[] args) throws IOException {
        OrientGraphNoTx graph = new OrientGraphNoTx("plocal:databases/poc2", "admin", "admin");

        asList("DIR", "FILE").forEach(type -> {
            if (graph.getVertexType(type) == null) {
                graph.createVertexType(type);
            }
        });

        graph.getVertices().forEach(graph::removeVertex);
        graph.getEdges().forEach(graph::removeEdge);

        if (args.length != 1 || args[0].trim().isEmpty()) {
            throw new IllegalArgumentException("Pass root directory");
        }

        String dirPath = args[0];
        if (Files.exists(Paths.get(dirPath))) {
            throw new FileNotFoundException(String.format("%s does not exist", dirPath));
        }

        Path rootDir = Paths.get(dirPath);
        AtomicInteger counter = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        Files.walkFileTree(rootDir, new FileVisitor<Path>() {

            Stack<Vertex> dirStack = new Stack<>();

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                int count = counter.incrementAndGet();
                if (count % 10_000 == 0) {
                    System.out.println(count);
                }

                OrientVertex vertex = graph.addVertex("class:DIR");
                vertex.setProperty("path", dir.toAbsolutePath().toString());
                dirStack.push(vertex);

                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                counter.incrementAndGet();

                if (!attrs.isRegularFile()) {
                    return CONTINUE;
                }

                OrientVertex vertex = graph.addVertex("class:FILE");
                vertex.setProperty("path", file.toAbsolutePath().toString());
                graph.addEdge("class:hasChild", dirStack.peek(), vertex, null);

                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                dirStack.pop();
                return CONTINUE;
            }
        });

        long endTime = System.currentTimeMillis();

        System.out.println("Time: " + (endTime - startTime) / 1000.0);
        System.out.println("Files: " + counter);
    }

}
