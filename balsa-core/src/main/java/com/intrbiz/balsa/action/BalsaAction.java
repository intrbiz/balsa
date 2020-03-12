package com.intrbiz.balsa.action;

import static com.intrbiz.balsa.BalsaContext.*;

import com.intrbiz.balsa.BalsaApplication;

public interface BalsaAction<A extends BalsaApplication>
{
    default A app()
    {
       return Balsa().app(); 
    }
}
