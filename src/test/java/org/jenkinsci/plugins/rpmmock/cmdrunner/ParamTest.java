package org.jenkinsci.plugins.rpmmock.cmdrunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ParamTest {

    private final Param.ParamType type;
    private final String stringValue;
    private Param param;

    public ParamTest( Param.ParamType type, String stringValue ){
        this.type = type;
        this.stringValue = stringValue;
    }

    @Parameters
    public static Collection<Object[]> paramsAsserts(){
        return Arrays.asList(
                new Object[][]{
                        { Param.ParamType.NAMELESS, "test" },
                        { Param.ParamType.DEFAULT, "--testName=\"test\"" },
                        { Param.ParamType.NO_VALUE, "-testName" },
                        { Param.ParamType.NO_VALUE_LONG, "--testName" },
                        { Param.ParamType.SIMPLE, "-testName test" },

                }
        );
    }

    @Before
    public void initialize(){
        param = new Param("testName", type);
    }

    @Test
    public void testParamToString() throws Exception {
        param.setValue("test");
        assertEquals( stringValue, param.toString());
    }
}
