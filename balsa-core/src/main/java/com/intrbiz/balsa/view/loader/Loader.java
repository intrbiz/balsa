package com.intrbiz.balsa.view.loader;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.view.component.View;

public interface Loader
{

    View load(View previous, String name, BalsaContext context)  throws BalsaException;
}
