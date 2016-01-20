package io.agi.core.orm;

/**
 * Creates a simple unit test interface where it returns a system exit code.
 *
 * Created by dave on 10/01/16.
 */
public interface UnitTest {

    /**
     * Simulates the interface for a process, i.e. the test is run independently of anything else.
     *
     * @param args
     * @return
     */
    void test( String[] args );

}
