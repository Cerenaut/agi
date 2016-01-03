package io.agi.core.alg;

import io.agi.core.orm.AbstractFactory;

/**
 * Created by dave on 28/12/15.
 */
public class ColumnFactory implements AbstractFactory< Column > {

    @Override
    public Column create() {
        return new Column();
    }

}
