/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.orm;

/**
 * Defer creating a factory type or avoid boilerplate factory types where not needed.
 * 
 * @author dave
 */
public interface AbstractFactory< T > {

    public T create();
    
}
