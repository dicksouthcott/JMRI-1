package jmri.jmrit.logixng.analog.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import javax.annotation.CheckForNull;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads a Memory.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class AnalogExpressionMemory extends AbstractAnalogExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private AnalogExpressionMemory _template;
    private NamedBeanHandle<Memory> _memoryHandle;
    private boolean _listenersAreRegistered = false;
    
    public AnalogExpressionMemory(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        
        super(sys, user);
    }

    private AnalogExpressionMemory(AnalogExpressionMemory template) {
        super(InstanceManager.getDefault(AnalogExpressionManager.class).getNewSystemName(), null);
        _template = template;
        _memoryHandle = _template._memoryHandle;
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getNewObjectBasedOnTemplate() {
        return new AnalogExpressionMemory(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    setMemory((Memory)null);
                }
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }
    
    /** {@inheritDoc} */
    public void setMemory(String memoryName) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setMemory must not be called when listeners are registered");
            log.error("setMemory must not be called when listeners are registered", e);
            throw e;
        }
        if (memoryName != null) {
            Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName);
            if (memory != null) {
                _memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(memoryName, memory);
            } else {
                log.warn("memory '{}' does not exists", memoryName);
            }
        } else {
            _memoryHandle = null;
        }
    }
    
    /** {@inheritDoc} */
    public void setMemory(NamedBeanHandle<Memory> handle) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setMemory must not be called when listeners are registered");
            log.error("setMemory must not be called when listeners are registered", e);
            throw e;
        }
        _memoryHandle = handle;
    }
    
    /** {@inheritDoc} */
    public void setMemory(@CheckForNull Memory memory) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setMemory must not be called when listeners are registered");
            log.error("setMemory must not be called when listeners are registered", e);
            throw e;
        }
        if (memory != null) {
            _memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(memory.getDisplayName(), memory);
        } else {
            _memoryHandle = null;
        }
    }
    
    /** {@inheritDoc} */
    public NamedBeanHandle<Memory> getMemory() {
        return _memoryHandle;
    }
    
    /** {@inheritDoc} */
    @Override
    public double evaluate() {
        if (_memoryHandle != null) {
            return jmri.util.TypeConversionUtil.convertToDouble(_memoryHandle.getBean().getValue(), false);
        } else {
            return 0.0;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void reset() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public FemaleSocket getChild(int index)
            throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public int getChildCount() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public String getShortDescription(Locale locale) {
        if (_memoryHandle != null) {
            return Bundle.getMessage(locale, "AnalogExpressionMemory1", _memoryHandle.getBean().getDisplayName());
        } else {
            return Bundle.getMessage(locale, "AnalogExpressionMemory1", "none");
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getLongDescription(Locale locale) {
        return getShortDescription(locale);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if ((! _listenersAreRegistered) && (_memoryHandle != null)) {
            _memoryHandle.getBean().addPropertyChangeListener("value", this);
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _memoryHandle.getBean().removePropertyChangeListener("value", this);
            _listenersAreRegistered = false;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(AnalogExpressionMemory.class);
    
}
