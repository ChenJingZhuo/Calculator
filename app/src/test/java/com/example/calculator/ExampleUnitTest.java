package com.example.calculator;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void sb(){
        StringBuilder sb = new StringBuilder();
        sb.append("6666\n8888");
        sb.replace(0,sb.length(),sb.substring(0,sb.lastIndexOf("\n")));
        System.out.println(sb.toString());
    }

    @Test
    public void test2(){
        if ("-0.0000000".matches("^[-]?0[.]?[0]*$")){
            System.out.println("true");
        }
    }

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}