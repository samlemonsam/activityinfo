package org.activityinfo.store.query.impl;

import com.google.common.collect.Lists;
import org.activityinfo.model.expr.FunctionCallNode;
import org.activityinfo.model.expr.functions.ExprFunction;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.service.store.CursorObserver;

import java.util.List;


/**
 * Listens for the values of each of its arguments, and when all are completed, 
 * forwards the result of the function call to a target listener.
 */
public class FunctionCallObserver  {

    private final ExprFunction exprFunction;
    private final CursorObserver<FieldValue> target;
    private final int argumentCount;
    private final List<FieldValue> argumentValues = Lists.newArrayList();
    private final List<CursorObserver<FieldValue>> argumentObservers = Lists.newArrayList();
    
    private int argumentsObserved = 0;
    private boolean done = false;

    public FunctionCallObserver(FunctionCallNode callNode, CursorObserver<FieldValue> target) {
        this.exprFunction = callNode.getFunction();
        this.argumentCount = callNode.getArguments().size();
        this.target = target;

        for (int i = 0; i < argumentCount; i++) {
            argumentObservers.add(new ArgumentObserver(i));
            argumentValues.add(null);
        }
    }

    private void fireOnNext() {
        FieldValue result = exprFunction.apply(argumentValues);
        target.onNext(result);
        
        argumentsObserved = 0;
    }

    public int getArgumentCount() {
        return argumentCount;
    }

    public CursorObserver<FieldValue> getArgumentObserver(int i) {
        return argumentObservers.get(i);
    }

    private class ArgumentObserver implements CursorObserver<FieldValue> {

        private final int index;

        public ArgumentObserver(int index) {
            this.index = index;
        }

        @Override
        public void onNext(FieldValue value) {
            argumentValues.set(index, value);
            argumentsObserved ++;
            if(argumentsObserved == argumentCount) {
                fireOnNext();
            }
        }


        @Override
        public void done() {
            // Fire done() exactly once.
            if(!done) {
                done = true;
                target.done();
            }
        }
    }
}
