package edu.mda.bcb.matrix;

import java.util.Comparator;

/**
 *
 * @author cjacoby
 */

public class Header<T> {
    
    final public String label;          // The headers label
    final public T index;               // The headers index. Generic, but probably int or long.
    
    public Header(String label, T index) {
        this.label = label;
        this.index = index;
    }
    
    public static Comparator HEADER_SORTED_ORDER = new Comparator<Header>() {
        
        @Override
        public int compare(Header h1, Header h2) {
            return h1.label.compareTo(h2.label);
        }
    
    };
    
}
