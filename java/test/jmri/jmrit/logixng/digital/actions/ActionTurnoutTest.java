package jmri.jmrit.logixng.digital.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ActionTurnout
 * 
 * @author Daniel Bergqvist 2018
 */
public class ActionTurnoutTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ActionTurnout actionTurnout;
    private Turnout turnout;
    
    
    @Override
    public ConditionalNG getConditionalNG() {
        return conditionalNG;
    }
    
    @Override
    public LogixNG getLogixNG() {
        return logixNG;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format("Set turnout IT1 to Thrown%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Set turnout IT1 to Thrown%n");
    }
    
    @Test
    public void testCtor() {
        ActionTurnout t = new ActionTurnout("IQDA321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAction() throws SocketAlreadyConnectedException, JmriException {
        // The action is not yet executed so the turnout should be closed
        Assert.assertTrue("turnout is closed",turnout.getCommandedState() == Turnout.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        Assert.assertTrue("turnout is thrown",turnout.getCommandedState() == Turnout.THROWN);
        
        // Test to set turnout to closed
        actionTurnout.setTurnoutState(ActionTurnout.TurnoutState.CLOSED);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        Assert.assertTrue("turnout is thrown",turnout.getCommandedState() == Turnout.CLOSED);
        
        // Test to set turnout to toggle
        actionTurnout.setTurnoutState(ActionTurnout.TurnoutState.TOGGLE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        Assert.assertTrue("turnout is thrown",turnout.getCommandedState() == Turnout.THROWN);
        
        // Test to set turnout to toggle
        actionTurnout.setTurnoutState(ActionTurnout.TurnoutState.TOGGLE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the turnout should be thrown
        Assert.assertTrue("turnout is thrown",turnout.getCommandedState() == Turnout.CLOSED);
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get the action and set the turnout
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        Assert.assertNotNull("Turnout is not null", turnout);
        ActionTurnout action = new ActionTurnout(InstanceManager.getDefault(DigitalActionManager.class).getNewSystemName(), null);
        action.setTurnout(turnout);
        
        // Get some other turnout for later use
        Turnout otherTurnout = InstanceManager.getDefault(TurnoutManager.class).provide("IM99");
        Assert.assertNotNull("Turnout is not null", otherTurnout);
        Assert.assertNotEquals("Turnout is not equal", turnout, otherTurnout);
        
        // Test vetoableChange() for some other propery
        action.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Turnout matches", turnout, action.getTurnout().getBean());
        
        // Test vetoableChange() for a string
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Turnout matches", turnout, action.getTurnout().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Turnout matches", turnout, action.getTurnout().getBean());
        
        // Test vetoableChange() for another turnout
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherTurnout, null));
        Assert.assertEquals("Turnout matches", turnout, action.getTurnout().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherTurnout, null));
        Assert.assertEquals("Turnout matches", turnout, action.getTurnout().getBean());
        
        // Test vetoableChange() for its own turnout
        boolean thrown = false;
        try {
            action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", turnout, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Turnout matches", turnout, action.getTurnout().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", turnout, null));
        Assert.assertNull("Turnout is null", action.getTurnout());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNG();
        
        turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        turnout.setCommandedState(Turnout.CLOSED);
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setEnabled(true);
        actionTurnout = new ActionTurnout(InstanceManager.getDefault(DigitalActionManager.class).getNewSystemName(), null);
        actionTurnout.setTurnout(turnout);
        actionTurnout.setTurnoutState(ActionTurnout.TurnoutState.THROWN);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionTurnout);
        conditionalNG.getChild(0).connect(socket);
        
        _base = actionTurnout;
        _baseMaleSocket = socket;
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
