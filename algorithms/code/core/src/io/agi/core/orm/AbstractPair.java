/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.orm;

/**
 * From ideas on StackOverflow. Is it a good idea?
 *
 * @author dave
 */
public class AbstractPair< A, B > {

    public A _first;
    public B _second;

    public AbstractPair() {
        super();
        _first = null;
        _second = null;
    }

    public AbstractPair( A first, B second ) {
        super();
        this._first = first;
        this._second = second;
    }

    @Override
    public int hashCode() {
        int hashFirst = 0;
        int hashSecond = 0;

        if ( _first != null ) hashFirst = _first.hashCode();
        if ( _second != null ) hashSecond = _second.hashCode();

        return ( hashFirst + hashSecond ) * hashSecond + hashFirst;
    }

    @Override
    public boolean equals( Object o ) {
        if ( !( o instanceof AbstractPair ) ) {
            return false;
        }

        AbstractPair ap = ( AbstractPair ) o;

        // evaluate _first:
        if ( _first == null ) {
            if ( ap._first != null ) {
                return false;
            }
        }
        else { // not null
            if ( ap._first == null ) {
                return false;
            }
            // both non-null:
            if ( !_first.equals( ap._first ) ) {
                return false;
            }
        }

        // evaluate _second:
        if ( _second == null ) {
            if ( ap._second != null ) {
                return false;
            }
        }
        else { // not null
            if ( ap._second == null ) {
                return false;
            }
            // both non-null:
            if ( !_second.equals( ap._second ) ) {
                return false;
            }
        }

        return true;
    }
}
