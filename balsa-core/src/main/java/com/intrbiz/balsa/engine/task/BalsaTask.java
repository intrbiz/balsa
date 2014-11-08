package com.intrbiz.balsa.engine.task;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.engine.session.BalsaSession;

public interface BalsaTask
{    
    void run(BalsaApplication application, BalsaSession session, String id);
}
