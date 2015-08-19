package org.activityinfo.i18n.tools.parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.activityinfo.i18n.tools.model.Message;
import org.activityinfo.i18n.tools.model.TranslationSet;
import org.activityinfo.i18n.tools.output.MessageDecorator;

/**
 *
 */
public class ValidatingVisitor extends VoidVisitorAdapter<Void> {

    private MessageDecorator decorator = new MessageDecorator();

    private final TranslationSet input;
    private TranslationSet validatedSet;

    public ValidatingVisitor(TranslationSet translationSet) {
        this.input = translationSet;
        this.validatedSet = new TranslationSet(translationSet.getLanguage());
    }

    public TranslationSet getValidatedSet() {
        return validatedSet;
    }

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        String key = AstEvaluator.parseTermKey(n);
        if(input.has(key)) {
            String inputMessage = input.get(key);
            if(validateMessage(key, n, inputMessage)) {
                validatedSet.add(key, input.get(key));
            }
        }

        super.visit(n, arg);
    }

    private boolean validateMessage(String key, MethodDeclaration decl, String inputMessage) {

        Message format = new Message(decorator.apply(inputMessage));

        // make sure there are enough arguments for all the placeholders
        for (Message.Chunk chunk : format.getChunks()) {
            if(chunk.isPlaceholder()) {
                if(chunk.getArgumentIndex() >= decl.getParameters().size()) {
                    System.err.println(String.format("Invalid translation %s[%s]: not enough arguments for [%s]",
                            key, input.getLanguage(), inputMessage));
                    return false;
                }
            }
        }


        // And make sure all arguments are used...
        if(decl.getParameters() != null) {
            for (int i = 0; i < decl.getParameters().size(); ++i) {
                if (!format.hasPlaceholder(i)) {
                    System.err.println(String.format("Invalid translation %s[%s]: argument %d is not used: [%s]",
                            key, input.getLanguage(), i, inputMessage));
                    return false;
                }
            }
        }
        return true;
    }
}
