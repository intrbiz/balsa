package com.intrbiz.balsa.test.route;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import com.intrbiz.balsa.engine.impl.route.Route;
import com.intrbiz.balsa.engine.impl.route.Route.CompiledPattern;

public class RoutePatternTests
{    
    @Test
    public void testAnyGlob()
    {
        CompiledPattern pattern = Route.compileBalsaPattern("/", "/path/**");
        assertThat(pattern.pattern.toString(), is(equalTo("\\A/path/.*\\z")));
    }
    
    @Test
    public void testPathAnyGlob()
    {
        CompiledPattern pattern = Route.compileBalsaPattern("/", "/path/*");
        assertThat(pattern.pattern.toString(), is(equalTo("\\A/path/[^/]*\\z")));
    }
    
    @Test
    public void testAllGlob()
    {
        CompiledPattern pattern = Route.compileBalsaPattern("/", "/path/++");
        assertThat(pattern.pattern.toString(), is(equalTo("\\A/path/.+\\z")));
    }
    
    @Test
    public void testPathAllGlob()
    {
        CompiledPattern pattern = Route.compileBalsaPattern("/", "/path/+");
        assertThat(pattern.pattern.toString(), is(equalTo("\\A/path/[^/]+\\z")));
    }
    
    @Test
    public void testNamedPathParameter()
    {
        CompiledPattern pattern = Route.compileBalsaPattern("/", "/path/:name");
        assertThat(pattern.pattern.toString(), is(equalTo("\\A/path/([^/]+)\\z")));
        assertThat(pattern.as, is(equalTo(new String[] {"name"})));
    }
    
    @Test
    public void testNamedAnyGlob()
    {
        CompiledPattern pattern = Route.compileBalsaPattern("/", "/path/**:name");
        assertThat(pattern.pattern.toString(), is(equalTo("\\A/path/(.*)\\z")));
        assertThat(pattern.as, is(equalTo(new String[] {"name"})));
    }
    
    @Test
    public void testNamedPathAnyGlob()
    {
        CompiledPattern pattern = Route.compileBalsaPattern("/", "/path/*:name");
        assertThat(pattern.pattern.toString(), is(equalTo("\\A/path/([^/]*)\\z")));
        assertThat(pattern.as, is(equalTo(new String[] {"name"})));
    }
    
    @Test
    public void testNamedAllGlob()
    {
        CompiledPattern pattern = Route.compileBalsaPattern("/", "/path/++:name");
        assertThat(pattern.pattern.toString(), is(equalTo("\\A/path/(.+)\\z")));
        assertThat(pattern.as, is(equalTo(new String[] {"name"})));
    }
    
    @Test
    public void testNamedPathAllGlob()
    {
        CompiledPattern pattern = Route.compileBalsaPattern("/", "/path/+:name");
        assertThat(pattern.pattern.toString(), is(equalTo("\\A/path/([^/]+)\\z")));
        assertThat(pattern.as, is(equalTo(new String[] {"name"})));
    }
    
    @Test
    public void testManyNamedPathParameter()
    {
        CompiledPattern pattern = Route.compileBalsaPattern("/", "/path/:name_1/:name_2/test/:name_3");
        assertThat(pattern.pattern.toString(), is(equalTo("\\A/path/([^/]+)/([^/]+)/test/([^/]+)\\z")));
        assertThat(pattern.as, is(equalTo(new String[] {"name_1", "name_2", "name_3"})));
    }
    
    @Test
    public void testMixed1()
    {
        CompiledPattern pattern = Route.compileBalsaPattern("/", "/path/:name_1/*/+/:name_2");
        assertThat(pattern.pattern.toString(), is(equalTo("\\A/path/([^/]+)/[^/]*/[^/]+/([^/]+)\\z")));
        assertThat(pattern.as, is(equalTo(new String[] {"name_1", "name_2"})));
    }
    
    @Test
    public void testMixed2()
    {
        CompiledPattern pattern = Route.compileBalsaPattern("/", "/path/:name/**");
        assertThat(pattern.pattern.toString(), is(equalTo("\\A/path/([^/]+)/.*\\z")));
        assertThat(pattern.as, is(equalTo(new String[] {"name"})));
    }
    
    @Test
    public void testMixed3()
    {
        CompiledPattern pattern = Route.compileBalsaPattern("/", "/path/:name/**:glob");
        assertThat(pattern.pattern.toString(), is(equalTo("\\A/path/([^/]+)/(.*)\\z")));
        assertThat(pattern.as, is(equalTo(new String[] {"name", "glob"})));
    }
}
