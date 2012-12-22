package com.intrbiz.balsa.engine.view;

import java.io.IOException;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;

public interface BalsaView
{
    void decode(BalsaContext context) throws BalsaException;
    
    void encode(BalsaContext context) throws IOException, BalsaException;
}
