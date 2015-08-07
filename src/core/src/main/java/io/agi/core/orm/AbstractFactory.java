/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.orm;

/**
 * Useful for making up factories on the fly..
 * 
 * @author dave
 */
public interface AbstractFactory< T > {

    public T create();
    
}
