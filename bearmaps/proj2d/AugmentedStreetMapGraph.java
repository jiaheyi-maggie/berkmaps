package bearmaps.proj2d;

import bearmaps.proj2ab.Point;
import bearmaps.proj2ab.WeirdPointSet;
import bearmaps.proj2c.streetmap.StreetMapGraph;
import bearmaps.proj2c.streetmap.Node;
import edu.princeton.cs.algs4.TrieSET;


import java.util.*;

/**
 * An augmented graph that is more powerful that a standard StreetMapGraph.
 * Specifically, it supports the following additional operations:
 *
 *
 * @author Alan Yao, Josh Hug, Maggie Yi
 */
public class AugmentedStreetMapGraph extends StreetMapGraph {

    Map<Point, Long> pointToIdMap;
    Map<String, List<Node>> nameToNodeListMap;
    WeirdPointSet kdTree;
    TrieSET trieSet;

    public AugmentedStreetMapGraph(String dbPath) {
        super(dbPath);
        List<Node> nodes = this.getNodes();
        List<Point> points = new LinkedList<>();
        pointToIdMap = new HashMap<>();
        nameToNodeListMap = new HashMap<>();
        trieSet = new TrieSET();

        for (Node node : nodes) {
            if (!neighbors(node.id()).isEmpty()) {
                Point point = new Point(node.lon(), node.lat());
                pointToIdMap.put(point, node.id());
                points.add(point);
            }

            if (node.name() != null) {
                String name = node.name();
                String cleanedName = cleanString(name);
                trieSet.add(cleanedName);
                if (!nameToNodeListMap.containsKey(cleanedName)) {
                    nameToNodeListMap.put(cleanedName, new LinkedList<>());
                }
                nameToNodeListMap.get(cleanedName).add(node);
            }
        }
        kdTree = new WeirdPointSet(points);
    }


    /**
     * For Project Part II
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    public long closest(double lon, double lat) {
        Point res = kdTree.nearest(lon, lat);
        return pointToIdMap.get(res);
    }


    /**
     * For Project Part III (gold points)
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        String prefixCleaned = cleanString(prefix);
        Iterable<String> cleanAll = trieSet.keysWithPrefix(prefixCleaned);
        Set<String> results = new HashSet<>();

        for (String s: cleanAll) {
            for (Node n : nameToNodeListMap.get(s)) {
                results.add(n.name());
            }
        }
        return new LinkedList<>(results);
    }

    /**
     * For Project Part III (gold points)
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" -> Number, The latitude of the node. <br>
     * "lon" -> Number, The longitude of the node. <br>
     * "name" -> String, The actual name of the node. <br>
     * "id" -> Number, The id of the node. <br>
     */
    public List<Map<String, Object>> getLocations(String locationName) {
        List<Map<String, Object>> locations = new LinkedList<>();

        List<Node> nodes = nameToNodeListMap.get(cleanString(locationName));
        if (nodes != null) {
            for (Node n : nodes) {
                Map<String, Object> info = new HashMap<>();
                info.put("lat", n.lat());
                info.put("lon", n.lon());
                info.put("name", n.name());
                info.put("id", n.id());
                locations.add(info);
            }
        }
        return locations;
    }


    /**
     * Useful for Part III. Do not modify.
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

}
