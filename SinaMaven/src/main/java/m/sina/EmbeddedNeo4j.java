package m.sina;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.impl.util.FileUtils;

import javax.management.relation.Relation;

/**
 * Created by zzzzddddgtuwup on 2/14/15.
 */
public class EmbeddedNeo4j {
    private static final String DB_PATH = "neo4j-topic-db";
    GraphDatabaseService graphDb;
    private static enum RelTypes implements RelationshipType {
        REPOST
    }

    public void cleanDb() throws IOException{
        FileUtils.deleteRecursively(new File(DB_PATH));
    }

    public void createDb(){
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
        registerShutdownHook(graphDb);
    }

    public void shutDown() {
        System.out.println();
        System.out.println("Shutting down database ...");
        graphDb.shutdown();
    }

    public Node createNode(String id, String screen_name){
        Node res = null;
        try (Transaction tx = graphDb.beginTx()) {
            res = graphDb.createNode();
            res.setProperty("id", id);
            res.setProperty("name", screen_name);
            tx.success();

        }
        return res;
    }

    public Node createNode(String id, String screen_name, String time){
        try (Transaction tx = graphDb.beginTx()) {
            Node current = graphDb.createNode();
            current.setProperty("id", id);
            current.setProperty("name", screen_name);
            current.setProperty("time",time);
            tx.success();
        }
        return null;
    }
    public Node findById(String id) {
        ExecutionEngine engine = new ExecutionEngine(graphDb);
        ExecutionResult result;
        Node res = null;
        try (Transaction tx = graphDb.beginTx()) {
            result = engine.execute("match (n{id:\"" + id + "\"}) return n");
            Iterator<Node> n_column = result.columnAs("n");
            if (n_column.hasNext()) {
                res = n_column.next();
            }
        }
        return res;
    }

    public Node findByScreenName(String screenName) {
        ExecutionEngine engine = new ExecutionEngine(graphDb);
        ExecutionResult result;
        Node res = null;
        try (Transaction tx = graphDb.beginTx()) {
            result = engine.execute("match (n{name:\"" + screenName + "\"}) return n");
            Iterator<Node> n_column = result.columnAs("n");
            if (n_column.hasNext()) {
                res = n_column.next();
            }
        }
        return res;
    }

    public Relationship findRelByNodesAndTweetId(Node start, Node end, String tweetId) {
        try (Transaction tx = graphDb.beginTx()) {
            for (Relationship neighbor : start.getRelationships(Direction.OUTGOING)) {
                if (neighbor.getEndNode().equals(end) && neighbor.getProperty("tweetId").equals(tweetId)) {
                    return neighbor;
                }
            }
        }
        return null;
    }

    public Relationship findRelByNodes(Node start, Node end) {
        try (Transaction tx = graphDb.beginTx()) {
            for (Relationship neighbor : start.getRelationships(Direction.OUTGOING)) {
                if (neighbor.getEndNode().equals(end)) {
                    return neighbor;
                }
            }
        }
        return null;
    }

    public void addRelWithTimeAndTweetId(Node start, Node end, String time, String tweetId){
        try (Transaction tx = graphDb.beginTx()) {
            Relationship relationship = start.createRelationshipTo(end, RelTypes.REPOST);
            relationship.setProperty("time", time);
            relationship.setProperty("tweetId", tweetId);
            tx.success();
        }
    }

    public void addRelWithTime(Node start, Node end, String time){
        try (Transaction tx = graphDb.beginTx()) {
            Relationship relationship = start.createRelationshipTo(end, RelTypes.REPOST);
            relationship.setProperty("time", time);
            tx.success();
        }
    }

    public String getRelTime(Relationship relationship){
        try (Transaction tx = graphDb.beginTx()) {
            return (String) relationship.getProperty("time");
        }
    }

    public void updateRelTime(Relationship relationship,String time){
        try (Transaction tx = graphDb.beginTx()) {
            relationship.setProperty("time", time);
            tx.success();
        }
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }


}
