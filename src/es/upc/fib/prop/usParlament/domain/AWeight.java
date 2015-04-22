package es.upc.fib.prop.usParlament.domain;

import java.util.Vector;

/**
 * Created by alexmirocat on 14/4/15.
 */

//TODO: pre & posts

public class AWeight {

    private AttributeImportance AIList; //Map of attributes' importances.

    private Vector<String> ca; //Vector of common attributes (names).

    public Vector<String> getCommonAttributes() { return ca; }

    public void setCommonAttributes(Vector<String> ca) { this.ca = ca; }

    public int computeWeight(MP m1, MP m2, int div) {
        int w = 0; //0 = no relationship
        for (String a : ca) {
            if (m1.getValue(a).equals(m2.getValue(a))) {
                w += AIList.getImportance(a);
            }
        }
        if (div != 0) {
            int maxW = 3*ca.size(); //maximum grade of relationship
            w /= maxW * div;
        }
        return w;
    }


}
