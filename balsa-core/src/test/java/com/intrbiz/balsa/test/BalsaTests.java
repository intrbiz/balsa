package com.intrbiz.balsa.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.intrbiz.balsa.test.session.SessionTests;

@RunWith(Suite.class)
@SuiteClasses({ SessionTests.class })
public class BalsaTests
{

}
