package com.intrbiz.balsa.engine.view;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;

public interface BalsaViewParser
{
    /**
     * Parse the given resource into a BalsaView
     * @param resource the resource to parse
     * @param context the current context
     * @return
     * @throws BalsaException
     */
    BalsaView parse(BalsaView previous, BalsaViewSource.Resource resource, BalsaContext context) throws BalsaException;
}