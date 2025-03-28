/*
Copyright 2017 Platinum Digital Group LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package net.platinumdigitalgroup.jvdf;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * An iterable tree structure that represents a set of key-value pairs in a VDF document.
 * @author Brendan Heinonen
 */
public class VDFNode extends TreeMap<String, Object[]> {
	
	//edit: add a constructor that allows a string comparator to be used
	public VDFNode(Comparator<String> comparator) {
		super(comparator);
	}
	
    /**
     * Puts a key/value pair into the map, or push it to the back of the multimap
     * @param key the key of the value
     * @param value the value which corresponds to the key
     * @return the value
     */
    public Object put(String key, Object value) {
        Object[] values = this.get(key);
        if(values == null) {
            this.put(key, new Object[]{ value });
        } else {
            Object[] appendTo = Arrays.copyOf(values, values.length + 1);
            appendTo[values.length] = value;
            this.put(key, appendTo);
        }
        return value;
    }

    /**
     * Returns the number of values that correspond to the specified key.
     * @param key the key name to get the value count for
     * @return the number of values that correspond to the key
     */
    public int values(String key) {
        Object[] values = this.get(key);
        if(values == null)
            return 0;
        return values.length;
    }

    /**
     * Fetches a string value by name and index.
     * @param key the key name
     * @param index the nth key
     * @return the string value of the specified key, or null if the key does not exist in this node
     */
    public String getString(String key, int index) {
       return (String)this.get(key)[index];
    }

    /**
     * Fetches a string value by name.
     * @param key the key name
     * @return the string value of the specified key, or null if the key does not exist in this node
     */
    public String getString(String key) {
        return getString(key, 0);
    }
    /**
     * Fetches an integer value by name.
     * @param key the key name
     * @return the int value of the specified key, or 0 if the key does not exist in this node
     */
    public int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    /**
     * Fetches a float value by name.
     * @param key the key name
     * @return the float value of the specified key, or 0.f if the key does not exist in this node
     */
    public float getFloat(String key) {
        return Float.parseFloat(getString(key));
    }

    /**
     * Fetches a long value by name.
     * @param key the key name
     * @return the long value of the specified key, or 0 if the key does not exist in this node
     */
    public long getLong(String key) {
        return Long.parseLong(getString(key));
    }

    /**
     * Fetches a pointer value by name.  Java does not have pointers, so this will really return a long.
     * @param key the key name
     * @return the long value of the specified key, or 0 if the key does not exist in this node
     */
    public long getPointer(String key) {
        return Long.parseLong(getString(key), 16);
    }

    /**
     * Proxy for getString for spec compliance reasons.
     * @param key the key name
     * @return the string value of the specified key, or null if the key does not exist in this node
     */
    public String getWideString(String key) {
        return getString(key);
    }

    /**
     * Fetches a color value by name.
     * @param key the key name
     * @return the AWT color value of the specified key, or null if the key does not exist in this node
     */
    public Color getColor(String key) {
        return Color.getColor(getString(key));
    }

    /**
     * Fetches a VDF child node by name.
     * @param key the key name
     * @return a VDFNode instance of the specified key, or null if the key does not exist in this node
     */
    public VDFNode getSubNode(String key) {
        return this.getSubNode(key, 0);
    }

    /**
     * Fetches a VDF child node by name and index.
     * @param key the key name
     * @param index the nth key
     * @return a VDFNode instance of the specified key, or null if the key does not exist in this node
     */
    public VDFNode getSubNode(String key, int index) {
        return (VDFNode)this.get(key)[index];
    }

    /**
     * Reduces multimapped keys into a single key.
     * @param recursive if subnodes should be reduced as well
     * @return this
     */
    public VDFNode reduce(boolean recursive) {
        this.entrySet()
                .parallelStream()
                // filter keys that have more than 1 value (multimapped)
                .filter(e -> e.getValue()[0] instanceof VDFNode)
                .forEach(e -> reduceKeyValue(e, recursive));
        return this;
    }

    private void reduceKeyValue(Map.Entry<String, Object[]> entry, boolean recursive) {
        Object[] nodes = entry.getValue();

        // The first value becomes the node that we're joining
        VDFNode newNode = (VDFNode)nodes[0];

        // If recursion is enabled, we need to reduce every subnode
        if (recursive)
            newNode.reduce(true);

        if(nodes.length <= 1)
            return;


        for (int i = 1, nodesLength = nodes.length; i < nodesLength; i++) {
            VDFNode node = (VDFNode)nodes[i];

            // Merge this node into the new node
            node.join(newNode);
        }

        this.put(entry.getKey(), new Object[] { newNode });
    }

    /**
     * Put the key/value pairs in this node into another node.
     * @param other the node to merge into
     */
    public void join(VDFNode other) {
        for(Map.Entry<String, Object[]> e : this.entrySet()) {
            for(Object o : e.getValue())
                other.put(e.getKey(), o);
        }
    }

    /**
     * Recursively reduces multimapped keys into single keys.
     * @return this
     */
    public VDFNode reduce() {
        return reduce(true);
    }


}
