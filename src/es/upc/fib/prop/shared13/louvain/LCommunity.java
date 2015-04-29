package es.upc.fib.prop.shared13.louvain;

import es.upc.fib.prop.shared13.Node;

import java.util.Set;

/**
 * Created by miquel on 25/04/15.
 */
public class LCommunity extends Node
{
    static private Integer nextID = 0;
    private Integer id;
    private Set<Node> sons;

    public Integer getId()
    {
        return id;
    }

    public LCommunity()
    {
        id = LCommunity.nextID++;
    }

    @Override
    public boolean equals(Object o)
    {
        if(! o.getClass().equals(this.getClass())) return false;
        return ((LCommunity)o).id == this.id;
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override
    public String toString()
    {
        return "c"+id.toString();
    }

    @Override
    public int compareTo(Node n)
    {
        return ((LCommunity)n).getId()-this.id;
    }
}
